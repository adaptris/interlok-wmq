package com.adaptris.core.jms.wmq;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.VendorImplementationBase;
import com.adaptris.core.jms.VendorImplementationImp;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQSession;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Advanced IBM MQ Series implementation.
 * </p>
 * <p>
 * Depending on your WebsphereMQ configuration you will need at least
 * 
 * <code><b>com.ibm.mq.jar, com.ibm.mqjms.jar, connector.jar, dhbcore.jar and jta.jar</b></code> from your WebsphereMQ installation.
 * If you intend on using bindings mode, then you may need to include additional jars such as <code>com.ibm.mqbind.jar</code>
 * </p>
 * <p>
 * This vendor implementation class directly exposes all the primitive and String getter and setters that are available in the
 * MQConnectionFactory for maximum flexibility in configuration.
 * </p>
 * <p>
 * The key from the <code>connection-factory-properties</code> element should match the name of the underlying MQConnectionFactory
 * property/method. <code>
 * <pre>
 *   &lt;connection-factory-properties>
 *     &lt;key-value-pair>
 *        &lt;key>SendExitInit&lt;/key>
 *        &lt;value>SomeData&lt;/value>
 *     &lt;/key-value-pair>
 *   &lt;/connection-factory-properties>
 * </pre>
 * </code> would call {@link MQConnectionFactory#setSendExitInit(String)}.
 * </p>
 * <p>
 * This vendor implementation also overrides {@link VendorImplementationImp#applyVendorSessionProperties(javax.jms.Session)} so that
 * specific MQ session properties can be applied. The way of doing this is exactly the same as setting properties on the
 * ConnectionFactory. <code>
 * <pre>
 *   &lt;session-properties>
 *     &lt;key-value-pair>
 *        &lt;key>OptimisticPublication&lt;/key>
 *        &lt;value>true&lt;/value>
 *     &lt;/key-value-pair>
 *   &lt;/session-properties>
 * </pre>
 * </code> would invoke {@link MQSession#setOptimisticPublication(boolean)} with true.
 * </p>
 * <p>
 * If you require SSL support then you should review this <a
 * href="http://www.ibm.com/developerworks/websphere/library/techarticles/0510_fehners/0510_fehners.html" >developerWorks
 * article</a> which contains a good primer about the settings that will be required for using SSL with the adapter.
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
 * <p>
 * <b>This was built against WebsphereMQ 6.x, but tested against both Websphere 6.x and 7.x</b>
 * </p>
 * 
 * @config advanced-mq-series-implementation
 * @license BASIC
 * 
 * @see com.ibm.mq.jms.MQConnectionFactory
 */
@XStreamAlias("advanced-mq-series-implementation")
public class AdvancedMqSeriesImplementation extends VendorImplementationImp implements LicensedComponent {
  /**
   * Properties matched against various MQSession methods.
   */
  public enum SessionProperty {
    /**
     * Invokes {@link MQSession#setBrokerTimeout(int)}
     *
     */
    BrokerTimeout {
      @Override
      void applyProperty(MQSession s, String o) throws JMSException {
        s.setBrokerTimeout(Integer.valueOf(o));
      }
    },
    /**
     * Invokes {@link MQSession#setOptimisticPublication(boolean)}
     *
     */
    OptimisticPublication {
      @Override
      void applyProperty(MQSession s, String o) throws JMSException {
        s.setOptimisticPublication(Boolean.valueOf(o));
      }
    },
    /**
     * Invokes {@link MQSession#setOutcomeNotification(boolean)}
     *
     */
    OutcomeNotification {
      @Override
      void applyProperty(MQSession s, String o) throws JMSException {
        s.setOutcomeNotification(Boolean.valueOf(o));
      }
    },
    /**
     * Invokes {@link MQSession#setProcessDuration(int)}
     *
     */
    ProcessDuration {
      @Override
      void applyProperty(MQSession s, String o) throws JMSException {
        s.setProcessDuration(Integer.valueOf(o));
      }
    },
    /**
     * Invokes {@link MQSession#setReceiveIsolation(int)}
     *
     */
    ReceiveIsolation {
      @Override
      void applyProperty(MQSession s, String o) throws JMSException {
        s.setReceiveIsolation(Integer.valueOf(o));
      }
    };
    abstract void applyProperty(MQSession s, String o) throws JMSException;
  }

  /**
   * Properties matched against various MQConnectionFactory methods.
   */
  public enum ConnectionFactoryProperty {
    /**
     * Invokes {@link MQConnectionFactory#setBrokerCCSubQueue(String)}
     *
     */
    BrokerCCSubQueue {
      @Override
      void applyProperty(MQConnectionFactory cf, String o) throws JMSException {
        cf.setBrokerCCSubQueue(o);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setBrokerControlQueue(String)}
     *
     */
    BrokerControlQueue {
      @Override
      void applyProperty(MQConnectionFactory cf, String o) throws JMSException {
        cf.setBrokerControlQueue(o);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setBrokerPubQueue(String)}
     *
     */
    BrokerPubQueue {
      @Override
      void applyProperty(MQConnectionFactory cf, String o) throws JMSException {
        cf.setBrokerPubQueue(o);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setBrokerQueueManager(String)}
     *
     */
    BrokerQueueManager {
      @Override
      void applyProperty(MQConnectionFactory cf, String p) throws JMSException {
        cf.setBrokerQueueManager(p);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setBrokerSubQueue(String)}
     *
     */
    BrokerSubQueue {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setBrokerSubQueue(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setBrokerVersion(int)}
     *
     */
    BrokerVersion {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setBrokerVersion(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setCCDTURL(URL)}
     *
     */
    CCDTURL {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        try {
          cf.setCCDTURL(new URL(s));
        }
        catch (MalformedURLException e) {
          JMSException e2 = new JMSException(e.getMessage());
          e2.initCause(e);
          throw e2;
        }
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setCCSID(int)}
     *
     */
    CCSID {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setCCSID(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setChannel(String)}
     *
     */
    Channel {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setChannel(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setCleanupInterval(long)}
     *
     */
    CleanupInterval {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setCleanupInterval(Long.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setCleanupLevel(int)}
     *
     */
    CleanupLevel {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setCleanupLevel(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setClientID(String)}
     */
    ClientID {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setClientID(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setCloneSupport(int)}
     *
     */
    CloneSupport {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setCloneSupport(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setDescription(String)}
     *
     */
    Description {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setDescription(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setDirectAuth(int)}
     *
     */
    DirectAuth {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setDirectAuth(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setFailIfQuiesce(int)}
     *
     */
    FailIfQuiesce {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setFailIfQuiesce(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setHostName(String)}
     *
     */
    HostName {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setHostName(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setLocalAddress(String)}
     *
     */
    LocalAddress {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setLocalAddress(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setMapNameStyle(boolean)}
     *
     */
    MapNameStyle {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setMapNameStyle(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setMessageRetention(int)}
     *
     */
    MessageRetention {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setMessageRetention(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setMessageSelection(int)}
     *
     */
    MessageSelection {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setMessageSelection(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setMQConnectionOptions(int)}
     *
     */
    MQConnectionOptions {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setMQConnectionOptions(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setMsgBatchSize(int)}
     *
     */
    MsgBatchSize {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setMsgBatchSize(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setMulticast(int)}
     *
     */
    Multicast {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setMulticast(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setOptimisticPublication(boolean)}
     *
     */
    OptimisticPublication {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setOptimisticPublication(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setOutcomeNotification(boolean)}
     *
     */
    OutcomeNotification {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setOutcomeNotification(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setPollingInterval(int)}
     *
     */
    PollingInterval {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setPollingInterval(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setPort(int)}
     *
     */
    Port {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setPort(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setProcessDuration(int)}
     *
     */
    ProcessDuration {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setProcessDuration(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setProxyHostName(String)}
     *
     */
    ProxyHostName {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setProxyHostName(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setProxyPort(int)}
     *
     */
    ProxyPort {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setProxyPort(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setPubAckInterval(int)}
     *
     */
    PubAckInterval {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setPubAckInterval(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setQueueManager(String)}
     *
     */
    QueueManager {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setQueueManager(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setReceiveExit(String)}
     *
     */
    ReceiveExit {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setReceiveExit(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setReceiveExitInit(String)}
     *
     */
    ReceiveExitInit {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setReceiveExitInit(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setReceiveIsolation(int)}
     *
     */
    ReceiveIsolation {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setReceiveIsolation(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setRescanInterval(int)}
     *
     */
    RescanInterval {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setRescanInterval(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSecurityExit(String)}
     *
     */
    SecurityExit {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSecurityExit(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSecurityExitInit(String)}
     *
     */
    SecurityExitInit {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSecurityExitInit(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSendExit(String)}
     *
     */
    SendExit {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSendExit(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSendExitInit(String)}
     *
     */
    SendExitInit {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSendExitInit(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSparseSubscriptions(boolean)}
     *
     */
    SparseSubscriptions {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSparseSubscriptions(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSSLCertStores(String)}
     *
     */
    SSLCertStores {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSSLCertStores(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSSLCipherSuite(String)}
     *
     */
    SSLCipherSuite {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSSLCipherSuite(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSSLFipsRequired(boolean)}
     *
     */
    SSLFipsRequired {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSSLFipsRequired(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSSLPeerName(String)}
     *
     */
    SSLPeerName {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSSLPeerName(s);
      }
    },

    /**
     * Invokes {@link MQConnectionFactory#setSSLResetCount(int)}
     *
     */
    SSLResetCount {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSSLResetCount(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setStatusRefreshInterval(int)}
     *
     */
    StatusRefreshInterval {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setStatusRefreshInterval(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSubscriptionStore(int)}
     *
     */
    SubscriptionStore {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSubscriptionStore(Integer.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setSyncpointAllGets(boolean)}
     *
     */
    SyncpointAllGets {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setSyncpointAllGets(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setTargetClientMatching(boolean)}
     *
     */
    TargetClientMatching {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setTargetClientMatching(Boolean.valueOf(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setTemporaryModel(String)}
     *
     */
    TemporaryModel {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setTemporaryModel(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setTempQPrefix(String)}
     *
     */
    TempQPrefix {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setTempQPrefix(s);
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setTransportType(int)}
     *
     */
    TransportType {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setTransportType(TransportTypeHelper.getTransportType(s));
      }
    },
    /**
     * Invokes {@link MQConnectionFactory#setUseConnectionPooling(boolean)}
     *
     */
    UseConnectionPooling {
      @Override
      void applyProperty(MQConnectionFactory cf, String s) throws JMSException {
        cf.setUseConnectionPooling(Boolean.valueOf(s));
      }
    };

    abstract void applyProperty(MQConnectionFactory cf, String s) throws JMSException;
  };

  @NotNull
  @AutoPopulated
  private KeyValuePairSet connectionFactoryProperties;
  @NotNull
  @AutoPopulated
  private KeyValuePairSet sessionProperties;

  /**
   * <p>
   * Creates a new instance.
   * <ul>
   * <li>TransportType = JMSC.MQJMS_TP_CLIENT_MQ_TCPIP</li>
   * </ul>
   * </p>
   */
  public AdvancedMqSeriesImplementation() {
    setConnectionFactoryProperties(new KeyValuePairSet());
    getConnectionFactoryProperties().addKeyValuePair(
        new KeyValuePair(ConnectionFactoryProperty.TransportType.name(), String.valueOf(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP)));
    setSessionProperties(new KeyValuePairSet());
  }

  @Override
  public ConnectionFactory createConnectionFactory() throws JMSException {

    MQConnectionFactory result = new MQQueueConnectionFactory();
    applyProperties(result);
    return result;
  }

  private void applyProperties(MQConnectionFactory cf) throws JMSException {
    for (Iterator i = getConnectionFactoryProperties().getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kvp = (KeyValuePair) i.next();
      // Yeah we could use valueOf here, but really, our lusers are sure to not
      // be consistent and valueOf is case sensitive.
      for (ConnectionFactoryProperty sp : ConnectionFactoryProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(cf, kvp.getValue());
        }
      }

    }
  }

  /**
   *
   * @see VendorImplementationImp#applyVendorSessionProperties(javax.jms.Session)
   */
  @Override
  public void applyVendorSessionProperties(javax.jms.Session s) throws JMSException {
    for (Iterator i = getSessionProperties().getKeyValuePairs().iterator(); i.hasNext();) {
      boolean matched = false;

      KeyValuePair kvp = (KeyValuePair) i.next();
      // Yeah we could use valueOf here, but really, our lusers are sure to not
      // be consistent and valueOf is case sensitive.
      for (SessionProperty sp : SessionProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty((MQSession) s, kvp.getValue());
          matched = true;
        }
      }
      if (!matched) {
        log.trace("Ignoring unsupported Session Property " + kvp.getKey());
      }
    }
  }

  /**
   * @return the connectionFactoryProperties
   */
  public KeyValuePairSet getConnectionFactoryProperties() {
    return connectionFactoryProperties;
  }

  /**
   * @param crp the additional connectionFactoryProperties to set
   * @see ConnectionFactoryProperty
   */
  public void setConnectionFactoryProperties(KeyValuePairSet crp) {
    connectionFactoryProperties = crp;
  }

  /**
   * @return the sessionProperties
   */
  public KeyValuePairSet getSessionProperties() {
    return sessionProperties;
  }

  /**
   * Set any additional session properties to be applied.
   *
   * @param s the sessionProperties to set
   * @see SessionProperty
   */
  public void setSessionProperties(KeyValuePairSet s) {
    sessionProperties = s;
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
      AdvancedMqSeriesImplementation other = (AdvancedMqSeriesImplementation) comparable;
      return new EqualsBuilder().append(getConnectionFactoryProperties(), other.getConnectionFactoryProperties()).isEquals();
    }
    return false;
  }
}
