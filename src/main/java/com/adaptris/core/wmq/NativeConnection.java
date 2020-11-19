/*
 * $RCSfile: NativeConnection.java,v $
 * $Revision: 1.8 $
 * $Date: 2008/06/20 10:57:04 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Iterator;
import org.slf4j.Logger;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.wmq.NoOpJmsConnectionErrorHandler;
//import com.adaptris.core.jms.wmq.NoOpJmsConnectionErrorHandler;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.stream.Slf4jLoggingOutputStream;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

/**
 * Abstract Connection implementation that uses the native WebsphereMQ client.
 *
 *
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 *
 */
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
public abstract class NativeConnection extends AllowsRetriesConnection implements LicensedComponent {

  enum WebsphereProperty {

    CCSID {
      @Override
      void addTo(Hashtable p, String value) {
        p.put(MQC.CCSID_PROPERTY, Integer.parseInt(value));
      }
    },
    connectOptions {
      @Override
      void addTo(Hashtable p, String value) {
        p.put(MQC.CONNECT_OPTIONS_PROPERTY, Integer.parseInt(value));
      }
    },
    port {
      @Override
      void addTo(Hashtable p, String value) {
        p.put(MQC.PORT_PROPERTY, Integer.parseInt(value));
      }

    },
    sslFipsRequired {
      @Override
      void addTo(Hashtable p, String value) {
        p.put(MQC.SSL_FIPS_REQUIRED_PROPERTY, Boolean.valueOf(value));
      }
    };
    abstract void addTo(Hashtable p, String value);
  };

  private String queueManager;
  private MQQueueManager mqQueueManager;
  private KeyValuePairSet environmentProperties;
  private boolean redirectExceptionLogging;
  private static boolean exceptionRedirected = false;

  public NativeConnection() {
    setEnvironmentProperties(new KeyValuePairSet());
    setConnectionErrorHandler(new NoOpJmsConnectionErrorHandler());
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    if (getRedirectExceptionLogging()) {
      redirectExceptionLogging(log);
    }
  }

  @Override
  protected void prepareConnection() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
  }


  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Enterprise);
  }

  /**
   * @return the queueManager
   */
  public String getQueueManager() {
    return queueManager;
  }

  /**
   * Set the Queue manager associated with this connection.
   *
   * @param s the queueManager to set
   */
  public void setQueueManager(String s) {
    queueManager = s;
  }

  /**
   * @return the environmentProperties
   */
  public KeyValuePairSet getEnvironmentProperties() {
    return environmentProperties;
  }

  /**
   * Set any additional properties that should be passed in as part of the constructor to MQQueueManager.
   * <p>
   * The properties specified within the KeyValuePairSet will override the default MQEnvironment properties. Complex properties such
   * as Collections and byte[] are not supported.
   * </p>
   * <p>
   * These options are directly copied from the WebsphereMQ client API documentation. The literal values associated with each java
   * constant should be used where appropriate. e.g. rather than using <code>"MQC.CHANNEL_PROPERTY"</code> as one of the keys you
   * would use <code>"channel"</code> as that is the literal value the constant represents. Any options that aren't recognised by
   * WebsphereMQ will be ignored.
   * </p>
   * <p>
   * Possible values from the WebsphereMQ 6 documentation are:
   * </p>
   * <ul>
   * <li>MQC.CCSID_PROPERTY overrides CCSID</li>
   * <li>MQC.CHANNEL_PROPERTY overrides channel</li>
   * <li>MQC.CONNECT_OPTIONS_PROPERTY overrides connOptions</li>
   * <li>MQC.HOST_NAME_PROPERTY overrides hostname</li>
   * <li>MQC.LOCAL_ADDRESS_PROPERTY overrides localAddressSetting</li>
   * <li>MQC.PASSWORD_PROPERTY overrides password</li>
   * <li>MQC.PORT_PROPERTY overrides port</li>
   * <li>MQC.SSL_CERT_STORE_PROPERTY overrides sslCertStores</li>
   * <li>MQC.SSL_CIPHER_SUITE_PROPERTY overrides sslCipherSuite</li>
   * <li>MQC.SSL_FIPS_REQUIRED overrides sslFipsRequired</li>
   * <li>MQC.SSL_RESET_COUNT_PROPERTY . overrides sslResetCount</li>
   * <li>MQC.SSL_PEER_NAME_PROPERTY . overrides sslPeerName</li>
   * <li>MQC.SSL_SOCKET_FACTORY_PROPERTY overrides sslSocketFactory</li>
   * <li>MQC.TRANSPORT_PROPERTY forces MQC.TRANSPORT_MQSERIES_BINDINGS or MQC.TRANSPORT_MQSERIES_CLIENT</li>
   * <li>MQC.USER_ID_PROPERTY overrides userID</li>
   * </ul>
   *
   * @param kvps the environmentProperties to set
   * @see MQEnvironment#properties
   * @see MQQueueManager#MQQueueManager(String, java.util.Hashtable)
   */
  public void setEnvironmentProperties(KeyValuePairSet kvps) {
    environmentProperties = kvps;
  }

  /**
   * Connect and create a queue manager object for this connection.
   *
   * @return the current queue manager.
   */
  abstract MQQueueManager connect() throws MQException;

  /**
   * Disconnect the associated queueManager.
   *
   * @param mgr the QueueManager
   */
  abstract void disconnect(MQQueueManager mgr) throws MQException;

  /**
   * @return the redirectExceptionLogging
   */
  public boolean getRedirectExceptionLogging() {
    return redirectExceptionLogging;
  }

  /**
   * Redirect logging done by MQException.
   * <p>
   * MQException exposes a static field <code>log</code> which receives additional logging when an Exception is raised. Set this to
   * be true so that this logging is redirected to the Adapter logging framework rather than stderr.
   * </p>
   * <p>
   * The logging category will be this class with a level of <b>ERROR</b>.
   * </p>
   *
   * @param b the redirectExceptionLogging to set
   */
  public void setRedirectExceptionLogging(boolean b) {
    redirectExceptionLogging = b;
  }

  private synchronized static void redirectExceptionLogging(Logger log) {
    if (exceptionRedirected) {
      return;
    }
    else {
      MQException.log = new OutputStreamWriter(new Slf4jLoggingOutputStream(log, Slf4jLoggingOutputStream.LogLevel.ERROR));
      exceptionRedirected = true;
    }
  }

  static Hashtable asHashtable(KeyValuePairSet kvps) {
    Hashtable result = new Hashtable();
    for (Iterator i = kvps.getKeyValuePairs().iterator(); i.hasNext();) {
      KeyValuePair kvp = (KeyValuePair) i.next();
      try {
        WebsphereProperty p = WebsphereProperty.valueOf(kvp.getKey());
        p.addTo(result, kvp.getValue());
      }
      catch (IllegalArgumentException e) {
        result.put(kvp.getKey(), kvp.getValue());
      }
    }
    return result;
  }

  @Override
  protected void closeConnection() {
    ;
  }
}
