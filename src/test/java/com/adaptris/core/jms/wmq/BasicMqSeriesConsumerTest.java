/*
 * $RCSfile: BasicMqSeriesConsumerTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/07/21 11:59:59 $
 * $Author: lchan $
 */
package com.adaptris.core.jms.wmq;

import static com.adaptris.core.jms.wmq.AdvancedMqSeriesProducerTest.ACTIVATE_EXCEPTION_LISTENER_COMMENT;
import static com.adaptris.core.jms.wmq.BasicMqSeriesProducerTest.THIS_IS_JUST_AN_EXAMPLE_COMMENT;
import static com.adaptris.core.jms.wmq.BasicMqSeriesProducerTest.configure;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConsumerCase;
import com.adaptris.core.jms.PtpConsumer;

public class BasicMqSeriesConsumerTest extends JmsConsumerCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-BasicWebsphereMQ";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    PtpConsumer consumer = new PtpConsumer().withQueue("SOME_MQ_QUEUE");
    StandaloneConsumer result = new StandaloneConsumer(configure(new JmsConnection()), consumer);
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + ACTIVATE_EXCEPTION_LISTENER_COMMENT + THIS_IS_JUST_AN_EXAMPLE_COMMENT;
  }
}
