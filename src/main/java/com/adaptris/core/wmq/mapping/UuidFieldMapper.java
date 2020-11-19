package com.adaptris.core.wmq.mapping;

import java.io.IOException;
import java.util.UUID;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This field mapper sets the specified field to a freshly generated UUID.
 * <p>
 * Handy for when consuming request messages in which the recipient has to specify the correlation
 * id to be used.
 * </p>
 *
 * @config wmq-uuid-field-mapper
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 * 
 * @author stuellidge
 */
@XStreamAlias("wmq-uuid-field-mapper")
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
public class UuidFieldMapper extends FieldMapper {

  @Override
  public void copy(AdaptrisMessage msg, MQMessage mqMsg) throws IOException,
      MQException, CoreException {
    createField(getMqFieldName()).setMqField(mqMsg, UUID.randomUUID().toString(), getByteTranslator());
  }

  @Override
  public void copy(MQMessage mqMsg, AdaptrisMessage msg) throws IOException,
      MQException, CoreException {
    throw new CoreException(this.getClass()
        + " may not be used to copy MQMessage fields to AdaptrisMessage");
  }

}
