package com.adaptris.core.wmq.mapping;

import java.io.IOException;

import com.adaptris.annotation.MarshallingImperative;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.ByteTranslator;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Copies the AdaptrisMessage uniqueId to and from MQMessage fields.
 *
 * <p>
 * Note that if you wish to use the {@link AdaptrisMessage#getUniqueId()} as the <strong>messageId</strong> field (or vice-versa) on
 * the MQMessage then be aware that messageId field is not a string, so you will require a
 * {@linkplain com.adaptris.util.text.ByteTranslator}. Generally speaking, the AdaptrisMessage's unique ID will follow the same
 * format as {@linkplain java.util.UUID#randomUUID()} which means that neither
 * {@linkplain com.adaptris.util.text.HexStringByteTranslator} or {@linkplain com.adaptris.util.text.Base64ByteTranslator} will be
 * suitable.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>wmq-message-id-mapper</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 *
 *
 * @author lchan
 *
 */
@XStreamAlias("wmq-message-id-mapper")
@MarshallingImperative(mapTo="wmq-message-id-mapper", transientFields={})
public class MessageIdMapper extends FieldMapper {

  public MessageIdMapper() {
    super();
    setMqFieldName("messageId");
  }

  /**
   *
   * @param mqField the mqfieldname
   */
  public MessageIdMapper(String mqField) {
    this(mqField, null);
  }

  /**
   *
   * @param mqField the mqfieldname
   * @param bt the byte translator.
   */
  public MessageIdMapper(String mqField, ByteTranslator bt) {
    this();
    setMqFieldName(mqField);
    setByteTranslator(bt);
  }

  /**
   *
   * @see com.adaptris.core.wmq.mapping.FieldMapper#copy(com.adaptris.core.AdaptrisMessage, com.ibm.mq.MQMessage)
   */
  @Override
  public void copy(AdaptrisMessage msg, MQMessage mqMsg) throws IOException, MQException, CoreException {
    createField(getMqFieldName()).setMqField(mqMsg, msg.getUniqueId(), getByteTranslator());
  }

  /**
   *
   * @see com.adaptris.core.wmq.mapping.FieldMapper#copy(com.ibm.mq.MQMessage, com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void copy(MQMessage mq, AdaptrisMessage m) throws IOException, MQException, CoreException {
    m.setUniqueId(createField(getMqFieldName()).getMqField(mq, getByteTranslator()));
  }

}
