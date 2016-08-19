/*
 * $RCSfile: AdvancedMqSeriesProducerTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/02/13 16:38:41 $
 * $Author: lchan $
 */
package com.adaptris.core.jms.wmq;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsProducerExample;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.wmq.AdvancedMqSeriesImplementation.ConnectionFactoryProperty;
import com.adaptris.core.jms.wmq.AdvancedMqSeriesImplementation.SessionProperty;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class AdvancedMqSeriesProducerTest extends JmsProducerExample {

  protected static final String ACTIVATE_EXCEPTION_LISTENER_COMMENT = "\n<!--"
      + "\nIn version 6.0 of WebSphere MQ and previous you may have to install  "
      + "\nAPAR IY81774 which introduces a system property activateExceptionListener"
      + "\nthat should be set when starting the adapter."
      + "\n\nIf this property is set, all exceptions resulting from a broken connection"
      + "\nare sent to the exception listener, regardless of the context in which they"
      + "\noccur. If this is not set, then broken connections may not trigger the"
      + "\nstandard javax.jms.ExceptionListener interface which means the adapter"
      + "\nis not notified of a broken connection to WebsphereMQ and subsequently"
      + "\ncannot recover from a broken connection to WebsphereMQ" + "\n-->\n";

  protected static final String THIS_IS_JUST_AN_EXAMPLE_COMMENT = "\n<!--"
      + "\nNote that the values configured for AdvancedMqSeriesImplementation"
      + "\nare just example values, they are simply a random selection of"
      + "\nproperties with random values. They almost certainly will not"
      + "\nbe the correct ones for your environment; you should look at "
      + "\nyour WebsphereMQ configuration to see what values you should" + "\nuse." + "\n-->\n";

  public AdvancedMqSeriesProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-AdvancedWebsphereMQ";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination("SOME_MQ_QUEUE_NAME"));
    StandaloneProducer result = new StandaloneProducer(configure(new JmsConnection()), producer);

    return result;
  }

  protected static JmsConnection configure(JmsConnection c) {
    c.setUserName("BrokerUsername");
    c.setPassword("BrokerPassword");
    c.setVendorImplementation(createVendorImpl());
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return c;
  }

  protected static AdvancedMqSeriesImplementation createVendorImpl() {
    AdvancedMqSeriesImplementation mq = new AdvancedMqSeriesImplementation();
    KeyValuePairSet cp = mq.getConnectionFactoryProperties();
    cp.add(new KeyValuePair(ConnectionFactoryProperty.SecurityExitInit.name(), "MySecurityExitInit"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.SecurityExit.name(), "MySecurityExit"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.SSLCipherSuite.name(), "SSL_RSA_WITH_NULL_MD5"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.CCSID.name(), "819"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.QueueManager.name(), "MyQueueManager"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.Channel.name(), "MyChannel"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.TransportType.name(), TransportTypeHelper.Transport.MQJMS_TP_CLIENT_MQ_TCPIP
        .name()));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.HostName.name(), "localhost"));
    cp.add(new KeyValuePair(ConnectionFactoryProperty.Port.name(), "1414"));
    mq.getSessionProperties().addKeyValuePair(new KeyValuePair(SessionProperty.OptimisticPublication.name(), "true"));
    mq.setBrokerUrl("localhost");
    return mq;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + ACTIVATE_EXCEPTION_LISTENER_COMMENT + THIS_IS_JUST_AN_EXAMPLE_COMMENT;
  }
}
