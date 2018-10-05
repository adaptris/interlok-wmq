package com.adaptris.core.wmq.mapping;

import java.io.IOException;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.DateFormatUtil;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;

/**
 * Abstract base class for mapping MQMessage fields to AdaptrisMessage objects
 * and vice versa.
 * 
 * @author lchan
 * 
 */
public abstract class FieldMapper {

  /** Fields within a MQMessage.
   */
  public enum Field {

    accountingToken {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        msg.accountingToken = c.translate(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        return c.translate(msg.accountingToken);
      }

    },

    applicationIdData {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c) {
        msg.applicationIdData = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.applicationIdData;
      }

    },
    applicationOriginData {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c) {
        msg.applicationOriginData = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.applicationOriginData;
      }

    },    
    backoutCount {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c) {
        msg.backoutCount = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.backoutCount);
      }

    },
    characterSet {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c) {
        msg.characterSet = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.characterSet);
      }

    },

    correlationId {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        msg.correlationId = c.translate(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        return c.translate(msg.correlationId);
      }

    },
    encoding {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.encoding = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.encoding);
      }
    },
    expiry {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.expiry = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.expiry);
      }
    },
    feedback {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.feedback = Integer.valueOf(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.feedback);
      }
    },
    format {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.format = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.format;
      }
    },
    groupId {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }

        msg.groupId = c.translate(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        return c.translate(msg.groupId);
      }
    },
    messageFlags {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.messageFlags = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.messageFlags);
      }
    },
    messageId {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        msg.messageId = c.translate(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        if (c == null) {
          throw new IOException(name()
              + NO_BYTE_TRANSLATOR_ERROR);
        }
        return c.translate(msg.messageId);
      }
    },
    messageSequenceNumber {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.messageSequenceNumber = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.messageSequenceNumber);
      }
    },
    messageType {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.messageType = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.messageType);
      }
    },
    offset {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.offset = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.offset);
      }
    },
    originalLength {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.originalLength = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.originalLength);
      }
    },
    persistence {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.persistence = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.persistence);
      }
    },
    priority {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.priority = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.priority);
      }
    },
    putApplicationName {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.putApplicationName = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.putApplicationName;
      }
    },
    putApplicationType {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.putApplicationType = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.putApplicationType);
      }
    },
    putDateTime {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(DateFormatUtil.parse(value));
        msg.putDateTime = gc;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return DateFormatUtil.format(msg.putDateTime.getTime());
      }
    },    
    replyToQueueManagerName {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.replyToQueueManagerName = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.replyToQueueManagerName;
      }
    },
    replyToQueueName {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.replyToQueueName = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.replyToQueueName;
      }
    },
    report {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.report = Integer.parseInt(value);
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return String.valueOf(msg.report);
      }
    },
    userId {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        msg.userId = value;
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return msg.userId;
      }
    },
    version {
      @Override
      public void setMqField(MQMessage msg, String value, ByteTranslator c)
          throws IOException {
        try {
          int version = Integer.parseInt(value);
          msg.setVersion(version);
        } catch (Exception e) {
          throw new IOException("Failed to set version to " + value, e);
        } 
      }

      @Override
      public String getMqField(MQMessage msg, ByteTranslator c)
          throws IOException {
        return "" + msg.getVersion();
      }
    };
    
    public static final String NO_BYTE_TRANSLATOR_ERROR = " is a byte[] field, no ByteTranslator";
    
    /**
     * Map the value onto the specific mq-field.
     * 
     * @param msg the MQMessage whose fields need to be modified
     * @param value the value
     * @param c the byte translator, if appropriate
     * @throws IOException on error translating the fields.
     */
    public abstract void setMqField(MQMessage msg, String value,
                                    ByteTranslator c) throws IOException;

    /**
     * Get the MQMessage field and return it as a String.
     * 
     * @param msg the MQMessage
     * @param c the byte translator to be used if appropriate
     * @return the string representing the field.
     * @throws IOException on error translating the fields.
     */
    public abstract String getMqField(MQMessage msg, ByteTranslator c)
        throws IOException;

  }

  private String mqFieldName;
  private boolean convertNull;

  private ByteTranslator byteTranslator;
  protected transient Log logR;

  public FieldMapper() {
    logR = LogFactory.getLog(this.getClass());
  }

  /**
   * @return the mqFieldName
   */
  public String getMqFieldName() {
    return mqFieldName;
  }

  /**
   * The field name that will be mapped.
   * 
   * @param s the mqFieldName to set
   * @see com.ibm.mq.MQMD
   * @see Field
   */
  public void setMqFieldName(String s) {
    mqFieldName = s;
  }

  /**
   * Copy information from the AdaptrisMessage object into the MQMessage object.
   * 
   * @param msg the AdaptrisMessage Object.
   * @param mqMsg the MQMessage object.
   * @throws IOException wrapping any IOException
   * @throws MQException for any MQ related Exceptions
   * @throws CoreException for any framework related exceptions
   */
  public abstract void copy(AdaptrisMessage msg, MQMessage mqMsg)
      throws IOException, MQException, CoreException;

  /**
   * Copy information from the MQMessage object into the AdaptrisMessage object.
   * 
   * @param msg the AdaptrisMessage Object.
   * @param mqMsg the MQMessage object.
   * @throws IOException wrapping any IOException
   * @throws MQException for any MQ related Exceptions
   * @throws CoreException for any framework related exceptions
   */
  public abstract void copy(MQMessage mqMsg, AdaptrisMessage msg)
      throws IOException, MQException, CoreException;

  protected static Field createField(String fieldName) throws CoreException {

    Field result = null;
    try {
      result = Field.valueOf(fieldName);
    }
    catch (IllegalArgumentException e) {
      throw new CoreException(fieldName + " field is not currently supported");
    }
    return result;
  }

  /**
   * @return the ByteTranslator
   */
  public ByteTranslator getByteTranslator() {
    return byteTranslator;
  }

  /**
   * Set the ByteTranslator implementation to use when attempting to convert to
   * and from MQMessage fields that are byte[].
   * 
   * @param vt the ByteTranslator to set
   * @see ByteTranslator
   */
  public void setByteTranslator(ByteTranslator vt) {
    byteTranslator = vt;
  }

  /**
   * @return the convertNull
   */
  public boolean getConvertNull() {
    return convertNull;
  }

  /**
   * Whether or not to convert nulls into an empty string.
   * 
   * @param b the convertNull to set
   */
  public void setConvertNull(boolean b) {
    convertNull = b;
  }
}
