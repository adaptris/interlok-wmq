/*
 * $RCSfile: DetachedConnection.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/08/14 13:43:26 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.annotation.MarshallingImperative;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

/**
 * Connection implementation that uses the native WebsphereMQ client.
 * <p>
 * This implementation is one that creates a new MQQueueManager for each
 * invocation of connect(). This type of connection means that all
 * MQQueueManager instances will only be connected for the duration of a single
 * message flow through the adapter.
 * </p>
 * <p>
 * This is the preferred connection for connecting to WebsphereMQ. An
 * <code>AttachedConnection</code> may stop WebsphereMQ from performing a
 * controlled shutdown, and possibly stop WebsphereMQ from recovering from an
 * uncontrolled shutdown.
 * </p>
 * <p>
 * Depending on your WebsphereMQ configuration you will need to have installed
 * and configured the WebsphereMQ Client software for your platform. The jars
 * from the WebsphereMQ Client software should be copied into the adapter's lib
 * directory.
 * </p>
 * <p>
 * If you require SSL support then you should review this <a href="http://www.ibm.com/developerworks/websphere/library/techarticles/0510_fehners/0510_fehners.html"
 * >developerWorks article</a> which contains a good primer about the settings
 * that will be required for using SSL with the adapter.
 * </p>
 * <p>
 * <strong>Requires an ENTERPRISE license</strong>
 * </p>
 ** <p>
 * In the adapter configuration file this class is aliased as <b>wmq-detached-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 *
 */
@XStreamAlias("wmq-detached-connection")
@MarshallingImperative(mapTo = "wmq-detached-connection", transientFields = {})
public class DetachedConnection extends NativeConnection {

  public DetachedConnection() {
    super();
  }

  /**
   *
   * @see com.adaptris.core.wmq.NativeConnection#connect()
   */
  @Override
  MQQueueManager connect() throws MQException {
    return new MQQueueManager(getQueueManager(),
        asHashtable(getEnvironmentProperties()));
  }

  /**
   *
   * @see com.adaptris.core.wmq.NativeConnection#disconnect(com.ibm.mq.MQQueueManager)
   */
  @Override
  void disconnect(MQQueueManager qMgr) throws MQException {
    if (qMgr != null) {
      qMgr.disconnect();
    }
  }
}
