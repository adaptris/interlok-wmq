package com.adaptris.core.wmq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

/**
 * A dedicated class for producing MQMessages to WMQ. To produce a message to WQM you would usually need a few things; - An instance
 * of <code>con.ibm.mq.MQMessage</code> - A <code>NativeConnection</code> - And finally <code>MessageOptions</code>
 *
 * This class will take your connection, message options and MQMessage and produce the message to your WMQ queue.
 *
 * @author amcgrath
 *
 */
class MQMessageProducer {

  protected transient Log log = LogFactory.getLog(this.getClass().getName());

  /**
   * Your WMQ broker details.
   */
  private transient NativeConnection connection;
  /**
   * Message options that contain specific WMQ detail for opening, closing and writing messages to queues.
   */
  private transient MessageOptions options;

  /**
   * Will produce your <code>com.ibm.mq.MQMessage</code> to the queue specified by your <code>ProduceDestination</code>.
   *
   * @param msg the <code>MQMessage</code>
   * @param dest the {@link ProduceDestination}, generally should be ConfiguredProduceDestination
   * @throws {@link ProduceException} on exception.
   */
  public void produce(MQMessage msg, String queueName) throws ProduceException {
    try {
      int queueOpenOptions = checkQueueOptions();
      int messageOptions = checkMessageOptions();

      MQQueueManager qm = connection.connect();
      MQQueue mqQueue = qm.accessQueue(queueName, queueOpenOptions);

      mqQueue.closeOptions = getOptions().queueCloseOptionsIntValue();
      MQPutMessageOptions putOpt = new MQPutMessageOptions();
      putOpt.options = messageOptions;

      log.trace("Writing message to " + mqQueue.name);
      mqQueue.put(msg, putOpt);
      mqQueue.close();
      connection.disconnect(qm);
      log.info("msg produced to destination [" + mqQueue.name + "]");
    }
    catch (Exception ex) {
      throw new ProduceException(ex);
    }
  }

  private int checkQueueOptions() throws CoreException {
    int optionsInt = getOptions().queueOpenOptionsIntValue();
    if ((optionsInt & MQC.MQOO_SET_ALL_CONTEXT) <= 0 && (optionsInt & MQC.MQOO_SET_IDENTITY_CONTEXT) <= 0) {
      log.trace("Overriding queueOpenOptions with additional MQC.MQOO_SET_ALL_CONTEXT");
      optionsInt |= MQC.MQOO_SET_ALL_CONTEXT;
    }
    return optionsInt;
  }

  private int checkMessageOptions() throws CoreException {
    int options = getOptions().messageOptionsIntValue();
    if ((options & MQC.MQPMO_SET_ALL_CONTEXT) <= 0 && (options & MQC.MQPMO_SET_IDENTITY_CONTEXT) <= 0) {
      log.trace("Configured FieldMappers : overriding " + "messageOptions with additional MQC.MQPMO_SET_ALL_CONTEXT");
      options |= MQC.MQPMO_SET_ALL_CONTEXT;
    }
    return options;
  }

  public NativeConnection getConnection() {
    return connection;
  }

  public void setConnection(NativeConnection connection) {
    this.connection = connection;
  }

  public MessageOptions getOptions() {
    return options;
  }

  public void setOptions(MessageOptions options) {
    this.options = options;
  }
}
