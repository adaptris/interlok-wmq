package com.adaptris.core.wmq;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.core.wmq.mapping.FieldMapper;
import com.ibm.mq.MQException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * MessageProducer implementation that uses the WebsphereMQ native client.
 * <p>
 * Depending on your WebsphereMQ configuration you will need to have installed and configured the WebsphereMQ Client software for
 * your platform. The jars from the WebsphereMQ Client software should be copied into the adapter's lib directory.
 * </p>
 * <p>
 * If you configure FieldMappers, then by default the current date and time is inserted as the putDateTime field (using
 * MQC.MQOO_SET_ALL_CONTEXT means that all fields are undefined). This value may be overridden by explicitly declaring a FieldMapper
 * for the putDateTime field. In all cases, {@link com.adaptris.util.text.DateFormatUtil#parse(String)} will be used to parse any
 * values to be used for putDateTime.
 * </p>
 * 
 * @config wmq-native-producer
 * @license ENTERPRISE
 * 
 * 
 * @author lchan
 */
@XStreamAlias("wmq-native-producer")
@AdapterComponent
@ComponentProfile(summary = "Send messages to WebsphereMQ using the native client", tag = "producer,websphere",
    recommended = {AttachedConnection.class, DetachedConnection.class})
public class NativeProducer extends ProduceOnlyProducerImp implements LicensedComponent {

  @XStreamImplicit
  private List<FieldMapper> fieldMappers;
  private MessageOptions options;
  private Boolean checkOptions;
  private transient ProducerDelegate proxy;

  public NativeProducer() {
    setFieldMappers(new ArrayList<FieldMapper>());
    setOptions(new MessageOptions());
  }

  /**
   * This is used for testing purposes
   */
  public ProducerDelegate retrieveProxy(){
	  return proxy;
  }

  /**
   * This is used for testing purposes
   */
  public void registerProxy(ProducerDelegate delegate){
	  proxy = delegate;
  }

  @Override
  public final void prepare() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Enterprise);
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisMessageProducer#produce(com.adaptris.core.AdaptrisMessage,
   *      com.adaptris.core.ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    try {
      proxy.produce(msg, destination);
    } catch (ProduceException ex) {
      if(ex.getCause() instanceof MQException) {
        log.error("MQ Produce Exception:", ex);
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              Thread.sleep(2000); // sleep for a short time to allow any transaction to end, then restart everything.
            } catch (InterruptedException e) {
              // ignore
            }
//            MQException exception = (MQException) ex.getCause();
//            
//            if(exception.reasonCode == MQException.MQRC_CONNECTION_BROKEN) {
              retrieveConnection(NativeConnection.class).getConnectionErrorHandler().handleConnectionException();
//            }
          }
        }).start();
        
      } else
        throw ex;
    }
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    proxy = new ProducerDelegate(this, log);
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
  }

  /**
   * @return the propertyMappers
   */
  public List<FieldMapper> getFieldMappers() {
    return fieldMappers;
  }

  /**
   * Set the list of properties to map into MQMessage fields.
   *
   * @param l the propertyMappers to set
   * @see FieldMapper
   * @see #addFieldMapper(FieldMapper)
   */
  public void setFieldMappers(List l) {
    fieldMappers = l;
  }

  /**
   * Add a property mapper to the list of configured property mappers.
   * <p>
   * If any <code>FieldMapper</code>s are configured, then the corresponding MQOO_SET_ALL_CONTEXT or MQOO_SET_IDENTITY_CONTEXT
   * should be set for {@link MessageOptions#setQueueOpenOptions(String)} and MQPMO_SET_IDENTITY_CONTEXT or MQPMO_SET_ALL_CONTEXT
   * should be set for {@link MessageOptions#setMessageOptions(String)}. If these flags are not set, then it is unlikely that any
   * field mapping that you do will be passed through to WebsphereMQ
   * </p>
   *
   * @param p the property mapper
   * @see MessageOptions#setMessageOptions(String)
   * @see MessageOptions#setQueueOpenOptions(String)
   */
  public void addFieldMapper(FieldMapper p) {
    fieldMappers.add(p);
  }

  /**
   * @return the options
   */
  public MessageOptions getOptions() {
    return options;
  }

  /**
   * @param options the options to set
   */
  public void setOptions(MessageOptions options) {
    this.options = options;
  }

  public Boolean getCheckOptions() {
    return checkOptions;
  }

  /**
   * Specify whether or not to check the queue options and message options.
   * <p>
   * If any {@link FieldMapper} instances are configured, then the corresponding MQOO_SET_ALL_CONTEXT or MQOO_SET_IDENTITY_CONTEXT
   * generally needs to be set for {@link MessageOptions#setQueueOpenOptions(String)} and MQPMO_SET_IDENTITY_CONTEXT or
   * MQPMO_SET_ALL_CONTEXT for {@link MessageOptions#setMessageOptions(String)}. If these flags are not set, then we have found that
   * any field mapping that you do is not passed through to WebsphereMQ
   * </p>
   * <p>
   * If set to true, then if these options are not set, then they are added automatically.
   * </p>
   *
   * @param b default is true, false to not check options.
   * @see MessageOptions#setMessageOptions(String)
   * @see MessageOptions#setQueueOpenOptions(String)
   */
  public void setCheckOptions(Boolean b) {
    checkOptions = b;
  }

  protected boolean checkOptions() {
    return checkOptions != null ? checkOptions.booleanValue() : true;
  }
}
