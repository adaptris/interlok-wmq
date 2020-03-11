package com.adaptris.core.jms.wmq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("mq-session-property")
public class MqSessionProperty {

  @NotBlank
  private String propertyName;
  
  @NotNull
  private String value;
  
  @NotBlank
  @Pattern(regexp = "String|Integer|Long|Boolean")
  private String dataType;
  
  public MqSessionProperty() {
    
  }
  
  public MqSessionProperty(String propertyName, String value, String dataType) {
    this();
    this.setPropertyName(propertyName);
    this.setValue(value);
    this.setDataType(dataType);
  }

  public String getPropertyName() {
    return propertyName;
  }

  /**
   * See your WebsphereMQ documentation for available properties.
   * @param propertyName
   */
  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public String getValue() {
    return value;
  }

  /**
   * See your WebsphereMQ documentation for appropriate values for your property.
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

  public String getDataType() {
    return dataType;
  }

  /**
   * WebsphereMQ Session properties require the data type; one of the following String|Integer|Long|Boolean.
   * @param dataType
   */
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }
  
  
}
