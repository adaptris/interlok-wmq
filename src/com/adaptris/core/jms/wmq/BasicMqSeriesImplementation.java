package com.adaptris.core.jms.wmq;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.VendorImplementationBase;
import com.adaptris.core.jms.VendorImplementationImp;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.ibm.mq.jms.MQConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic IBM MQ Series implementation.
 * </p>
 * <p>
 * Depending on your WebsphereMQ configuration you will need at least the
 * <code><b>com.ibm.mq.jar, com.ibm.mqjms.jar, connector.jar, dhbcore.jar and jta.jar</b></code> from your WebsphereMQ installation.
 * If you intend on using bindings mode, then you may need to include additional jars such as <code>com.ibm.mqbind.jar</code>
 * </p>
 * <p>
 * Note that if you require SSL support then you should use {@link AdvancedMqSeriesImplementation} which allows greater flexibility
 * in configuration
 * </p>
 * <p>
 * By default, all JMS clients to MQ Series will create what is known as an MQRFH2 Header that will form part of the Websphere MQ
 * message. This is used to store (amongst other things) some of the JMS headers that you wanted to preserve using
 * {@link com.adaptris.core.jms.MessageTypeTranslatorImp#setMoveJmsHeaders(Boolean)}, and all the custom JMS properties that you may
 * have chosen to preserve from AdaptrisMessage metadata by configuring
 * {@link com.adaptris.core.jms.MessageTypeTranslatorImp#setMoveMetadata(Boolean)} to be true.<strong>This means that the message
 * format internally within WebpshereMQ is MQRFH2 and not MQSTR format</strong>. Accordingly the receiving application needs to be
 * able to parse MQRFH2 headers which may not be possible.
 * </p>
 * 
 * <p>
 * If the MQRFH2 Header/format is not required or you need to change the message type to MQSTR, then you need to tell MQSeries to
 * omit the MQRFH2 Header; this will mean that you'll lose all the JMS properties that are <a
 * href="http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/topic/com.ibm.mq.csqzaw.doc/uj25460_.htm">mapped into MQRFH2 as
 * standard</a> by MQSeries and also any custom JMS Properties that you might be sending. To omit the MQRFH2 header, then you need
 * to add <code>?targetClient=1</code> after the queue name in your {@link com.adaptris.core.ProduceDestination} implementation. For
 * example, if the queue that you need to produce to is called SampleQ1 then the string you need to use is
 * <strong>queue:///SampleQ1?targetClient=1</strong>. More information about the mapping of JMS messages onto MQ Messages can be
 * found <a href="http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/topic/com.ibm.mq.csqzaw.doc/uj25430_.htm">at this link</a>
 * </p>
 * <p>
 * More generally speaking, the more powerful form of specifying a destination using uniform resource identifiers (URIs) is
 * preferred. This form allows you to specify remote queues (queues on a queue manager other than the one to which you are
 * connected). It also allows you to set the other properties contained in a com.ibm.mq.jms.MQQueue object. The URI for a queue
 * begins with the sequence queue://, followed by the name of the queue manager on which the queue resides. This is followed by a
 * further /, the name of the queue, and optionally, a list of name-value pairs that set the remaining Queue properties. For
 * example: <strong>queue://Some_Other_Queue_Manager/SampleQ1?key1=value1&key2=value2</strong>. If you don't specify a queue manager
 * in the URI then it is interpreted to be the queue manager to which you are currently connected to e.g.
 * <strong>queue:///SampleQ1?key1=value1&key2=value2</strong>).
 * </p>
 * <p>
 * Various name value pairs can be used as part of the URI; these include, but is not limited to (some of these values may have a
 * direct correlation to standard JMS headers, if you specify both, then behaviour is dependent on how WebsphereMQ JMS is
 * configured)
 * <table border=1>
 * <thead>
 * <th>Property</th>
 * <th>Description</th>
 * <th>Values</th>
 * </thead>
 * <tr>
 * <td>CCSID</td>
 * <td>Character set of the destination</td>
 * <td>integers - valid values listed in base WebSphere MQ documentation</td>
 * </tr>
 * <tr>
 * <td>encoding</td>
 * <td>How to represent numeric fields</td>
 * <td>An integer value as described in the base WebSphere MQ documentation</td>
 * </tr>
 * <tr>
 * <td>expiry</td>
 * <td>Lifetime of the message in milliseconds</td>
 * <td>0 for unlimited, positive integers for timeout (ms) - This might interfere with any TTL value you configure in the producer.</td>
 * </tr>
 * <tr>
 * <td>multicast</td>
 * <td>Sets multicast mode for direct connections</td>
 * <td>-1=ASCF, 0=DISABLED, 3=NOTR, 5=RELIABLE, 7=ENABLED</td>
 * </tr>
 * <tr>
 * <td>persistence</td>
 * <td>Whether the message should be <i>hardened</i> to disk</td>
 * <td>1=non-persistent, 2=persistent, -1=QDEF (use the queue definition)</td>
 * </tr>
 * <tr>
 * <td>priority</td>
 * <td>Priority of the message</td>
 * <td>0 through 9, -1=QDEF (use the queue definition)- This might interfere with any priority value you configure in the producer.</td>
 * </tr>
 * <tr>
 * <td>targetClient</td>
 * <td>Whether the receiving application is JMS compliant</td>
 * <td>0=JMS, 1=MQ</td>
 * </tr>
 * </table>
 * 
 * @config basic-mq-series-implementation
 * @license BASIC
 */
@XStreamAlias("basic-mq-series-implementation")
public class BasicMqSeriesImplementation extends VendorImplementationImp implements LicensedComponent {

  private int ccsid;
  @NotBlank
  @AutoPopulated
  @Pattern(regexp = "MQJMS_TP_BINDINGS_MQ|MQJMS_TP_CLIENT_MQ_TCPIP|MQJMS_TP_DIRECT_TCPIP|MQJMS_TP_MQJD|MQJMS_TP_DIRECT_HTTP|[0-9]+")
  private String transportType;
  private String queueManager;
  private String channel;
  private String temporaryModel; // nb PTP only
  @NotBlank
  private String brokerHost;
  private int brokerPort;
  @Deprecated
  private String brokerUrl;
  /**
   * <p>
   * Creates a new instance.
   * <ul>
   * Defaults are:
   * <li>ccsid = -1</li>
   * <li>transport type = MQJMS_TP_CLIENT_MQ_TCPIP (which is equivalent to {@value com.ibm.mq.jms.JMSC#MQJMS_TP_CLIENT_MQ_TCPIP})</li>
   * <li>queue manager, channel and temporary model are all null.</li>
   * </ul>
   * </p>
   */
  public BasicMqSeriesImplementation() {
    setCcsid(-1);
    setTransportType(TransportTypeHelper.Transport.MQJMS_TP_CLIENT_MQ_TCPIP.name());
    setQueueManager(null);
    setChannel(null);
    setTemporaryModel(null);
  }

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {

    MQConnectionFactory result = new MQConnectionFactory();
    result.setHostName(getBrokerHost());
    result.setPort(getBrokerPort());

    result.setTransportType(TransportTypeHelper.getTransportType(getTransportType()));

    if (getQueueManager() != null) {
      result.setQueueManager(getQueueManager());
    }

    if (getCcsid() != -1) {
      result.setCCSID(getCcsid());
    }

    if (getChannel() != null) {
      result.setChannel(getChannel());
    }

    if (getTemporaryModel() != null) {
      result.setTemporaryModel(getTemporaryModel());
    }

    return result;
  }

  /**
   * <p>
   * Returns ccsid.
   * </p>
   *
   * @return ccsid
   */
  public int getCcsid() {
    return ccsid;
  }

  /**
   * <p>
   * Sets ccsid.
   * </p>
   *
   * @param i the ccsid to set
   */
  public void setCcsid(int i) {
    ccsid = i;
  }

  /**
   * <p>
   * Returns transportType.
   * </p>
   *
   * @return transportType
   */
  public String getTransportType() {
    return transportType;
  }

  /**
   * <p>
   * Sets transportType.
   * </p>
   *
   * @param i the transportType to set
   */
  public void setTransportType(String i) {
    transportType = i;
  }

  /**
   * <p>
   * Returns channel.
   * </p>
   *
   * @return channel
   */
  public String getChannel() {
    return channel;
  }

  /**
   * <p>
   * Sets channel.
   * </p>
   *
   * @param s the channel to set
   */
  public void setChannel(String s) {
    channel = s;
  }

  /**
   * <p>
   * Returns queueManager.
   * </p>
   *
   * @return queueManager
   */
  public String getQueueManager() {
    return queueManager;
  }

  /**
   * <p>
   * Sets queueManager.
   * </p>
   *
   * @param s the queueManager to set
   */
  public void setQueueManager(String s) {
    queueManager = s;
  }

  /**
   * <p>
   * Returns temporaryModel.
   * </p>
   *
   * @return temporaryModel
   */
  public String getTemporaryModel() {
    return temporaryModel;
  }

  /**
   * <p>
   * Sets temporaryModel.
   * </p>
   *
   * @param s the temporaryModel to set
   */
  public void setTemporaryModel(String s) {
    temporaryModel = s;
  }

  public String getBrokerHost() {
    return brokerHost;
  }

  public void setBrokerHost(String brokerHost) {
    this.brokerHost = brokerHost;
  }

  public int getBrokerPort() {
    return brokerPort;
  }

  public void setBrokerPort(int port) {
    this.brokerPort = port;
  }


  @Override
  public void prepare() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
    super.prepare();
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Basic);
  }

  @Override
  public boolean connectionEquals(VendorImplementationBase comparable) {
    if (comparable instanceof BasicMqSeriesImplementation) {
      BasicMqSeriesImplementation other = (BasicMqSeriesImplementation) comparable;
      return new EqualsBuilder().append(getBrokerHost(), other.getBrokerHost()).append(getBrokerPort(), other.getBrokerPort())
          .append(getChannel(), other.getChannel())
          .append(getQueueManager(), other.getQueueManager())
          .isEquals();
    }
    return false;
  }
  
  @Override
  public String retrieveBrokerDetailsForLogging() {
    return String.format("Host: %s; Port: %d: Channel: %s; Transport: %s", 
        getBrokerHost(), getBrokerPort(), getChannel(), getTransportType());
  }

  /**
   * 
   * @deprecated has never had any effect; simple included to avoid config break.
   */
  @Deprecated
  public String getBrokerUrl() {
    return brokerUrl;
  }

  /**
   * 
   * @deprecated has never had any effect; simple included to avoid config break.
   */
  @Deprecated
  public void setBrokerUrl(String s) {
    brokerUrl = s;
  }
}
