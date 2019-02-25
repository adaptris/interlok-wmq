/*
 * $RCSfile: NativeConsumer.java,v $
 * $Revision: 1.8 $
 * $Date: 2009/07/01 16:52:52 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.core.wmq.mapping.FieldMapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MessageConsumer implementation that uses the WebsphereMQ native client.
 * <p>
 * Depending on your WebsphereMQ configuration you will need to have installed and configured the WebsphereMQ Client software for
 * your platform. The jars from the WebsphereMQ Client software should be copied into the adapter's lib directory.
 * </p>
 * 
 * @config wmq-native-consumer
 * @license ENTERPRISE
 * 
 * @author lchan
 * 
 */
@XStreamAlias("wmq-native-consumer")
@AdapterComponent
@ComponentProfile(summary = "Receive messages from WebsphereMQ using the native client", tag = "consumer,websphere",
    recommended = {AttachedConnection.class, DetachedConnection.class})
public class NativeConsumer extends AdaptrisPollingConsumer implements LicensedComponent {

  private List<FieldMapper> preGetFieldMappers;
  private List<FieldMapper> fieldMappers;
  private MessageOptions options;
  private transient ConsumerDelegate proxy;
  private boolean logAllExceptions;
  private NativeErrorHandler errorHandler;

  public NativeConsumer() {
    setOptions(new MessageOptions());
    setFieldMappers(new ArrayList<FieldMapper>());
    setPreGetFieldMappers(new ArrayList<FieldMapper>());
    setLogAllExceptions(false);
  }

  @Override
  public void init() throws CoreException {
    proxy = new ConsumerDelegate(this, log);
    if(getErrorHandler() != null) {
      proxy.setErrorHandler(getErrorHandler());
    }
    super.init();
  }

  @Override
  public int processMessages() {
    return proxy.processMessages();
  }

  /**
   * This is used for testing purposes
   */
  public ConsumerDelegate retrieveProxy(){
	  return proxy;
  }

  /**
   * This is used for testing purposes
   */
  public void registerProxy(ConsumerDelegate delegate){
	  proxy = delegate;
  }

  /**
   * @return the options
   */
  public MessageOptions getOptions() {
    return options;
  }

  /**
   * Set the specific options when opening a MQQueue and receiving messages.
   * <p>
   * This consumer implementation makes use of the MQQueue#getCurrentDepth() method. As a result, if you choose non default queue
   * opening options you must explicitly enable the flag MQC.MQOO_INQUIRE as part of
   * {@link MessageOptions#setQueueOpenOptions(String)}
   * </p>
   *
   * @param options the options to set
   */
  public void setOptions(MessageOptions options) {
    this.options = options;
  }

  /**
   * @return the propertyMappers
   */
  public List<FieldMapper> getFieldMappers() {
    return fieldMappers;
  }

  /**
   * Set the list of property mappers mapping from MQMessage fields.
   *
   * @param l the propertyMappers to set
   * @see FieldMapper
   */
  public void setFieldMappers(List<FieldMapper> l) {
    fieldMappers = l;
  }

  /**
   * Add a property mapper to the list of configured property mappers.
   *
   * @param p the property mapper
   * @see FieldMapper
   */
  public void addFieldMapper(FieldMapper p) {
    fieldMappers.add(p);
  }

  /**
   * @return the logAllExceptions
   * @see #setLogAllExceptions(boolean)
   */
  public boolean getLogAllExceptions() {
    return logAllExceptions;
  }

  /**
   * Specify whether to log all exceptions encountered during processing.
   * <p>
   * The default value is false, which means that a simple one-line log message
   * is output each time the PollingConsumer encounters an exception, in the
   * expectation that the errors are transient and will be resolved by the next
   * scheduled poll. In some cases, this is not desired, perhaps you are testing
   * some complex configuration, and you need to see the staktrace associated
   * with the exception generated by WebsphereMQ
   * </p>
   *
   * @param b the logAllExceptions to set
   */
  public void setLogAllExceptions(boolean b) {
    logAllExceptions = b;
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Enterprise);
  }

  @Override
  protected void prepareConsumer() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
  }


  /**
   * Specifies pre-get field mappers
   * <p>
   * Sets a list of FieldMappers that will be applied to the empty MQMessage
   * object sent to the MQ Queue Manager when retrieving messages. This
   * enables items such as messageType and version to be set prior to
   * fetching messages.
   * </p>
   * @param preGetFieldMappers
   */
  public void setPreGetFieldMappers(List<FieldMapper> preGetFieldMappers) {
    this.preGetFieldMappers = preGetFieldMappers;
  }

  /**
   * @return the list of pre-Get field mappers
   */
  public List<FieldMapper> getPreGetFieldMappers() {
    return preGetFieldMappers;
  }

  public NativeErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(NativeErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
}