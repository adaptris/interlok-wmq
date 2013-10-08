package com.adaptris.core.wmq;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceDestination;
import com.adaptris.util.KeyValuePair;
import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MQMessageProducerTest extends TestCase {

  /**
   * This tests the MQMessageProducer It also tests
   * ForwardingNativeConsumerErrorHandler
   */
  private static final String ERROR_DESTINATION = "Error Destination";

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
      setThreadingPolicy(new Synchroniser());
    }
  };

  private MQException exceptionNoQueue = context.mock(MQException.class);
  private MQMessage mqMsg = context.mock(MQMessage.class);
  private MQQueueManager mqQueueManager = context.mock(MQQueueManager.class);
  private MQQueue mqQueue = context.mock(MQQueue.class);
  private ProduceDestination errorDestination = context
      .mock(ProduceDestination.class);
  private NativeConsumer nativeConsumer = context.mock(NativeConsumer.class);
  private ForwardingNativeConsumerErrorHandler errorHandler;
  private MessageOptions msgOptions;
  private AttachedConnection attConn = context.mock(AttachedConnection.class);

  @Override
  protected void setUp() throws Exception {
    exceptionNoQueue = new MQException(MQException.MQCC_FAILED,
        MQException.MQRC_Q_DELETED, "Mock Test");

    errorHandler = new ForwardingNativeConsumerErrorHandler();
    errorHandler.registerParentConsumer(nativeConsumer);
    errorHandler.setDestination(errorDestination);

    msgOptions = new MessageOptions();
  }

  public void testOnError() throws Exception {
    context.checking(new Expectations() {
      {
        allowing(errorDestination).getDestination(with(any(AdaptrisMessage.class)));
              will(returnValue(ERROR_DESTINATION));
        allowing(nativeConsumer).getOptions();
              will(returnValue(msgOptions));
        allowing(nativeConsumer).retrieveConnection(with(any(Class.class)));
              will(returnValue(attConn));
        allowing(attConn).connect();
              will(returnValue(mqQueueManager));
        allowing(mqQueueManager).accessQueue(with(any(String.class)), with(any(int.class)));
            will(returnValue(mqQueue));
        allowing(attConn);

        atLeast(2).of(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
        allowing(mqQueue).close();
            throwException(exceptionNoQueue);
      }
    });

    createAttachedConnection();

    errorHandler.onError(mqMsg);

    // Repeat with message options
    errorHandler.setOptions(new MessageOptions());
    errorHandler.getOptions().addMessageOption("MQPMO_SET_ALL_CONTEXT");
    errorHandler.getOptions().addMessageOption("MQPMO_SET_IDENTITY_CONTEXT");
    errorHandler.getOptions().addQueueOpenOption("MQOO_SET_ALL_CONTEXT");
    errorHandler.getOptions().addQueueOpenOption("MQOO_SET_IDENTITY_CONTEXT");
    errorHandler.onError(mqMsg);

    context.assertIsSatisfied();
  }

  public void testMQProducerBasic() throws Exception {
    MQMessageProducer producer = new MQMessageProducer();
    producer.setConnection(attConn);
    NativeConnection conn = producer.getConnection();
    assertEquals(attConn, conn);
  }

  private void createAttachedConnection() {
    attConn.setQueueManager("your_Q_Manager");
    attConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
    attConn.setWorkersFirstOnShutdown(true);
    attConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_NULL_MD5"));
  }
}
