/*
 * $RCSfile: ProducerDelegate.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/07/24 12:27:28 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import java.util.Date;

import org.apache.commons.logging.Log;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.wmq.MQMessageFactory.MQMessageInstance;
import com.adaptris.core.wmq.mapping.FieldMapper;
import com.adaptris.util.text.DateFormatUtil;
import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

/**
 * Proxy class to handle the actual produce of the AdaptrisMessage to
 * WebsphereMQ
 * <p>
 * This is primarily to avoid any NoClassDef errors when starting up the Adapter
 * without the appropriate WebsphereMQ jars. This is a castor-workaround.
 * </p>
 *
 * @author lchan
 *
 */
class ProducerDelegate {

  private NativeProducer adpProducer;
  private transient Log logR;

  private ProducerDelegate() {

  }

  ProducerDelegate(NativeProducer p, Log log) {
    this();
    logR = log;
    adpProducer = p;
  }

  void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    String queueName;
    try {
      queueName = dest.getDestination(msg);
      int queueOpenOptions = checkQueueOptions();
      int messageOptions = checkMessageOptions();
      MQQueueManager qm = adpProducer.retrieveConnection(NativeConnection.class).connect();
      MQQueue mqQueue = qm.accessQueue(queueName, queueOpenOptions);

      mqQueue.closeOptions = adpProducer.getOptions().queueCloseOptionsIntValue();
      MQMessageInstance msgHandler = MQMessageFactory.create(adpProducer.getOptions().getMessageFormat());
      MQMessage mqMsg = msgHandler.create();
      MQPutMessageOptions putOpt = new MQPutMessageOptions();
      putOpt.options = messageOptions;
      if ((messageOptions & MQC.MQPMO_SET_ALL_CONTEXT) > 0) {
        FieldMapper.Field.putDateTime.setMqField(mqMsg, DateFormatUtil.format(new Date()), null);
      }
      for (FieldMapper f : adpProducer.getFieldMappers()) {
        f.copy(msg, mqMsg);
      }
      msgHandler.write(msg, mqMsg);
      logR.trace("Writing message to " + mqQueue.name);
      mqQueue.put(mqMsg, putOpt);
      mqQueue.close();
      adpProducer.retrieveConnection(NativeConnection.class).disconnect(qm);
      logR.info("msg produced to destination [" + mqQueue.name + "]");
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
  }

  private int checkQueueOptions() throws CoreException {
    int options = adpProducer.getOptions().queueOpenOptionsIntValue();
    if (adpProducer.checkOptions()) {
      if (adpProducer.getFieldMappers().size() > 0) {
        if ((options & MQC.MQOO_SET_ALL_CONTEXT) <= 0 && (options & MQC.MQOO_SET_IDENTITY_CONTEXT) <= 0) {
          logR.trace("Configured FieldMappers : overriding " + "queueOpenOptions with additional MQC.MQOO_SET_ALL_CONTEXT");
          options |= MQC.MQOO_SET_ALL_CONTEXT;
        }
      }
    }
    return options;
  }

  private int checkMessageOptions() throws CoreException {
    int options = adpProducer.getOptions().messageOptionsIntValue();
    if (adpProducer.checkOptions()) {
      if (adpProducer.getFieldMappers().size() > 0) {
        if ((options & MQC.MQPMO_SET_ALL_CONTEXT) <= 0 && (options & MQC.MQPMO_SET_IDENTITY_CONTEXT) <= 0) {
          logR.trace("Configured FieldMappers : overriding " + "messageOptions with additional MQC.MQPMO_SET_ALL_CONTEXT");
          options |= MQC.MQPMO_SET_ALL_CONTEXT;
        }
      }
    }
    return options;
  }
}
