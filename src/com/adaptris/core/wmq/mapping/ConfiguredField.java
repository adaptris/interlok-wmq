package com.adaptris.core.wmq.mapping;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.annotation.MarshallingImperative;

import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.ByteTranslator;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;

/**
 * Add a statically configured value as a MQMessage field.
 * * <p>
 * In the adapter configuration file this class is aliased as <b>wmq-configured-field</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 * 
 */
@XStreamAlias("wmq-configured-field")
@MarshallingImperative(mapTo="wmq-configured-field", transientFields={})
public class ConfiguredField extends FieldMapper {

  private String configuredValue;

  public ConfiguredField() {
    super();
  }

  /**
   * 
   * @param mqField the field name
   * @param value the value to map.
   */
  public ConfiguredField(String mqField, String value) {
    this(mqField, value, null);
  }

  /**
   * 
   * @param mqField the field name
   * @param value the value to map.
   * @param bt the byte translator
   */
  public ConfiguredField(String mqField, String value, ByteTranslator bt) {
    this();
    setMqFieldName(mqField);
    setConfiguredValue(value);
    setByteTranslator(bt);
  }

  @Override
  public void copy(AdaptrisMessage msg, MQMessage mqMsg) throws IOException,
      MQException, CoreException {
    String value = getConfiguredValue();
    if (value == null && getConvertNull()) {
      logR.trace("Converting null configured value to \"\"");
      value = "";
    }
    logR.trace("Setting [" + value + "] as field [" + getMqFieldName() + "]");
    createField(getMqFieldName()).setMqField(mqMsg, value, getByteTranslator());
  }

  /**
   * 
   * @see com.adaptris.core.wmq.mapping.FieldMapper#copy(com.ibm.mq.MQMessage,
   *      com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void copy(MQMessage mqMsg, AdaptrisMessage msg) throws IOException,
      MQException, CoreException {
    throw new CoreException(this.getClass()
        + " may not be used to copy MQMessage fields to AdaptrisMessage");
  }

  /**
   * @return the configuredValue
   */
  public String getConfiguredValue() {
    return configuredValue;
  }

  /**
   * @param s the value which will be used to populate the associated MQ Field.
   */
  public void setConfiguredValue(String s) {
    configuredValue = s;
  }

}
