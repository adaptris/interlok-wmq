package com.adaptris.core.wmq;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneConsumer;

public class NativeConsumerWithErrorHandlerTest extends NativeConsumerTest {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected NativeErrorHandler createErrorHandler() {
    ForwardingNativeConsumerErrorHandler fnceh = new ForwardingNativeConsumerErrorHandler();
    fnceh.setDestination(new ConfiguredProduceDestination("The_Error_Queue"));
    fnceh.setOptions(new MessageOptions());
    return fnceh;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-"
        + ((NativeConsumer) ((StandaloneConsumer) object).getConsumer()).getErrorHandler().getClass().getSimpleName();
  }
}
