package com.adaptris.core.wmq;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceDestination;
import com.adaptris.util.KeyValuePair;
import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

import junit.framework.TestCase;

public class MQMessageProducerTest extends TestCase{

	/**
	 * This tests the MQMessageProducer
	 * It also tests ForwardingNativeConsumerErrorHandler
	 */
	private static final String ERROR_DESTINATION = "Error Destination";
	
	private MQException exceptionNoQueue;
	@Mock private MQMessage mqMsg;
	@Mock private MQQueueManager mqQueueManager;
	@Mock private MQQueue mqQueue;
	@Mock private ProduceDestination errorDestination;
	@Mock private NativeConsumer nativeConsumer;
	private ForwardingNativeConsumerErrorHandler errorHandler;
	private MessageOptions msgOptions;
	private AttachedConnection attConn;
	
	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		exceptionNoQueue = new MQException(MQException.MQCC_FAILED, MQException.MQRC_Q_DELETED, "Mock Test");
		
		errorHandler = new ForwardingNativeConsumerErrorHandler();
		errorHandler.registerParentConsumer(nativeConsumer);
		errorHandler.setDestination(errorDestination);
		when(errorDestination.getDestination((AdaptrisMessage)Matchers.anyObject())).thenReturn(ERROR_DESTINATION);

		msgOptions = new MessageOptions();
		when(nativeConsumer.getOptions()).thenReturn(msgOptions);
		
		createAttachedConnection();
		
		when(nativeConsumer.retrieveConnection(NativeConnection.class)).thenReturn(attConn);
		when(attConn.connect()).thenReturn(mqQueueManager);
		when(mqQueueManager.accessQueue(Matchers.anyString(), Matchers.anyInt())).thenReturn(mqQueue);
	}
	public void testOnError() throws Exception{
		errorHandler.onError(mqMsg);
		verify(mqQueue).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue).close();
		
		//Repeat with message options
		errorHandler.setOptions(new MessageOptions());
		errorHandler.getOptions().addMessageOption("MQPMO_SET_ALL_CONTEXT");
		errorHandler.getOptions().addMessageOption("MQPMO_SET_IDENTITY_CONTEXT");
		errorHandler.getOptions().addQueueOpenOption("MQOO_SET_ALL_CONTEXT");
		errorHandler.getOptions().addQueueOpenOption("MQOO_SET_IDENTITY_CONTEXT");
		errorHandler.onError(mqMsg);
		verify(mqQueue, times(2)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue, times(2)).close();
		
		//Throw error on close
		doThrow(exceptionNoQueue).when(mqQueue).close();
		try{
			errorHandler.onError(mqMsg);
			fail("An error should have been thrown");
		}
		catch(Exception e){
			assertEquals(exceptionNoQueue, e.getCause());
		}
		verify(mqQueue, times(3)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		
	}
	public void testMQProducerBasic() throws Exception{
		MQMessageProducer producer = new MQMessageProducer();
		producer.setConnection(attConn);
		NativeConnection conn = producer.getConnection();
		assertEquals(attConn, conn);
	}
	private void createAttachedConnection() {
		attConn = spy(new AttachedConnection());
		attConn.setQueueManager("your_Q_Manager");
		attConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
		attConn.setWorkersFirstOnShutdown(true);
		attConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_NULL_MD5"));
	}
}
