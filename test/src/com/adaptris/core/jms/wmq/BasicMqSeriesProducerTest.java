package com.adaptris.core.jms.wmq;

import static com.adaptris.core.jms.wmq.AdvancedMqSeriesProducerTest.ACTIVATE_EXCEPTION_LISTENER_COMMENT;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsProducerExample;
import com.adaptris.core.jms.PtpProducer;

public class BasicMqSeriesProducerTest extends JmsProducerExample {

  protected static final String THIS_IS_JUST_AN_EXAMPLE_COMMENT = "\n<!--"
      + "\nNote that the values configured for BasicMqSeriesImplementation" + "\nare just example values. They may not"
      + "\nbe the correct ones for your environment; you should look at "
      + "\nyour WebsphereMQ configuration to see what values you should" + "\nuse." + "\n-->\n";

  public BasicMqSeriesProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-BasicWebsphereMQ";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    PtpProducer producer = new PtpProducer(new ConfiguredProduceDestination("MQ_QUEUE"));
    StandaloneProducer result = new StandaloneProducer(configure(new JmsConnection()), producer);
    return result;
  }

  protected static JmsConnection configure(JmsConnection c) {
    c.setUserName("BrokerUsername");
    c.setPassword("BrokerPassword");
    BasicMqSeriesImplementation mq = new BasicMqSeriesImplementation();
    mq.setCcsid(819);
    mq.setQueueManager("MyQueueManager");
    mq.setChannel("MyChannel");
    mq.setBrokerHost("localhost");
    mq.setBrokerPort(1414);
    c.setVendorImplementation(mq);
    c.setWorkersFirstOnShutdown(true);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return c;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + ACTIVATE_EXCEPTION_LISTENER_COMMENT + THIS_IS_JUST_AN_EXAMPLE_COMMENT;
  }
}
