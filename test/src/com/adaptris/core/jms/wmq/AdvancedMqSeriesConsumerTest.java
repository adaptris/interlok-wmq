package com.adaptris.core.jms.wmq;

import static com.adaptris.core.jms.wmq.AdvancedMqSeriesProducerTest.ACTIVATE_EXCEPTION_LISTENER_COMMENT;
import static com.adaptris.core.jms.wmq.AdvancedMqSeriesProducerTest.THIS_IS_JUST_AN_EXAMPLE_COMMENT;
import static com.adaptris.core.jms.wmq.AdvancedMqSeriesProducerTest.configure;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConsumerCase;
import com.adaptris.core.jms.PtpConsumer;

public class AdvancedMqSeriesConsumerTest extends JmsConsumerCase {


  public AdvancedMqSeriesConsumerTest(String name) {
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
    PtpConsumer consumer = new PtpConsumer(new ConfiguredConsumeDestination("SOME_MQ_QUEUE"));
    StandaloneConsumer result = new StandaloneConsumer(configure(new JmsConnection()), consumer);
    return result;

  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + ACTIVATE_EXCEPTION_LISTENER_COMMENT + THIS_IS_JUST_AN_EXAMPLE_COMMENT;
  }

}
