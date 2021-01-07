/*
 * $RCSfile: FieldMapperTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.wmq.mapping.FieldMapper.Field;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.Base64ByteTranslator;
import com.adaptris.util.text.DateFormatUtil;
import com.adaptris.util.text.SimpleByteTranslator;
import com.ibm.mq.MQMessage;

public class FieldMapperTest extends BaseCase {

  private static final String LINE_SEP = System.getProperty("line.separator");
  private static final String XML_DOC = "<root>" + LINE_SEP + "<document>value</document>" + LINE_SEP + "</root>" + LINE_SEP;
  private static final String DEST_XPATH = "/root/document";
  private static final String STRING_CONTENT = "1234567890ABCDEF";
  private static final String NUMERIC_CONTENT = "102";
  private static final String VERSION = "2";

  @Test
  public void testFieldAccountingToken() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.accountingToken);
    String field = get(mqMsg, Field.accountingToken);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldApplicationIdData() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.applicationIdData);
    String field = get(mqMsg, Field.applicationIdData);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldApplicationOriginData() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.applicationOriginData);
    String field = get(mqMsg, Field.applicationOriginData);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldFormat() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.format);
    String field = get(mqMsg, Field.format);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldPutApplicationName() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.putApplicationName);
    String field = get(mqMsg, Field.putApplicationName);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldReplyToQueueManagerName() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.replyToQueueManagerName);
    String field = get(mqMsg, Field.replyToQueueManagerName);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldReplyToQueueName() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.replyToQueueName);
    String field = get(mqMsg, Field.replyToQueueName);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldUserId() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.userId);
    String field = get(mqMsg, Field.userId);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldCorrelationId() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.correlationId);
    String field = get(mqMsg, Field.correlationId);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldGroupId() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.groupId);
    String field = get(mqMsg, Field.groupId);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldMessageId() throws Exception {
    MQMessage mqMsg = new MQMessage();
    set(mqMsg, Field.messageId);
    String field = get(mqMsg, Field.messageId);
    assertEquals(STRING_CONTENT, field);
  }

  @Test
  public void testFieldBackoutCount() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.backoutCount);
    String field = get(mqMsg, Field.backoutCount);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldEncoding() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.encoding);
    String field = get(mqMsg, Field.encoding);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldExpiry() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.expiry);
    String field = get(mqMsg, Field.expiry);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldFeedback() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.feedback);
    String field = get(mqMsg, Field.feedback);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldMessageFlags() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.messageFlags);
    String field = get(mqMsg, Field.messageFlags);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldMessageSequenceNumber() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.messageSequenceNumber);
    String field = get(mqMsg, Field.messageSequenceNumber);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldCharacterSet() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.characterSet);
    String field = get(mqMsg, Field.characterSet);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldMessageType() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.messageType);
    String field = get(mqMsg, Field.messageType);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldOffset() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.offset);
    String field = get(mqMsg, Field.offset);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldOriginalLength() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.originalLength);
    String field = get(mqMsg, Field.originalLength);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldPersistence() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.persistence);
    String field = get(mqMsg, Field.persistence);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldPriority() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.priority);
    String field = get(mqMsg, Field.priority);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldPutApplicationType() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.putApplicationType);
    String field = get(mqMsg, Field.putApplicationType);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldReport() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setInt(mqMsg, Field.report);
    String field = get(mqMsg, Field.report);
    assertEquals(NUMERIC_CONTENT, field);
  }

  @Test
  public void testFieldVersion() throws Exception {
    MQMessage mqMsg = new MQMessage();
    setVersion(mqMsg, Field.version);
    String field = get(mqMsg, Field.version);
    assertEquals(VERSION, field);

  }

  @Test
  public void testFieldPutDateTime() throws Exception {
    MQMessage mqMsg = new MQMessage();
    String now = DateFormatUtil.format(new Date());
    Field.putDateTime.setMqField(mqMsg, now, null);
    String field = get(mqMsg, Field.putDateTime);
    assertEquals(now, field);
  }

  @Test
  public void testConfiguredFieldValueNull() throws Exception {
    FieldMapper cf = new ConfiguredField(FieldMapper.Field.applicationIdData.toString(), null);
    cf.setConvertNull(true);
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    cf.copy(msg, mq);
    assertEquals("", mq.applicationIdData);
  }

  @Test
  public void testInvalidConfiguredField() throws Exception {
    FieldMapper cf = new ConfiguredField("Doobrey", "Dum");
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      cf.copy(msg, mq);
      fail("No CoreException thrown for an invalid field");
    }
    catch (CoreException e) {
    }
  }

  @Test
  public void testConfiguredFieldToMqMessage() throws Exception {
    FieldMapper cf = new ConfiguredField(FieldMapper.Field.applicationIdData.toString(), ConfiguredField.class.getName());
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    cf.copy(msg, mq);
    assertEquals(ConfiguredField.class.getName(), mq.applicationIdData);
  }

  @Test
  public void testConfiguredFieldFromMqMessage() throws Exception {
    try {
      FieldMapper cf = new ConfiguredField(FieldMapper.Field.applicationIdData.toString(), ConfiguredField.class.getName());
      MQMessage mq = new MQMessage();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      cf.copy(mq, msg);
      fail("Exception expected");
    }
    catch (Exception e) {
      // expected.
    }
  }

  @Test
  public void testXpathFieldToMqMessage() throws Exception {
    FieldMapper cf = new XpathField(FieldMapper.Field.applicationIdData.toString(), DEST_XPATH);
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOC);
    cf.copy(msg, mq);
    assertEquals("value", mq.applicationIdData);
  }

  @Test
  public void testXpathFieldToMqMessage_NoResult() throws Exception {
    try {
      FieldMapper cf = new XpathField(FieldMapper.Field.groupId.toString(), null, new SimpleByteTranslator());
      MQMessage mq = new MQMessage();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOC);
      cf.copy(msg, mq);
      fail("Exception expected from SimpleByteTranslator");
    }
    catch (CoreException expected) {
      // expected;
    }
  }

  @Test
  public void testXpathFieldToMqMessage_NoResult_ConvertNull() throws Exception {
    FieldMapper cf = new XpathField(FieldMapper.Field.applicationIdData.toString(), null);
    cf.setConvertNull(true);
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_DOC);
    cf.copy(msg, mq);
    assertEquals("", mq.applicationIdData);
  }

  @Test
  public void testXpathFieldFromMqMessage() throws Exception {
    try {
      FieldMapper cf = new XpathField(FieldMapper.Field.applicationIdData.toString(), DEST_XPATH);
      MQMessage mq = new MQMessage();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      cf.copy(mq, msg);
      fail("Exception expected");
    }
    catch (Exception e) {
      // expected.
    }
  }

  @Test
  public void testMetadataFieldMapperToMqMessage() throws Exception {
    FieldMapper cf = new MetadataFieldMapper(FieldMapper.Field.applicationIdData.toString(), "metadataKey");
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("metadataKey", "metadataValue");
    cf.copy(msg, mq);
    assertEquals("metadataValue", mq.applicationIdData);
  }

  @Test
  public void testMetadataFieldMapperFromMqMessage() throws Exception {
    FieldMapper cf = new MetadataFieldMapper(FieldMapper.Field.applicationIdData.toString(), "metadataKey");
    MQMessage mq = new MQMessage();
    mq.applicationIdData = "someValue";
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    cf.copy(mq, msg);
    assertEquals("someValue", msg.getMetadataValue("metadataKey"));
  }

  @Test
  public void testMessageIdMapperToMqMessage() throws Exception {
    FieldMapper cf = new MessageIdMapper(FieldMapper.Field.applicationIdData.toString());
    MQMessage mq = new MQMessage();
    String uniq = new GuidGenerator().getUUID();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.setUniqueId(uniq);
    cf.copy(msg, mq);
    assertEquals(uniq, mq.applicationIdData);
  }

  @Test
  public void testMessageIdMapperFromMqMessage() throws Exception {
    FieldMapper cf = new MessageIdMapper(FieldMapper.Field.applicationIdData.toString());
    MQMessage mq = new MQMessage();
    String uniq = new GuidGenerator().getUUID();
    mq.applicationIdData = uniq;
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    cf.copy(mq, msg);
    assertEquals(uniq, msg.getUniqueId());
  }

  @Test
  public void testUuidMapperToMqMessage() throws Exception {
    FieldMapper cf = new UuidFieldMapper();
    cf.setMqFieldName("applicationIdData");
    MQMessage mq = new MQMessage();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    cf.copy(msg, mq);
    // Can't see what's written but at least no errors are thrown
  }

  @Test
  public void testUuidMapperFromMqMessage() throws Exception {
    FieldMapper cf = new UuidFieldMapper();
    cf.setMqFieldName("applicationIdData");
    MQMessage mq = new MQMessage();
    String uniq = new GuidGenerator().getUUID();
    mq.applicationIdData = uniq;
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      cf.copy(mq, msg);
      fail("CoreException not thrown when trying to write from Adaptris to MQ via UUID mapper");
    }
    catch (CoreException e) {
    }
  }

  private void set(MQMessage mqMsg, Field f) throws IOException {
    try {
      f.setMqField(mqMsg, STRING_CONTENT, null);
    }
    catch (IOException e) {
      if (e.getMessage().endsWith(Field.NO_BYTE_TRANSLATOR_ERROR)) {
        f.setMqField(mqMsg, STRING_CONTENT, new Base64ByteTranslator());
      }
    }
  }

  private void setInt(MQMessage mqMsg, Field f) throws IOException {
    try {
      f.setMqField(mqMsg, STRING_CONTENT, null);
    }
    catch (NumberFormatException e) {
      f.setMqField(mqMsg, NUMERIC_CONTENT, null);
    }
  }

  private void setVersion(MQMessage mqMsg, Field f) throws IOException {
    try {
      f.setMqField(mqMsg, STRING_CONTENT, null);
    }
    catch (IOException e) {
      f.setMqField(mqMsg, VERSION, null);
    }
  }

  private String get(MQMessage mqMsg, Field f) throws IOException {
    String msgTxt = null;
    try {
      msgTxt = f.getMqField(mqMsg, null);
    }
    catch (IOException e) {
      if (e.getMessage().endsWith(Field.NO_BYTE_TRANSLATOR_ERROR)) {
        msgTxt = f.getMqField(mqMsg, new Base64ByteTranslator());
      }
    }
    return msgTxt;
  }

}
