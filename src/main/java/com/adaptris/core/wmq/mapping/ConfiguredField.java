package com.adaptris.core.wmq.mapping;

import java.io.IOException;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Add a statically configured value as a MQMessage field.
 *
 * @config wmq-configured-field
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 *
 */
@XStreamAlias("wmq-configured-field")
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
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
