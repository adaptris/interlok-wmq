package com.adaptris.core.jms.xa.wmq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;

import com.adaptris.core.jms.wmq.BasicMqSeriesImplementation;
import com.adaptris.core.jms.wmq.TransportTypeHelper;
import com.adaptris.xa.jms.XAVendorImplementation;
import com.ibm.mq.jms.MQXAConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xa-basic-mq-series-implementation")
public class XaBasicMqSeriesImplementation extends BasicMqSeriesImplementation implements XAVendorImplementation {
  
  public XaBasicMqSeriesImplementation() {
    super();
  }

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {
    throw new UnsupportedOperationException("This implementation only supports creating XA connection factories.");
  }

  @Override
  public XAConnectionFactory createXAConnectionFactory() throws JMSException {
    MQXAConnectionFactory result = new MQXAConnectionFactory();
    result.setHostName(getBrokerHost());
    result.setPort(getBrokerPort());

    result.setTransportType(TransportTypeHelper.getTransportType(getTransportType()));

    if (getQueueManager() != null) 
      result.setQueueManager(getQueueManager());

    if (getCcsid() != -1) 
      result.setCCSID(getCcsid());

    if (getChannel() != null) 
      result.setChannel(getChannel());

    if (getTemporaryModel() != null) 
      result.setTemporaryModel(getTemporaryModel());

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
