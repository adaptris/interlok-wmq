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
 * Copy AdaptrisMessage metadata values to and from MQMessage fields.
 *
 * @config wmq-metadata-field-mapper
 *
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 *
 */
@XStreamAlias("wmq-metadata-field-mapper")
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
public class MetadataFieldMapper extends FieldMapper {

  private String metadataKey;

  public MetadataFieldMapper() {
    super();
  }

  /**
   *
   * @param mqField The MQ Field name
   * @param metadataKey the adaptris metadata key.
   */
  public MetadataFieldMapper(String mqField, String metadataKey) {
    this(mqField, metadataKey, null);
  }

  /**
   *
   * @param mqField the MQ Field name
   * @param metadataKey the adaptris metadata key
   * @param bt the byte translator.
   */
  public MetadataFieldMapper(String mqField, String metadataKey, ByteTranslator bt) {
    this();
    setMqFieldName(mqField);
    setMetadataKey(metadataKey);
    setByteTranslator(bt);
  }

  @Override
  public void copy(AdaptrisMessage msg, MQMessage mqMsg) throws IOException,
      MQException, CoreException {
    if (msg.containsKey(getMetadataKey())) {
      String mdValue = msg.getMetadataValue(getMetadataKey());
      if (mdValue == null && getConvertNull()) {
        logR.trace("Converting null value for " + getMetadataKey() + " to \"\"");
        mdValue = "";
      }
      logR.trace("Setting [" + mdValue + "] as field [" + getMqFieldName()
          + "]");
      createField(getMqFieldName()).setMqField(mqMsg, mdValue, getByteTranslator());
    }
    else {
      logR.debug("Message does not contain " + getMetadataKey() + ", ignoring");
    }
  }

  @Override
  public void copy(MQMessage mqMsg, AdaptrisMessage msg) throws IOException,
      MQException, CoreException {
    msg.addMetadata(getMetadataKey(), createField(getMqFieldName()).getMqField(mqMsg,
        getByteTranslator()));
  }

  /**
   * @return the metadataKey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * @param metadataKey the metadataKey from which corresponds to the MQ Field
   *          value.
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
