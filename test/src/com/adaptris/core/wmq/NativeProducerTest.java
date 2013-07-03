package com.adaptris.core.wmq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.wmq.mapping.ConfiguredField;
import com.adaptris.core.wmq.mapping.MessageIdMapper;
import com.adaptris.core.wmq.mapping.MetadataFieldMapper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.license.License;
import com.adaptris.util.text.CharsetByteTranslator;
import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class NativeProducerTest extends ProducerCase {

	private static final String DESTINATION = "Over There";
	private static final String MESSAGE_PAYLOAD = "Message Payload";

	private NativeProducer p;
	private DetachedConnection con;
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
	@Mock private AdaptrisMessage adpMsg;
	@Mock private License lic;

  private static final String BASE_DIR_KEY = "WmqNativeProducerExamples.baseDir";

  public NativeProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		p = new NativeProducer();
    LifecycleHelper.init(p);
	}
	private void setupProduce() throws Exception{
		exceptionNoMessages = new MQException(MQException.MQCC_WARNING, MQException.MQRC_NO_MSG_AVAILABLE, "Mock Test");
		exceptionNoQueue = new MQException(MQException.MQCC_FAILED, MQException.MQRC_Q_DELETED, "Mock Test");

		doNothing().when(adaptrisListener).onAdaptrisMessage((AdaptrisMessage)Matchers.anyObject());
		doNothing().when(metadataFieldMapper).copy((MQMessage)Matchers.anyObject(), (AdaptrisMessage)Matchers.anyObject());

		when(adpMsg.getStringPayload()).thenReturn(MESSAGE_PAYLOAD);

		p = spy(new NativeProducer());
		p.registerConnection(adaptrisConnection);
		p.setDestination(new ConfiguredProduceDestination("SYSTEM.DEFAULT.LOCAL.QUEUE"));

		p.addFieldMapper(metadataFieldMapper);

    LifecycleHelper.init(p);

		//Set the ConsumerDelegate as a spy
		p.registerProxy(spy(p.retrieveProxy()));

		//Create our own MQGetMessageOptions so we can return it in a stub later
		mqGetOptions = spy(new MQGetMessageOptions());
		mqGetOptions.options = p.getOptions().messageOptionsIntValue();

		//Set output for message1
//		when(mqMsg1.readUTF()).thenReturn(MESSAGE_PAYLOAD);

		//Throw a "No Message" exception for message2 to end the .consumeMessage() loop
		doThrow(exceptionNoMessages).when(mqQueue).get(mqMsg2, mqGetOptions);

		//Create stubs so we can use our mock NativeConnection, MQQueueManager, MQGetMessageOptions
		//and MQQueue
	    String queueName = p.getDestination().getDestination(adpMsg);
	    when(adaptrisConnection.retrieveConnection(NativeConnection.class)).thenReturn(nativeConnection);
		when(nativeConnection.connect()).thenReturn(mqQueueManager);
//		when(p.retrieveProxy().accessMQGetMessageOptions((MQGetMessageOptions)Matchers.anyObject())).thenReturn(mqGetOptions);
		when(mqQueueManager.accessQueue(Matchers.anyString(), Matchers.anyInt())).thenReturn(mqQueue);

		//Output our mock messages in desired order
//		when(p.retrieveProxy().accessMQMessage((MQMessage)Matchers.anyObject())).thenReturn(mqMsg1, mqMsg1, mqMsg2);
	}
	public void testNativeProducer() throws Exception{
		//These 3 do nothing
    LifecycleHelper.start(p);
    LifecycleHelper.stop(p);
    LifecycleHelper.close(p);
	}
	public void testProduce() throws Exception{
		setupProduce();

		p.produce(adpMsg);
		verify(mqQueue).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue).close();

		p.setCheckOptions(false);
		p.produce(adpMsg);
		verify(mqQueue, times(2)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue, times(2)).close();

		//Test with checkOption = true
		p.setCheckOptions(true);
		assertTrue(p.getCheckOptions());
		p.produce(adpMsg);
		verify(mqQueue, times(3)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue, times(3)).close();

		p.getOptions().addMessageOption("MQPMO_SET_ALL_CONTEXT");
		p.getOptions().addQueueOpenOption("MQOO_SET_ALL_CONTEXT");
		p.produce(adpMsg);
		verify(mqQueue, times(4)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue, times(4)).close();

		p.setOptions(new MessageOptions());
		p.getOptions().addMessageOption("MQPMO_SET_IDENTITY_CONTEXT");
		p.getOptions().addQueueOpenOption("MQOO_SET_IDENTITY_CONTEXT");
		p.produce(adpMsg);
		verify(mqQueue, times(5)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue, times(5)).close();

		//Test with empty field mappers
		p.getFieldMappers().clear();
		p.produce(adpMsg);
		verify(mqQueue, times(6)).put((MQMessage)Matchers.anyObject(), (MQPutMessageOptions)Matchers.anyObject());
		verify(mqQueue, times(6)).close();

		//Test Exception
		when(mqQueueManager.accessQueue(Matchers.anyString(), Matchers.anyInt())).thenReturn(null);
		try{
			p.produce(adpMsg);
			fail("No ProducedException thrown when MQQueue is null");
		}
		catch(ProduceException e){}


	}
	public void testLicense() throws Exception{
		when(lic.isEnabled(License.ENTERPRISE)).thenReturn(true);
		when(lic.isEnabled(License.JMS)).thenReturn(false);
		assertTrue(p.isEnabled(lic));

		when(lic.isEnabled(License.ENTERPRISE)).thenReturn(false);
		assertFalse(p.isEnabled(lic));

		when(lic.isEnabled(License.JMS)).thenReturn(true);
		assertTrue(p.isEnabled(lic));
	}

	@Override
	protected Object retrieveObjectForSampleConfig() {
		createProducer();

		StandaloneProducer result = new StandaloneProducer();
		result.setConnection(con);
		result.setProducer(p);

		return result;
	}
	private void createProducer(){
		con = new DetachedConnection();
		con.setQueueManager("your_Q_Manager");
		con.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
		con.setWorkersFirstOnShutdown(true);
		con.getEnvironmentProperties().addKeyValuePair(
				new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_NULL_MD5"));
		p.setDestination(new ConfiguredProduceDestination("SYSTEM.DEFAULT.LOCAL.QUEUE"));
		p.getOptions().setQueueOpenOptions("MQOO_INPUT_AS_Q_DEF,MQOO_OUTPUT,MQOO_SET_ALL_CONTEXT");
		p.getOptions().setMessageOptions("MQPMO_NO_SYNCPOINT,MQPMO_SET_ALL_CONTEXT");
		MessageIdMapper mm = new MessageIdMapper();
    mm.setByteTranslator(new CharsetByteTranslator("UTF-8"));
		p.addFieldMapper(mm);
		MetadataFieldMapper m = new MetadataFieldMapper();
		m.setMetadataKey("application_Id_Data_MetadataKey");
		m.setMqFieldName("applicationIdData");
		p.addFieldMapper(m);
		ConfiguredField cf = new ConfiguredField();
		cf.setMqFieldName("putApplicationName");
		cf.setConfiguredValue("Adaptris Mediation Framework");
		p.addFieldMapper(cf);
	}
}
