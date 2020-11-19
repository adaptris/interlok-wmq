/*
 * $RCSfile: AttachedConnection.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/08/14 13:43:26 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Connection implementation that uses the native WebsphereMQ API.
 * <p>
 * This implementation creates a single MQQueueManager instance when the connection is started. This
 * MQQueueManager instance is then shared across all components associated with this connection.
 * </p>
 * <p>
 * It is not recommended to use this type of connection, as a persistent MQQueueManager may stop
 * some versions of WebsphereMQ from performing a controlled shutdown. In addition to this, it may
 * also stop WebsphereMQ from recovering from an uncontrolled shutdown.
 * </p>
 * <p>
 * If you require SSL support then you should review this <a href=
 * "http://www.ibm.com/developerworks/websphere/library/techarticles/0510_fehners/0510_fehners.html"
 * >developerWorks article</a> which contains a good primer about the settings that will be required
 * for using SSL with the adapter.
 * </p>
 * <p>
 * Depending on your WebsphereMQ configuration you will need to have installed and configured the
 * WebsphereMQ Client software for your platform. The jars from the WebsphereMQ Client software
 * should be copied into the adapter's lib directory.
 * </p>
 *
 * @config wmq-attached-connection
 * @license ENTERPRISE
 *
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 *
 */
@Deprecated
@XStreamAlias("wmq-attached-connection")
@AdapterComponent
@ComponentProfile(summary = "Connection for supporting a native WebsphereMQ Client", tag = "connections,websphere",
    recommended = {NativeConsumer.class, NativeProducer.class})
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
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

  }

  /**
  *
  * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
  */
 @Override
 protected void initConnection() throws CoreException {
   super.initConnection();
   int attemptCount = 0;
   while(true) {
     attemptCount++;
     try {
       mqQueueManager = new MQQueueManager(getQueueManager(),
           asHashtable(getEnvironmentProperties()));
       break;
     }
     catch (MQException e) {
        if (logWarning(attemptCount)) {
          log.warn("Attempt [{}] failed for queue manager [{}]", attemptCount, getQueueManager(), e);
       }

       if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
         log.error("Failed to connect to queue manager [{}]", getQueueManager(), e);
         throw new CoreException(e);
       }
       else {
         log.warn("Attempt [{}] failed for broker [{}], retrying", attemptCount, getQueueManager());
         log.info(createLoggingStatement(attemptCount));
         try {
          Thread.sleep(connectionRetryInterval());
        } catch (InterruptedException e1) {
          log.warn("Interrupted connection restart.");
          break;
        }
         continue;
       }
     }
   }
 }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
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
