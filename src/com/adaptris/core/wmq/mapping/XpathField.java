package com.adaptris.core.wmq.mapping;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.adaptris.annotation.MarshallingImperative;

import static com.adaptris.core.util.XmlHelper.createXmlUtils;

import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.text.ByteTranslator;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;

/**
 * Resolve an XPath on the AdaptrisMessage payload and use that value as a
 * MQMessage field.
 ** <p>
 * In the adapter configuration file this class is aliased as <b>xpath-field</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 *
 */
@XStreamAlias("xpath-field")
@MarshallingImperative(mapTo="xpath-field", transientFields={})
public class XpathField extends FieldMapper {

  private String xpath;

  public XpathField() {
    super();
  }

  /**
   *
   * @param mqField The MQ Field name
   * @param xpath the XPath
   */
  public XpathField(String mqField, String xpath) {
    this(mqField, xpath, null);
  }

  /**
   *
   * @param mqField the MQ Field name
   * @param xpath the xpath
   * @param bt the byte translator.
   */
  public XpathField(String mqField, String xpath, ByteTranslator bt) {
    this();
    setMqFieldName(mqField);
    setXpath(xpath);
    setByteTranslator(bt);
  }

  @Override
  public void copy(AdaptrisMessage msg, MQMessage mqMsg) throws IOException,
      MQException, CoreException {
    try {
      XmlUtils xml = createXmlUtils(msg);
      String xpathValue = xml.getSingleTextItem(getXpath());
      if (xpathValue == null && getConvertNull()) {
        logR.trace("Converting null value for " + getXpath() + " to \"\"");
        xpathValue = "";
      }
      logR.trace("Setting [" + xpathValue + "] as field [" + getMqFieldName()
          + "]");
      createField(getMqFieldName()).setMqField(mqMsg, xpathValue, getByteTranslator());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }


  /**
   *
   * @see com.adaptris.core.wmq.mapping.FieldMapper#copy(com.ibm.mq.MQMessage,
   *      com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void copy(MQMessage mqMsg, AdaptrisMessage msg) throws IOException,
      MQException, CoreException {
    throw new CoreException(
        this.getClass()
            + " may not be used to copy MQMessage fields to AdaptrisMessage");
  }

  /**
   * Return the XPath expression.
   *
   * @return the value
   */
  public String getXpath() {
    return xpath;
  }

  /**
   * Set the XPath Expression.
   * <p>
   * If the XPath will resolve to more than 1 item, then any one of items is
   * used to populate the MQMessage field
   * </p>
   *
   * @param s the value to set
   */
  public void setXpath(String s) {
    xpath = s;
  }
}
