package com.adaptris.core.jms.wmq;

import com.ibm.mq.jms.JMSC;

public class TransportTypeHelper {

  public enum Transport {
    MQJMS_TP_BINDINGS_MQ(JMSC.MQJMS_TP_BINDINGS_MQ),
    MQJMS_TP_CLIENT_MQ_TCPIP(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP),
    MQJMS_TP_DIRECT_TCPIP(JMSC.MQJMS_TP_DIRECT_TCPIP),
    MQJMS_TP_MQJD(JMSC.MQJMS_TP_MQJD),
    MQJMS_TP_DIRECT_HTTP(JMSC.MQJMS_TP_DIRECT_HTTP);
    
    int type;
    Transport(int i) {
      type = i;
    }
    
    int getTransportType() {
      return type;
    }
  }
  
  /**
   * Resolve a friendly name from {@link TransportTypeHelper.Transport} enum into its associated integer value.
   * 
   * @param s the configured string, if unknown, then assumes that {@link Integer#parseInt(String)} will work.
   * @return the int value.
   */
  public static int getTransportType(String s) {
    Transport type = Transport.valueOf(s);
    if (type != null) {
      return type.getTransportType();
    }
    // type is null, so let's just treat it as an integer.
    return Integer.parseInt(s);
  }
}



