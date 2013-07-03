package com.adaptris.core.wmq.mapping;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.annotation.MarshallingImperative;

import java.io.IOException;
import java.util.UUID;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;

/**
 * This field mapper sets the specified field to a freshly generated UUID.
 * Handy for when consuming request messages in which the recipient has 
 * to specify the correlation id to be used.* <p>
 * In the adapter configuration file this class is aliased as <b>uuid-field-mapper</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author stuellidge
 */
@XStreamAlias("uuid-field-mapper")
@MarshallingImperative(mapTo="uuid-field-mapper", transientFields={})
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
