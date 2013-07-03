package com.adaptris.core.wmq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.wmq.mapping.FieldMapper;
import com.adaptris.core.wmq.mapping.MessageIdMapper;
import com.adaptris.core.wmq.mapping.MetadataFieldMapper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.license.License;
import com.adaptris.util.text.Base64ByteTranslator;
import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class NativeConsumerTest extends ConsumerCase {

	private static final String SYSTEM_DEFAULT_LOCAL_QUEUE = "SYSTEM.DEFAULT.LOCAL.QUEUE";
	private static final String DEFAULT_Q_OPT = "MQOO_INPUT_AS_Q_DEF,MQC.MQOO_OUTPUT,MQC.MQOO_INQUIRE";
	private static final String MESSAGE_PAYLOAD = "Message Payload";
  private static final String BASE_DIR_KEY = "WmqNativeConsumerExamples.baseDir";

	private NativeConsumer consumer;
	private MQException exceptionNoMessages;
	private MQException exceptionNoQueue;
	private MQGetMessageOptions mqGetOptions;
	@Mock private MQQueueManager mqQueueManager;
	@Mock private MQQueue mqQueue;
	@Mock private MQMessage mqMsg1;
	@Mock private MQMessage mqMsg2;
	@Mock private NativeConnection nativeConnection;
	@Mock private AdaptrisConnection adaptrisConnection;
	@Mock private AdaptrisMessageListener adaptrisListener;
	@Mock private MetadataFieldMapper metadataFieldMapper;
	@Mock private ForwardingNativeConsumerErrorHandler errorHandler;
	@Mock License lic;

	public NativeConsumerTest(String name) {
		super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
	}

	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		exceptionNoMessages = new MQException(MQException.MQCC_WARNING, MQException.MQRC_NO_MSG_AVAILABLE, "Mock Test");
		exceptionNoQueue = new MQException(MQException.MQCC_FAILED, MQException.MQRC_Q_DELETED, "Mock Test");

		doNothing().when(adaptrisListener).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());
		doNothing().when(metadataFieldMapper).copy((MQMessage)Matchers.anyObject(), (AdaptrisMessage)Matchers.anyObject());

		consumer = spy(new NativeConsumer());
		consumer.registerConnection(adaptrisConnection);
		consumer.registerAdaptrisMessageListener(adaptrisListener);
		consumer.setDestination(new ConfiguredConsumeDestination(SYSTEM_DEFAULT_LOCAL_QUEUE));

		doNothing().when(errorHandler).onError(mqMsg1);
		consumer.setErrorHandler(errorHandler);

		ArrayList<FieldMapper> preGetFieldMappers = new ArrayList<FieldMapper>();
		preGetFieldMappers.add(metadataFieldMapper);
		consumer.setPreGetFieldMappers(preGetFieldMappers);
		consumer.addFieldMapper(metadataFieldMapper);

		consumer.init();

		//Set the ConsumerDelegate as a spy
		consumer.registerProxy(spy(consumer.retrieveProxy()));

		consumer.retrieveProxy().setErrorHandler(errorHandler);

		//Create our own MQGetMessageOptions so we can return it in a stub later
		mqGetOptions = spy(new MQGetMessageOptions());
		mqGetOptions.options = consumer.getOptions().messageOptionsIntValue();

		//Set output for message1
		when(mqMsg1.readUTF()).thenReturn(MESSAGE_PAYLOAD);

		//Throw a "No Message" exception for message2 to end the .consumeMessage() loop
		doThrow(exceptionNoMessages).when(mqQueue).get(mqMsg2, mqGetOptions);

		//Create stubs so we can use our mock NativeConnection, MQQueueManager, MQGetMessageOptions
		//and MQQueue
	    String queueName = consumer.getDestination().getDestination();
	    when(adaptrisConnection.retrieveConnection(NativeConnection.class)).thenReturn(nativeConnection);
		when(nativeConnection.connect()).thenReturn(mqQueueManager);
		when(consumer.retrieveProxy().accessMQGetMessageOptions((MQGetMessageOptions)Matchers.anyObject())).thenReturn(mqGetOptions);
		when(mqQueueManager.accessQueue(queueName, consumer.getOptions().queueOpenOptionsIntValue())).thenReturn(mqQueue);

		//Output our mock messages in desired order
		when(consumer.retrieveProxy().accessMQMessage((MQMessage)Matchers.anyObject())).thenReturn(mqMsg1, mqMsg1, mqMsg2);

	}

	public void testProcessMessages() throws Exception{
		//Main Test
		int count = consumer.processMessages();

		//Check Results
		assertEquals(2, count);
		verify(adaptrisListener, times(2)).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());
	}
	public void testLicense() throws Exception{
		when(lic.isEnabled(License.ENTERPRISE)).thenReturn(true);
		when(lic.isEnabled(License.JMS)).thenReturn(false);
		assertTrue(consumer.isEnabled(lic));

		when(lic.isEnabled(License.ENTERPRISE)).thenReturn(false);
		assertFalse(consumer.isEnabled(lic));

		when(lic.isEnabled(License.JMS)).thenReturn(true);
		assertTrue(consumer.isEnabled(lic));
	}
	public void testProcessMessagesReacquireLock() throws Exception{
		//TODO This test is incomplete because can't mock final method
		//When done, result should be 1 only
		//when(consumer.reacquireLock()).thenReturn(false);
		consumer.setReacquireLockBetweenMessages(true);

		//Main Test
		int count = consumer.processMessages();

		//Check Results, only 1 processed although 2 could be received
		//assertEquals(1, count);
		assertEquals(2, count);
		verify(adaptrisListener, times(2)).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());
	}

	public void testExceptions() throws Exception{
		int count;

		MessageOptions msgOptionsSpy = spy(consumer.getOptions());
		consumer.setOptions(msgOptionsSpy);

		//Throw an MQ exception for message1
		doThrow(exceptionNoQueue).when(metadataFieldMapper).copy((MQMessage)Matchers.anyObject(), (AdaptrisMessage)Matchers.anyObject());
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw an IO exception for message1
		doThrow(new IOException("Mock Test")).when(metadataFieldMapper).copy((MQMessage)Matchers.anyObject(), (AdaptrisMessage)Matchers.anyObject());
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw MQError when trying to close MQQueueManager
		doThrow(exceptionNoQueue).when(nativeConnection).disconnect(mqQueueManager);
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw MQError when trying to close MQQueue
		doThrow(exceptionNoQueue).when(mqQueue).close();
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw MQError when trying to access Queue
	    String queueName = consumer.getDestination().getDestination();
		when(mqQueueManager.accessQueue(queueName, consumer.getOptions().queueOpenOptionsIntValue())).thenThrow(exceptionNoQueue);
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw CoreException when trying to access Queue
		when(msgOptionsSpy.queueOpenOptionsIntValue()).thenThrow(new CoreException("Mock test"));
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw an error that should be caught within .init()
		doThrow(new CoreException("mock test")).when(msgOptionsSpy).addMessageOption(Matchers.anyString());
		consumer.init();
	}
	public void testExceptions2() throws Exception{

		int count;

		//Set for alternative error handling fork from now on
		consumer.setLogAllExceptions(true);

		//Remove error handler
		consumer.setErrorHandler(null);
		consumer.retrieveProxy().setErrorHandler(null);

		//Throw IO exception for message1
		doThrow(new IOException("Mock Test")).when(metadataFieldMapper).copy((MQMessage)Matchers.anyObject(), (AdaptrisMessage)Matchers.anyObject());
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());

		//Throw a Core exception for message1
		doThrow(new CoreException("Mock Test")).when(metadataFieldMapper).copy((MQMessage)Matchers.anyObject(), (AdaptrisMessage)Matchers.anyObject());
		count = consumer.processMessages();
		assertEquals(0, count);
		verify(adaptrisListener, never()).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());
	}
	public void testInitWithoutErrorHandler() throws Exception{
		//Note that processing without the error handler is called in testExceptions2
		consumer.setErrorHandler(null);
		consumer.init();
	}
	@Override
	protected Object retrieveObjectForSampleConfig() {
		NativeConsumer consumer = new NativeConsumer();
		consumer.setDestination(new ConfiguredConsumeDestination(SYSTEM_DEFAULT_LOCAL_QUEUE));
		consumer.getOptions().setQueueOpenOptions(DEFAULT_Q_OPT);
		List<FieldMapper> list = createFieldMappers();
		for (FieldMapper m : list) {
			consumer.addFieldMapper(m);
		}
		consumer.setErrorHandler(createErrorHandler());
		StandaloneConsumer result = new StandaloneConsumer(createConnection(), consumer);
		return result;
	}

	protected NativeErrorHandler createErrorHandler() {
		return null;
	}

	protected NativeConnection createConnection() {
		DetachedConnection con = new DetachedConnection();
		con.setQueueManager("your_Q_Manager");
		con.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
		con.setWorkersFirstOnShutdown(true);
		con.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_NULL_MD5"));
		return con;
	}

	protected List<FieldMapper> createFieldMappers() {
		List<FieldMapper> result = new ArrayList<FieldMapper>();
		MessageIdMapper mm = new MessageIdMapper();
		mm.setByteTranslator(new Base64ByteTranslator());
		result.add(mm);
		MetadataFieldMapper m = new MetadataFieldMapper();
		m.setMetadataKey("application_Id_Data_MetadataKey");
		m.setMqFieldName("applicationIdData");
		result.add(m);
		return result;
	}
}
