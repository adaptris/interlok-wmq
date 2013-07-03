/*
 * $RCSfile: MQMessageFactory.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/07/14 09:57:10 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;

/**
 * MQMessageFactory for creating MQMessageInstance instances.
 * 
 * @author lchan
 */
public final class MQMessageFactory {

  /** Types of MQMEssage.
   */
  public enum MQMessageInstance {
    /** A Text (UTF-8) Message.
     * 
     */
    Text {
      @Override
      public MQMessage create() {
        MQMessage result = new MQMessage();
        result.format = MQC.MQFMT_STRING;
        return result;
      }

      @Override
      public void write(AdaptrisMessage msg, MQMessage mqMsg)
          throws IOException, MQException {
        mqMsg.writeUTF(msg.getStringPayload());
      }

      @Override
      public void write(MQMessage mqMsg, AdaptrisMessage msg)
          throws IOException, MQException {
        msg.setStringPayload(mqMsg.readUTF(), "UTF-8");
      }
    },
    /** A String Message.
     * 
     */
    String {
      @Override
      public MQMessage create() {
        MQMessage result = new MQMessage();
        result.format = MQC.MQFMT_STRING;
        return result;
      }

      @Override
      public void write(AdaptrisMessage msg, MQMessage mqMsg)
          throws IOException, MQException {
        mqMsg.writeString(msg.getStringPayload());
      }

      @Override
      public void write(MQMessage mqMsg, AdaptrisMessage msg)
          throws IOException, MQException {
        int strLen = mqMsg.getDataLength();
        msg.setStringPayload(mqMsg.readStringOfByteLength(strLen));
      }
    },
    /** A Bytes Message.
     * 
     */
    Bytes {
      @Override
      public MQMessage create() {
        MQMessage result = new MQMessage();
        result.format = MQC.MQFMT_NONE;
        return result;
      }

      @Override
      public void write(AdaptrisMessage msg, MQMessage mqMsg)
          throws IOException, MQException {
        mqMsg.write(msg.getPayload());
      }

      @Override
      public void write(MQMessage mqMsg, AdaptrisMessage msg)
          throws IOException, MQException {
        byte[] b = new byte[mqMsg.getDataLength()];
        mqMsg.readFully(b);
        msg.setPayload(b);
      }
    },
    /** An Object Message.
     * 
     */
    Object {
      @Override
      public MQMessage create() {
        MQMessage result = new MQMessage();
        result.format = "Object";
        return result;
      }

      @Override
      public void write(AdaptrisMessage msg, MQMessage mqMsg)
          throws IOException, MQException {
        mqMsg.write(msg.getPayload());
      }

      @Override
      public void write(MQMessage mqMsg, AdaptrisMessage msg)
          throws IOException, MQException {
        byte[] b = new byte[mqMsg.getDataLength()];
        mqMsg.readFully(b);
        msg.setPayload(b);
      }
    };
    /**
     * Create an MQMessage object.
     * 
     * @return an MQMessage
     */
    public abstract MQMessage create();

    /**
     * Write the contents of the AdaptrisMessage object to the MQMessage object.
     * 
     * @param msg the AdaptrisMessage Object
     * @param mqMsg the MQMessage Object
     * @throws IOException when writing to the buffer
     * @throws MQException upon MQ communication errors.
     */
    public abstract void write(AdaptrisMessage msg, MQMessage mqMsg)
        throws IOException, MQException;

    /**
     * Write the contents of the MQMessage object to the AdaptrisMessage object.
     * 
     * @param msg the AdaptrisMessage Object
     * @param mqMsg the MQMessage Object
     * @throws IOException when writing to the buffer
     * @throws MQException upon MQ communication errors.
     */
    public abstract void write(MQMessage mqMsg, AdaptrisMessage msg)
        throws IOException, MQException;
  }

  private static Log logR = LogFactory.getLog(MQMessageFactory.class);

  static MQMessageInstance create(String s) {
    MQMessageInstance result = null;
    for (MQMessageInstance sp : MQMessageInstance.values()) {
      if (s.equalsIgnoreCase(sp.toString())) {
        result = sp;
        break;
      }
    }
    if (result == null) {
      logR.warn(s + " is not supported, defaulting to "
          + MQMessageInstance.Text);
      result = MQMessageInstance.Text;
    }
    return result;
  }

}
