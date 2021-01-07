/*
 * $RCSfile: MQMessageFactoryTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/08/13 13:28:43 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.wmq.MQMessageFactory.MQMessageInstance;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;

public class MQMessageFactoryTest extends BaseCase {

  private static final String LINE_SEP = System.getProperty("line.separator");
  private static final String XML_DOC = "<root>" + LINE_SEP
      + "<document>value</document>" + LINE_SEP + "</root>" + LINE_SEP;

  @Mock private MQMessage mqMsg;
  @Mock private AdaptrisMessage adpMsg;

  private AutoCloseable closeable;

  @Before
  public void setUp() throws Exception {
    closeable = MockitoAnnotations.openMocks(this);

    when(mqMsg.readUTF()).thenReturn(XML_DOC);
    when(mqMsg.readStringOfByteLength(XML_DOC.length())).thenReturn(XML_DOC);
    int length = XML_DOC.getBytes().length;
    byte[] b = new byte[length];
    when(mqMsg.getDataLength()).thenReturn(length);
    doNothing().when(mqMsg).readFully(b);

    when(adpMsg.getStringPayload()).thenReturn(XML_DOC);
    when(adpMsg.getPayload()).thenReturn(XML_DOC.getBytes());
  }

  @After
  public void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  public void testCreate() throws Exception {
    MQMessageInstance[] instances = MQMessageInstance.values();
    for (int i = 0; i < instances.length; i++) {
      MQMessageFactory.create(instances[i].name());
    }

    MQMessageInstance instance = MQMessageFactory.create("Not an MQMessageInstance");
    assertEquals(MQMessageInstance.Text, instance);

    //Why not get 100% code coverage during testing
    new MQMessageFactory();
  }

  @Test
  public void testTextMessage() throws Exception {
    MQMessageInstance instance = MQMessageInstance.Text;
    MQMessage msg = instance.create();
    assertEquals(MQC.MQFMT_STRING, msg.format);
    //Original Test that doesn't work
    //instance.write(createMessage(), msg);
    //AdaptrisMessage writeTo = AdaptrisMessageFactory.create();
    //instance.write(msg, writeTo);
    //assertEquals(XML_DOC, writeTo.getStringPayload());

    instance.write(adpMsg, mqMsg);
    verify(mqMsg).writeUTF(XML_DOC);

    instance.write(mqMsg, adpMsg);
    verify(adpMsg).setStringPayload(XML_DOC, "UTF-8");
  }

  @Test
  public void testStringMessage() throws Exception {
    MQMessageInstance instance = MQMessageInstance.String;
    MQMessage msg = instance.create();
    assertEquals(MQC.MQFMT_STRING, msg.format);

    instance.write(adpMsg, mqMsg);
    verify(mqMsg).writeString(XML_DOC);

    instance.write(mqMsg, adpMsg);
    verify(adpMsg).setStringPayload(XML_DOC, adpMsg.getCharEncoding());
  }

  @Test
  public void testBytesMessage() throws Exception {
    MQMessageInstance instance = MQMessageInstance.Bytes;
    MQMessage msg = instance.create();
    assertEquals(MQC.MQFMT_NONE, msg.format);

    instance.write(adpMsg, mqMsg);
    verify(mqMsg).write(XML_DOC.getBytes());

    int length = XML_DOC.getBytes().length;
    byte[] b = new byte[length];

    instance.write(mqMsg, adpMsg);
    verify(adpMsg).setPayload(b);
  }

  @Test
  public void testObjectMessage() throws Exception {
    MQMessageInstance instance = MQMessageInstance.Object;
    MQMessage msg = instance.create();
    assertEquals("Object", msg.format);

    instance.write(adpMsg, mqMsg);
    verify(mqMsg).write(XML_DOC.getBytes());

    int length = XML_DOC.getBytes().length;
    byte[] b = new byte[length];

    instance.write(mqMsg, adpMsg);
    verify(adpMsg).setPayload(b);
  }

  private AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage result = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(XML_DOC);
    return result;
  }
}
