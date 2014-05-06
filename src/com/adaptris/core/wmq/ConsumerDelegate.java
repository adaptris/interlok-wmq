/*
 * $RCSfile: ConsumerDelegate.java,v $
 * $Revision: 1.7 $
 * $Date: 2008/11/14 14:55:09 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import java.io.IOException;

import org.apache.commons.logging.Log;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.wmq.MQMessageFactory.MQMessageInstance;
import com.adaptris.core.wmq.mapping.FieldMapper;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

/**
 * Proxy class to handle the actual consume of the AdaptrisMessage from WebsphereMQ
 * <p>
 * This is primarily to avoid any NoClassDef errors when starting up the Adapter without the appropriate WebsphereMQ jars. This is a
 * castor-workaround.
 * </p>
 *
 * @author lchan
 *
 */
class ConsumerDelegate {

  private transient NativeConsumer adpConsumer;
  private transient Log logger;
  private transient MQMessageAccessor messageAccessor;
  private transient MQMessageOptionsAccessor messageOptionsAccessor;

  private NativeErrorHandler errorHandler;

  private ConsumerDelegate() {

  }

  ConsumerDelegate(NativeConsumer c, Log log) {
    messageAccessor = new MQMessageAccessor();
    messageOptionsAccessor = new MQMessageOptionsAccessor();
    adpConsumer = c;
    logger = log;
    try {
      c.getOptions().addMessageOption("MQGMO_NO_WAIT");
    }
    catch (CoreException e) {
      logger.error(e);
    }
  }

  int processMessages() {
    int count = 0;
    String queueName = adpConsumer.getDestination().getDestination();
    MQQueue mqQueue = null;
    MQQueueManager qm = null;
    try {
      qm = adpConsumer.retrieveConnection(NativeConnection.class).connect();
      mqQueue = qm.accessQueue(queueName, adpConsumer.getOptions().queueOpenOptionsIntValue());
      mqQueue.closeOptions = adpConsumer.getOptions().queueCloseOptionsIntValue();
    }
    catch (MQException e) {
      logger.error("Failed to open WebsphereMQ Queue " + queueName + " will re-attempt on next schedule");
      logException(e);
      return count;
    }
    catch (CoreException ex) {
      logger.error(ex.getMessage());
      logException(ex);
      return count;
    }
    try {
      MQMessageInstance handler = MQMessageFactory.create(adpConsumer.getOptions().getMessageFormat());
      boolean carryOn = false;
      do {
        carryOn = consumeMessage(mqQueue, handler);
        if (carryOn) {
          count++;
        }
        if (!adpConsumer.continueProcessingMessages()) {
          break;
        }
      }
      while (carryOn);
    }
    catch (Exception e) {
      logger.error("Failed to receive messages from Websphere MQ, will re-attempt on next schedule");
      logException(e);
    }
    finally {
      close(mqQueue);
      close(qm);
    }
    return count;
  }

  // redmineID #391
  // When reading alias queues from WMQ, the NativeConsumer throws an exception (reason code 2068) when calling
  // mqQueue.getCurrentDepth(). This is because, as an alias it theoretically doesn't have a depth - you would have to resolve the
  // underlying local queue and check that.
  //
  // However, even this isn't guaranteed to work as the getCurrentDepth() method will also fail if the queue is a cluster queue and
  // doesn't have a local instance.
  //
  // Having read around the subject, getCurrentDepth() is actually quite inefficient and is only really meant for admin api's where
  // performance isn't an issue. Apparently what we are meant to do is to simply call MQQueue.get() and catch any exceptions. If the
  // Reason code is 2033 (no messages) then we ignore the error and return as if current depth == 0. Otherwise we treat as an
  // unexpected error.
  //
  // Lewin - Personally I consider this a VERY LAME FEATURE.
  private boolean consumeMessage(MQQueue mqQueue, MQMessageInstance handler) throws Exception {
    boolean messageConsumed = false;
    AdaptrisMessage msg = AdaptrisMessageFactory.defaultIfNull(adpConsumer.getMessageFactory()).newMessage();
    MQMessage mqMsg = accessMQMessage(handler.create());
    for (FieldMapper f : adpConsumer.getPreGetFieldMappers()) {
      f.copy(msg, mqMsg);
    }
    
    MQGetMessageOptions getOpt = createMQGetMessageOptions();
    
    try {
      mqQueue.get(mqMsg, getOpt);
      handler.write(mqMsg, msg);
      for (FieldMapper f : adpConsumer.getFieldMappers()) {
        f.copy(mqMsg, msg);
      }
      adpConsumer.retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
      messageConsumed = true;
    }
    catch (MQException e) {
      if (e.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
        messageConsumed = false;
      }
      else {
        runErrorHandler(mqMsg, e);
      }
    }
    catch (IOException ioex) {
      runErrorHandler(mqMsg, ioex);
    }
    return messageConsumed;
  }
  private MQGetMessageOptions createMQGetMessageOptions() throws Exception{
    MQGetMessageOptions getOpt = new MQGetMessageOptions();
    getOpt.options = adpConsumer.getOptions().messageOptionsIntValue();
    return accessMQGetMessageOptions(getOpt);
  }
  
  /*
   * These "access" methods have been added to allow mocking
   * By stubbing this method a test can use it's own mock MQMessage/MQGetMessageOptions
   */
  public MQMessage accessMQMessage(MQMessage mqMsg){
    return messageAccessor.accessMessage(mqMsg);
  }

  public MQGetMessageOptions accessMQGetMessageOptions(MQGetMessageOptions options){
    return messageOptionsAccessor.accessMessageOptions(options);
  }
  
  private void runErrorHandler(MQMessage message, Exception ex) throws Exception {
    if (getErrorHandler() == null) {
      throw ex;
    }
    else {
      logger.debug("Running error handler.");
      ((AbstractNativeErrorHandler) getErrorHandler()).registerParentConsumer(adpConsumer);
      getErrorHandler().onError(message);
    }
  }

  private void close(MQQueue mqQueue) {
    if (mqQueue == null) {
      return;
    }
    try {
      mqQueue.close();
    }
    catch (MQException e) {
      logger.warn("Error closing queue " + mqQueue.name);
      logException(e);
    }
  }

  private void close(MQQueueManager mqQmgr) {
    if (mqQmgr == null) {
      return;
    }
    try {
      adpConsumer.retrieveConnection(NativeConnection.class).disconnect(mqQmgr);
    }
    catch (MQException e) {
      logger.warn("Error disconnecting Queue Manager");
      logException(e);
    }
  }

  private void logException(Exception e) {
    if (adpConsumer.getLogAllExceptions()) {
      logger.error(e.getMessage(), e);
    }
  }

  public NativeErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setErrorHandler(NativeErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public MQMessageAccessor getMessageAccessor() {
    return messageAccessor;
  }

  public void setMessageAccessor(MQMessageAccessor messageAccessor) {
    this.messageAccessor = messageAccessor;
  }

  public MQMessageOptionsAccessor getMessageOptionsAccessor() {
    return messageOptionsAccessor;
  }

  public void setMessageOptionsAccessor(
      MQMessageOptionsAccessor messageOptionsAccessor) {
    this.messageOptionsAccessor = messageOptionsAccessor;
  }
}
