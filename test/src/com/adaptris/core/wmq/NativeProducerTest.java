package com.adaptris.core.wmq;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.wmq.mapping.ConfiguredField;
import com.adaptris.core.wmq.mapping.MessageIdMapper;
import com.adaptris.core.wmq.mapping.MetadataFieldMapper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.text.CharsetByteTranslator;
import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class NativeProducerTest extends ProducerCase {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery() {{
    setImposteriser(ClassImposteriser.INSTANCE);
    setThreadingPolicy(new Synchroniser());
  }};

  private static final String MESSAGE_PAYLOAD = "Message Payload";

  private NativeProducer p;
  private DetachedConnection con;
  private MQException exceptionNoMessages;
  
  private MQGetMessageOptions mqGetOptions = context.mock(MQGetMessageOptions.class);
  private MQQueueManager mqQueueManager = context.mock(MQQueueManager.class);
  private MQQueue mqQueue = context.mock(MQQueue.class);
  private MQMessage mqMsg2 = context.mock(MQMessage.class, "mqMsg2");
  private NativeConnection nativeConnection = context.mock(NativeConnection.class);
  private AdaptrisConnection adaptrisConnection = context.mock(AdaptrisConnection.class);
  private AdaptrisMessageListener adaptrisListener = context.mock(AdaptrisMessageListener.class);
  private MetadataFieldMapper metadataFieldMapper = context.mock(MetadataFieldMapper.class);
  private AdaptrisMessage adpMsg = context.mock(AdaptrisMessage.class);
  private License lic = context.mock(License.class);

  private static final String BASE_DIR_KEY = "WmqNativeProducerExamples.baseDir";

  public NativeProducerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected void setUp() throws Exception {
    p = new NativeProducer();
    LifecycleHelper.init(p);
  }

  private void setupProduce() throws Exception {
    exceptionNoMessages = new MQException(MQException.MQCC_WARNING, MQException.MQRC_NO_MSG_AVAILABLE, "Mock Test");

    context.checking(new Expectations() {{
      allowing(adaptrisListener).onAdaptrisMessage(with(any(AdaptrisMessage.class)));
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
      allowing(adpMsg).getStringPayload();
            will(returnValue(MESSAGE_PAYLOAD));
    }});

    p = new NativeProducer();
    p.registerConnection(adaptrisConnection);
    p.setDestination(new ConfiguredProduceDestination("SYSTEM.DEFAULT.LOCAL.QUEUE"));

    p.addFieldMapper(metadataFieldMapper);

    LifecycleHelper.init(p);

    // Set the ConsumerDelegate as a spy
    ProducerDelegate delegate = new ProducerDelegate(p, LoggerFactory.getLogger(super.getClass().getName()));
    p.registerProxy(delegate);

    // Create our own MQGetMessageOptions so we can return it in a stub later
    mqGetOptions.options = p.getOptions().messageOptionsIntValue();

    // Set output for message1
    // when(mqMsg1.readUTF()).thenReturn(MESSAGE_PAYLOAD);

    context.checking(new Expectations() {{
   // Throw a "No Message" exception for message2 to end the .consumeMessage()
      allowing(mqQueue).get(mqMsg2, mqGetOptions);
            will(throwException(exceptionNoMessages));
      allowing(adaptrisConnection).retrieveConnection(NativeConnection.class);
            will(returnValue(nativeConnection));
      allowing(nativeConnection).disconnect(with(any(MQQueueManager.class)));
      allowing(nativeConnection).connect();
            will(returnValue(mqQueueManager));
      allowing(mqQueueManager).accessQueue(with(any(String.class)), with(any(int.class)));
            will(returnValue(mqQueue));
    }});
    // when(p.retrieveProxy().accessMQGetMessageOptions((MQGetMessageOptions)Matchers.anyObject())).thenReturn(mqGetOptions);

    // Output our mock messages in desired order
    // when(p.retrieveProxy().accessMQMessage((MQMessage)Matchers.anyObject())).thenReturn(mqMsg1,
    // mqMsg1, mqMsg2);
  }

  public void testNativeProducer() throws Exception {
    // These 3 do nothing
    LifecycleHelper.start(p);
    LifecycleHelper.stop(p);
    LifecycleHelper.close(p);
  }

  public void testProduce() throws Exception {
    setupProduce();

    context.checking(new Expectations() {{
      oneOf(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
      oneOf(mqQueue).close();
    }});
    p.produce(adpMsg);
    
    context.assertIsSatisfied();
  }
  
  public void testProduceNoOptions() throws Exception {
    setupProduce();

    context.checking(new Expectations() {{
      oneOf(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
      oneOf(mqQueue).close();
    }});
    
    p.setCheckOptions(false);
    p.produce(adpMsg);
    
    context.assertIsSatisfied();
  }
  
  public void testProduceWithOptions() throws Exception {
    setupProduce();

    context.checking(new Expectations() {{
      oneOf(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
      oneOf(mqQueue).close();
    }});
    
    p.setCheckOptions(true);
    p.produce(adpMsg);
    
    context.assertIsSatisfied();
  }
  
  public void testProduceWithConfiguredOptions() throws Exception {
    setupProduce();

    context.checking(new Expectations() {{
      oneOf(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
      oneOf(mqQueue).close();
    }});
    
    p.setCheckOptions(true);
    p.getOptions().addMessageOption("MQPMO_SET_ALL_CONTEXT");
    p.getOptions().addQueueOpenOption("MQOO_SET_ALL_CONTEXT");
    p.produce(adpMsg);
    
    context.assertIsSatisfied();
  }
  
  public void testProduceWithConfiguredNewMessageOptions() throws Exception {
    setupProduce();

    context.checking(new Expectations() {{
      oneOf(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
      oneOf(mqQueue).close();
    }});
    
    p.setCheckOptions(true);
    p.setOptions(new MessageOptions());
    p.getOptions().addMessageOption("MQPMO_SET_ALL_CONTEXT");
    p.getOptions().addQueueOpenOption("MQOO_SET_ALL_CONTEXT");
    p.produce(adpMsg);
    
    context.assertIsSatisfied();
  }
  
  public void testProduceWithEmptyFieldMapper() throws Exception {
    setupProduce();

    context.checking(new Expectations() {{
      oneOf(mqQueue).put(with(any(MQMessage.class)), with(any(MQPutMessageOptions.class)));
      oneOf(mqQueue).close();
    }});
    
    p.getFieldMappers().clear();
    p.produce(adpMsg);
    
    context.assertIsSatisfied();
  }

  public void testLicense() throws Exception {
    context.checking(new Expectations() {{
        oneOf(lic).isEnabled(LicenseType.Enterprise);
        will(returnValue(true));
    }});
    
    assertTrue(p.isEnabled(lic));
    
    context.checking(new Expectations() {{
        oneOf(lic).isEnabled(LicenseType.Enterprise);
        will(returnValue(false));
    }});
    
    assertFalse(p.isEnabled(lic));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    createProducer();

    StandaloneProducer result = new StandaloneProducer();
    result.setConnection(con);
    result.setProducer(p);

    return result;
  }

  private void createProducer() {
    con = new DetachedConnection();
    con.setQueueManager("your_Q_Manager");
    con.getEnvironmentProperties().addKeyValuePair(
        new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
    con.setWorkersFirstOnShutdown(true);
    con.getEnvironmentProperties()
        .addKeyValuePair(
            new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY,
                "SSL_RSA_WITH_NULL_MD5"));
    p.setDestination(new ConfiguredProduceDestination(
        "SYSTEM.DEFAULT.LOCAL.QUEUE"));
    p.getOptions().setQueueOpenOptions(
        "MQOO_INPUT_AS_Q_DEF,MQOO_OUTPUT,MQOO_SET_ALL_CONTEXT");
    p.getOptions()
        .setMessageOptions("MQPMO_NO_SYNCPOINT,MQPMO_SET_ALL_CONTEXT");
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
