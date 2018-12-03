package com.adaptris.core.jms.xa.wmq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;

import com.adaptris.core.jms.wmq.AdvancedMqSeriesImplementation;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.xa.jms.XAVendorImplementation;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQXAConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xa-advanced-mq-series-implementation")
public class XaAdvancedMqSeriesImplementation extends AdvancedMqSeriesImplementation implements XAVendorImplementation {

  public XaAdvancedMqSeriesImplementation() {
    setConnectionFactoryProperties(new KeyValuePairSet());
    getConnectionFactoryProperties().addKeyValuePair(
        new KeyValuePair(ConnectionFactoryProperty.TransportType.name(), String.valueOf(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP)));
    setSessionProperties(new KeyValuePairSet());
  }

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    throw new UnsupportedOperationException("This implementation only supports creating XA connection factories.");
  }

  @Override
  public XAConnectionFactory createXAConnectionFactory() throws JMSException {
    MQXAConnectionFactory result = new MQXAConnectionFactory();
    applyProperties(result);
    return result;
  }

  @Override
  public XASession createSession(Connection c, boolean transacted, int acknowledgeMode) throws JMSException {
    if(c instanceof XAConnection) {
      return createXASession((XAConnection)c);
    }
    throw new IllegalStateException("XA VendorImplementation.createSession() called with non-XA connection");
  }

  @Override
  public XASession createXASession(XAConnection c) throws JMSException {
    log.info("Creating a new XA session.");
    XASession session = ((XAConnection)c).createXASession();
    applyVendorSessionProperties(session);
    return session;
  }
  
}
