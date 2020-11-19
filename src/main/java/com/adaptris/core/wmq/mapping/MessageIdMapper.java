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
 * Copies the AdaptrisMessage uniqueId to and from MQMessage fields.
 *
 * <p>
 * Note that if you wish to use the {@link AdaptrisMessage#getUniqueId()} as the
 * <strong>messageId</strong> field (or vice-versa) on the MQMessage then be aware that messageId
 * field is not a string, so you will require a {@linkplain com.adaptris.util.text.ByteTranslator}.
 * Generally speaking, the AdaptrisMessage's unique ID will follow the same format as
 * {@linkplain java.util.UUID#randomUUID()} which means that neither
 * {@linkplain com.adaptris.util.text.HexStringByteTranslator} or
 * {@linkplain com.adaptris.util.text.Base64ByteTranslator} will be suitable.
 * </p>
 *
 * @config wmq-message-id-mapper
 *
 *
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 *
 */
@XStreamAlias("wmq-message-id-mapper")
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
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
