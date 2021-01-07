package com.adaptris.core.wmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ibm.mq.MQC;

public class MessageOptionsTest {

  /**
   * Many thing have already been tested in other tests
   *
   * These are additional tests to improve code coverage
   */

  private static final String NON_EXISTENT_INT = "-5352";
  private static final String NON_EXISTENT_STR = "Arbitrary String";

  @Test
  public void testOptions() throws Exception{
    MessageOptions options = new MessageOptions();

    //OPTIONS AS NUMERICS TESTS
    options.setQueueCloseOptions("" + MQC.MQCO_DELETE_PURGE);

    options.addQueueCloseOption("MQCO_DELETE");
    assertTrue((options.queueCloseOptionsIntValue() & MQC.MQCO_DELETE) == 1);
    assertTrue((options.queueCloseOptionsIntValue() & MQC.MQCO_DELETE_PURGE) == 2);

    //Repeat addition of option
    int v = options.queueCloseOptionsIntValue();
    options.addQueueCloseOption("MQCO_DELETE");
    assertEquals(v, options.queueCloseOptionsIntValue());

    //Try non-existent value
    try{
      options.addQueueCloseOption(NON_EXISTENT_INT);
      fail("Core Exception should have been thrown");
    } catch (Exception e) {
    }
    assertTrue((options.queueCloseOptionsIntValue() & MQC.MQCO_DELETE) == 1);

    v = options.queueCloseOptionsIntValue();
    options.addQueueOpenOption(NON_EXISTENT_STR);
    assertEquals(v, options.queueCloseOptionsIntValue());
    //assertTrue(options.getQueueCloseOptions().contains("MQCO_DELETE"));

    //OPTIONS AS TEXT TESTS
    options.setQueueCloseOptions("MQCO_DELETE");
    assertTrue((options.queueCloseOptionsIntValue() & MQC.MQCO_DELETE) == 1);

    //Repeat addition of option, value should remain unchanged
    v = options.queueCloseOptionsIntValue();
    options.addQueueCloseOption("MQCO_DELETE");
    assertEquals(v, options.queueCloseOptionsIntValue());

    options.setQueueCloseOptions(NON_EXISTENT_STR);
    try{
      options.queueCloseOptionsIntValue();
      fail("Core Exception should have been thrown");
    } catch (Exception e) {
    }

  }
}
