package com.adaptris.core.wmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.wmq.mapping.FieldMapper;
import com.adaptris.core.wmq.mapping.MessageIdMapper;
import com.adaptris.core.wmq.mapping.MetadataFieldMapper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
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

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
      setThreadingPolicy(new Synchroniser());
    }
  };
  
  private States test = context.states("test");
  
  private NativeConsumer consumer;
  private MQException exceptionNoMessages;
  private MQException exceptionNoQueue;
  private MQGetMessageOptions mqGetOptions = context.mock(MQGetMessageOptions.class);

  private MQQueueManager mqQueueManager = context.mock(MQQueueManager.class);
  private MQQueue mqQueue = context.mock(MQQueue.class);
  private MQMessage mqMsg1 = context.mock(MQMessage.class, "mqMessage1");
  private MQMessage mqMsg2 = context.mock(MQMessage.class, "mqMessage2");
  private NativeConnection nativeConnection = context.mock(NativeConnection.class);
  private AdaptrisConnection adaptrisConnection = context.mock(AdaptrisConnection.class);
  private AdaptrisMessageListener adaptrisListener = context.mock(AdaptrisMessageListener.class);
  private MetadataFieldMapper metadataFieldMapper = context.mock(MetadataFieldMapper.class);
  private ForwardingNativeConsumerErrorHandler errorHandler = context.mock(ForwardingNativeConsumerErrorHandler.class);
  private License lic = context.mock(License.class);
  private MQMessageAccessor messageAccessor = context.mock(MQMessageAccessor.class);
  private MQMessageOptionsAccessor messageOptionsAccessor = context.mock(MQMessageOptionsAccessor.class);

  public NativeConsumerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected void setUp() throws Exception {
    exceptionNoMessages = new MQException(MQException.MQCC_WARNING, MQException.MQRC_NO_MSG_AVAILABLE, "Mock Test");
    exceptionNoQueue = new MQException(MQException.MQCC_FAILED, MQException.MQRC_Q_DELETED, "Mock Test");

    consumer = new NativeConsumer();
    consumer.registerConnection(adaptrisConnection);
    consumer.registerAdaptrisMessageListener(adaptrisListener);
    consumer.setDestination(new ConfiguredConsumeDestination(SYSTEM_DEFAULT_LOCAL_QUEUE));

    consumer.setErrorHandler(errorHandler);

    ArrayList<FieldMapper> preGetFieldMappers = new ArrayList<FieldMapper>();
    preGetFieldMappers.add(metadataFieldMapper);
    consumer.setPreGetFieldMappers(preGetFieldMappers);
    consumer.addFieldMapper(metadataFieldMapper);

    consumer.init();

    context.checking(new Expectations() {{
      allowing(metadataFieldMapper); when(test.is("allowing-all-metadata"));
      allowing(errorHandler).onError(mqMsg1);
      allowing(mqMsg1).readUTF();     
          will(returnValue(MESSAGE_PAYLOAD));   
      // Throw a "No Message" exception for message2 to end the .consumeMessage()
      allowing(mqQueue).get(mqMsg2, mqGetOptions);       
          will(throwException(exceptionNoMessages));
      allowing(mqQueue).get(mqMsg1, mqGetOptions);
      allowing(mqQueue).close();
      allowing(adaptrisConnection).retrieveConnection(NativeConnection.class);    
          will(returnValue(nativeConnection));
      allowing(nativeConnection).connect();       
          will(returnValue(mqQueueManager));
      allowing(nativeConnection).disconnect(mqQueueManager);
      allowing(messageOptionsAccessor).accessMessageOptions(with(any(MQGetMessageOptions.class)));
            will(returnValue(mqGetOptions));
      allowing(mqQueueManager).accessQueue(consumer.getDestination().getDestination(), consumer.getOptions().queueOpenOptionsIntValue());      
          will(returnValue(mqQueue));
      allowing(errorHandler);
   // Output our mock messages in desired order
      allowing(messageAccessor).accessMessage(with(any(MQMessage.class))); 
          will(onConsecutiveCalls(returnValue(mqMsg1), returnValue(mqMsg1), returnValue(mqMsg2)));
      }
    });
    
 // Set the ConsumerDelegate as a spy
    consumer.registerProxy(new ConsumerDelegate(consumer, LoggerFactory.getLogger(super.getClass().getName())));

    consumer.retrieveProxy().setErrorHandler(errorHandler);
    consumer.retrieveProxy().setMessageAccessor(messageAccessor);
    consumer.retrieveProxy().setMessageOptionsAccessor(messageOptionsAccessor);

    // Create our own MQGetMessageOptions so we can return it in a stub later
    mqGetOptions.options = consumer.getOptions().messageOptionsIntValue();
  }

  public void testProcessMessages() throws Exception {
    test.become("allowing-all-metadata");
    context.checking(new Expectations() {{
      exactly(2).of(adaptrisListener).onAdaptrisMessage(with(any(AdaptrisMessage.class)));
    }});
    
    // Main Test
    consumer.processMessages();
    
    context.assertIsSatisfied();
  }

  public void testLicense() throws Exception {
    context.checking(new Expectations() {{
        oneOf(lic).isEnabled(LicenseType.Enterprise);
        will(returnValue(true));
    }});
    
    assertTrue(consumer.isEnabled(lic));
    
    context.checking(new Expectations() {{
        oneOf(lic).isEnabled(LicenseType.Enterprise);
        will(returnValue(false));
    }});
    
    assertFalse(consumer.isEnabled(lic));
    
  }

  public void testProcessMessagesReacquireLock() throws Exception {
    test.become("allowing-all-metadata");
    
    context.checking(new Expectations() {{
      exactly(2).of(adaptrisListener).onAdaptrisMessage(with(any(AdaptrisMessage.class)));
    }});
    // TODO This test is incomplete because can't mock final method
    // When done, result should be 1 only
    // when(consumer.reacquireLock()).thenReturn(false);
    consumer.setReacquireLockBetweenMessages(true);

    // Main Test
    int count = consumer.processMessages();

    // Check Results, only 1 processed although 2 could be received
    // assertEquals(1, count);
    assertEquals(2, count);

    context.assertIsSatisfied();
  }

  public void testMetedataFieldMapperException() throws Exception {
    test.become("exception-all-metadata");
 // Throw an MQ exception for message1
    context.checking(new Expectations() {{
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
            will(throwException(exceptionNoQueue));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }
  
  public void testMetedataFieldMapperIOException() throws Exception {
    test.become("exception-all-metadata");
 // Throw an MQ exception for message1
    context.checking(new Expectations() {{
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
            will(throwException(new IOException("Mock Test")));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }
  
  public void testExceptionOnDisconnect() throws Exception {
    test.become("exception-all-metadata");
    
    context.checking(new Expectations() {{
      never(adaptrisListener).onAdaptrisMessage(with(any(AdaptrisMessage.class)));
      allowing(nativeConnection).disconnect(mqQueueManager); 
            will(throwException(exceptionNoQueue));
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
            will(throwException(new IOException("Mock Test")));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }
  
  public void testExceptionOnClose() throws Exception {
    test.become("exception-all-metadata");
    
    context.checking(new Expectations() {{
      never(adaptrisListener).onAdaptrisMessage(with(any(AdaptrisMessage.class)));
      allowing(mqQueue).close(); 
            will(throwException(exceptionNoQueue));
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
            will(throwException(new IOException("Mock Test")));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }
  
  public void testExceptionOnQueueAccess() throws Exception {
    test.become("allowing-all-metadata");
    
    consumer.setDestination(new ConfiguredConsumeDestination("someDest"));
    
    context.checking(new Expectations() {{
      exactly(1).of(mqQueueManager).accessQueue("someDest", consumer.getOptions().queueOpenOptionsIntValue()); 
            will(throwException(exceptionNoQueue));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }

  public void testIOExceptionWithoutHandler() throws Exception {
    test.become("exception-all-metadata");
    
    consumer.setLogAllExceptions(true);
    consumer.setErrorHandler(null);
    consumer.retrieveProxy().setErrorHandler(null);
    
    context.checking(new Expectations() {{
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
            will(throwException(exceptionNoQueue));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }
  
  public void testMetedataFieldMapperIOExceptionNoHandler() throws Exception {
    test.become("exception-all-metadata");
    
    consumer.setLogAllExceptions(true);
    consumer.setErrorHandler(null);
    consumer.retrieveProxy().setErrorHandler(null);
    
 // Throw an MQ exception for message1
    context.checking(new Expectations() {{
      allowing(metadataFieldMapper).copy(with(any(MQMessage.class)), with(any(AdaptrisMessage.class)));
            will(throwException(new IOException("Mock Test")));
      allowing(metadataFieldMapper).copy(with(any(AdaptrisMessage.class)), with(any(MQMessage.class)));
    }});
    
    int count = consumer.processMessages();
    assertEquals(0, count);
    
    context.assertIsSatisfied();
  }

  public void testInitWithoutErrorHandler() throws Exception {
    // Note that processing without the error handler is called in
    // testExceptions2
    consumer.setErrorHandler(null);
    consumer.init();
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    NativeConsumer consumer = new NativeConsumer();
    consumer.setDestination(new ConfiguredConsumeDestination(
        SYSTEM_DEFAULT_LOCAL_QUEUE));
    consumer.getOptions().setQueueOpenOptions(DEFAULT_Q_OPT);
    List<FieldMapper> list = createFieldMappers();
    for (FieldMapper m : list) {
      consumer.addFieldMapper(m);
    }
    consumer.setErrorHandler(createErrorHandler());
    StandaloneConsumer result = new StandaloneConsumer(createConnection(),
        consumer);
    return result;
  }

  protected NativeErrorHandler createErrorHandler() {
    return null;
  }

  protected NativeConnection createConnection() {
    DetachedConnection con = new DetachedConnection();
    con.setQueueManager("your_Q_Manager");
    con.getEnvironmentProperties().addKeyValuePair(
        new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
    con.setWorkersFirstOnShutdown(true);
    con.getEnvironmentProperties()
        .addKeyValuePair(
            new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY,
                "SSL_RSA_WITH_NULL_MD5"));
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
