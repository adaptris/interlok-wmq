/*
 * $RCSfile: AttachedConnection.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/08/14 13:43:26 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import com.adaptris.annotation.MarshallingImperative;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

/**
 * Connection implementation that uses the native WebsphereMQ API.
 * <p>
 * This implementation creates a single MQQueueManager instance when the
 * connection is started. This MQQueueManager instance is then shared across all
 * components associated with this connection.
 * </p>
 * <p>
 * It is not recommended to use this type of connection, as a persistent
 * MQQueueManager may stop some versions of WebsphereMQ from performing a
 * controlled shutdown. In addition to this, it may also stop WebsphereMQ from
 * recovering from an uncontrolled shutdown.
 * </p>
 * <p>
 * If you require SSL support then you should review this <a href="http://www.ibm.com/developerworks/websphere/library/techarticles/0510_fehners/0510_fehners.html"
 * >developerWorks article</a> which contains a good primer about the settings
 * that will be required for using SSL with the adapter.
 * </p>
 * <p>
 * Depending on your WebsphereMQ configuration you will need to have installed
 * and configured the WebsphereMQ Client software for your platform. The jars
 * from the WebsphereMQ Client software should be copied into the adapter's lib
 * directory.
 * </p>
 * <p>
 * <strong>Requires an ENTERPRISE license</strong>
 * </p>
 ** <p>
 * In the adapter configuration file this class is aliased as <b>wmq-attached-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 *
 */
@XStreamAlias("wmq-attached-connection")
@MarshallingImperative(mapTo = "wmq-attached-connection", transientFields = {})
public class AttachedConnection extends NativeConnection {

  private MQQueueManager mqQueueManager;

  public AttachedConnection() {
    super();
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
    super.startConnection();
    try {
      mqQueueManager = new MQQueueManager(getQueueManager(),
          asHashtable(getEnvironmentProperties()));
    }
    catch (MQException e) {
      throw new CoreException(e);
    }
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
    super.stopConnection();
    try {
      mqQueueManager.disconnect();
    }
    catch (Exception e) {
      log.warn("Exception caught trying to close QueueManager "
          + e.getMessage());
    }
  }

  /**
   *
   * @see com.adaptris.core.wmq.NativeConnection#connect()
   */
  @Override
  MQQueueManager connect() {
    return mqQueueManager;
  }

  /**
   *
   * @see com.adaptris.core.wmq.NativeConnection#disconnect(com.ibm.mq.MQQueueManager)
   */
  @Override
  void disconnect(MQQueueManager mgr) throws MQException {
    ;
  }

}
