/*
 * $RCSfile: MessageOptions.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/06/19 15:18:01 $
 * $Author: lchan $
 */
package com.adaptris.core.wmq;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.core.wmq.MQMessageFactory.MQMessageInstance;
import com.ibm.mq.MQC;
import com.ibm.mq.MQQueueManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Options handling queue access and MQMessage options when getting or putting messages on a MQQueue
 * 
 * <p>
 * The options can be set as an integer, or you can comma separate the option names. The options are directly copied from the
 * WebsphereMQ client API documentation.
 * </p>
 * 
 * @config wmq-message-options
 * 
 * @author lchan
 * 
 */
@XStreamAlias("wmq-message-options")
public class MessageOptions {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotBlank
  @AutoPopulated
	private String queueOpenOptions;
  @NotBlank
  @AutoPopulated
	private String queueCloseOptions;
  @NotBlank
  @AutoPopulated
	private String messageOptions;

  @NotNull
  @AutoPopulated
  private MQMessageInstance messageFormat;

	public MessageOptions() {
		setQueueOpenOptions("MQOO_INPUT_AS_Q_DEF,MQOO_OUTPUT,MQOO_BROWSE");
		setQueueCloseOptions("MQCO_NONE");
    setMessageFormat(MQMessageInstance.Text);
		setMessageOptions("MQPMO_NO_SYNCPOINT");
	}

	/**
	 * @return the openOptions
	 */
	public String getQueueOpenOptions() {
		return queueOpenOptions;
	}

	/**
	 * Set the open options on the queue when accessing the Queue.
	 * <p>
	 * Any or none of the following MQC fields may be used.
	 * If more than one option is required, the values can be combined
	 * by simply comma separating each string option.
	 * </p>
	 * <ul>
	 * <li>MQOO_INQUIRE</li>
	 * <li>MQOO_BROWSE</li>
	 * <li>MQOO_INPUT_AS_Q_DEF</li>
	 * <li>MQOO_INPUT_SHARED</li>
	 * <li>MQOO_INPUT_EXCLUSIVE</li>
	 * <li>MQOO_OUTPUT</li>
	 * <li>MQOO_SAVE_ALL_CONTEXT</li>
	 * <li>MQOO_PASS_IDENTITY_CONTEXT</li>
	 * <li>MQOO_PASS_ALL_CONTEXT</li>
	 * <li>MQOO_SET_IDENTITY_CONTEXT</li>
	 * <li>MQOO_SET_ALL_CONTEXT</li>
	 * <li>MQOO_ALTERNATE_USER_AUTHORITY</li>
	 * <li>MQOO_FAIL_IF_QUIESCING</li>
	 * </ul>
	 *
	 * @param i the openOptions to set. The default options are
	 *          MQOO_INPUT_AS_Q_DEF , MQOO_OUTPUT
	 * @see MQQueueManager#accessQueue(String, int)
	 * @see MQC
	 */
	public void setQueueOpenOptions(String i) {
		queueOpenOptions = i;
	}

	/**
	 * @return the closeOptions
	 */
	public String getQueueCloseOptions() {
		return queueCloseOptions;
	}

	/**
	 * Set the close options when closing the queue.
	 * <p>
	 * One of the following MQC fields may be used.
	 * </p>
	 * <ul>
	 * <li>MQCO_NONE - the default value</li>
	 * <li>MQCO_DELETE - permanent dynamic queues only</li>
	 * <li>MQCO_DELETE_PURGE - permanent dynamic queues only</li>
	 * </ul>
	 *
	 * @param i the closeOptions to set
	 * @see MQC#MQCO_NONE
	 * @see MQC#MQCO_DELETE
	 * @see MQC#MQCO_DELETE_PURGE
	 */
	public void setQueueCloseOptions(String i) {
		queueCloseOptions = i;
	}

	/**
	 * @return the messageFormat for the producer.
	 */
  public MQMessageInstance getMessageFormat() {
		return messageFormat;
	}

	/**
	 * Set the <code>MQMessage.format</code> field to the desired value.
	 * <p>
	 * Only Text, String, Bytes, and Object are supported. They correspond to the
	 * values MQC.MQFMT_STRING, MQC.MQFMT_STRING, MQC.MQFMT_NONE and "Object"
	 * respectively.
	 * <ul>
	 * <li><b>Text</b> implies UTF. </li>
	 * <li><b>String</b> relies on the character set of the message.</li>
	 * </ul>
	 * </p>
	 *
	 * @see MQMessageInstance
	 * @param s the messageType to set
	 */
  public void setMessageFormat(MQMessageInstance s) {
		messageFormat = s;
	}

	/**
	 * @return the messageOptions
	 */
	public String getMessageOptions() {
		return messageOptions;
	}

	/**
	 * Options that control the action of MQQueue.put() and MQQueue.get()
	 * <p>
	 * Any or none of the following MQC fields
	 * may be used.  If more than one option is required, the values can be combined
	 * by simply comma separating each option.
	 * </p>
	 * <p>
	 * If the context of the message options is part of a producer, then the
	 * following values have meaning.
	 * </p>
	 * <ul>
	 * <li>MQPMO_SYNCPOINT</li>
	 * <li>MQPMO_NO_SYNCPOINT - the default value</li>
	 * <li>MQPMO_NO_SYNCPOINT</li>
	 * <li>MQPMO_NO_CONTEXT</li>
	 * <li>MQPMO_DEFAULT_CONTEXT</li>
	 * <li>MQPMO_SET_IDENTITY_CONTEXT</li>
	 * <li>MQPMO_SET_ALL_CONTEXT</li>
	 * <li>MQPMO_FAIL_IF_QUIESCING</li>
	 * <li>MQPMO_NEW_MSG_ID</li>
	 * <li>MQPMO_NEW_CORREL_ID</li>
	 * <li>MQPMO_LOGICAL_ORDER</li>
	 * <li>MQPMO_ALTERNATE_USER_AUTHORITY</li>
	 * <li>MQPMO_RESOLVE_LOCAL_Q</li>
	 * </ul>
	 * <p>
	 * If the context of the message options is part of a consumer, then the
	 * following values have meaning.
	 * </p>
	 * <ul>
	 * <li>MQGMO_WAIT - default</li>
	 * <li>MQGMO_NO_WAIT</li>
	 * <li>MQGMO_SYNCPOINT</li>
	 * <li>MQGMO_NO_SYNCPOINT - default</li>
	 * <li>MQGMO_BROWSE_FIRST</li>
	 * <li>MQGMO_BROWSE_NEXT</li>
	 * <li>MQGMO_BROWSE_MSG_UNDER_CURSOR</li>
	 * <li>MQGMO_MSG_UNDER_CURSOR</li>
	 * <li>MQGMO_LOCK MQC.MQGMO_UNLOCK</li>
	 * <li>MQGMO_ACCEPT_TRUNCATED_MSG</li>
	 * <li>MQGMO_FAIL_IF_QUIESCING</li>
	 * <li>MQGMO_CONVERT</li>
	 * </ul>
	 *
	 * @param i the messageOptions to set
	 */
	public void setMessageOptions(String i) {
		messageOptions = i;
	}

  /**
   * Calculate the integer value of the options field.
   *
   * @return the integer value of the options.
   * @throws CoreException
   */
	public int messageOptionsIntValue() throws CoreException {
    log.trace("Converting [{}] into an int for message options", getMessageOptions());
    return calculateMqcFieldValues(getMessageOptions());
	}

  /**
   * Calculate the integer value of the options field.
   *
   * @return the integer value of the options.
   * @throws CoreException
   */
	public int queueOpenOptionsIntValue() throws CoreException {
    log.trace("Converting [{}] into an int for queue open options", getQueueOpenOptions());
		return calculateMqcFieldValues(getQueueOpenOptions());
	}

  /**
   * Calculate the integer value of the options field.
   *
   * @return the integer value of the options.
   * @throws CoreException
   */
	public int queueCloseOptionsIntValue() throws CoreException {
    log.trace("Converting [{}] into an int for queue close options", getQueueCloseOptions());
		return calculateMqcFieldValues(getQueueCloseOptions());
	}

	/**
	 * Check to see if an integer has been entered into the options field.
	 * If it has then simply return this value, otherwise calculate the options
	 * integer value, by resolving the MQC field names and adding them all together.
	 */
	private int calculateMqcFieldValues(String input) throws CoreException {
		int returnValue = 0;
		try {
			returnValue = Integer.parseInt(input);
		} catch (NumberFormatException ex) {
			try {
				String[] fields = tokinize(input);
				for(String field : fields) {
          log.trace("Trying to convert {} into something usable", field);
					returnValue += getMqcFieldValue(field);
				}
			} catch (Exception exc) {
				throw new CoreException("MQC Fields could not be calculated");
			}

		}
    log.trace("Calculated {}", returnValue);
		return returnValue;
	}

	private String[] tokinize(String input) {
		return input.split(",");
	}

	private int getMqcFieldValue(String fieldName)
			throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		Field mqcField = MQC.class.getDeclaredField(fieldName);
		return mqcField.getInt(null);
	}

  /**
   * Will add the value of the MQC field name given to the options.
   *
   * @param option the MQC field name
   * @throws CoreException
   */
  protected void addMessageOption(String option) throws CoreException {
		setMessageOptions(appendOption(getMessageOptions(), option));
	}

  /**
   * Will add the value of the MQC field name given to the options.
   *
   * @param option the MQC field name
   * @throws CoreException
   */
  protected void addQueueOpenOption(String option) throws CoreException {
		setQueueOpenOptions(appendOption(getQueueOpenOptions(), option));
	}

  /**
   * Will add the value of the MQC field name given to the options.
   *
   * @param option the MQC field name
   * @throws CoreException
   */
  protected void addQueueCloseOption(String option) throws CoreException {
		setQueueCloseOptions(appendOption(getQueueCloseOptions(), option));
	}

	private String appendOption(String originalOptions, String option) throws CoreException {
		try {
			int parseInt = Integer.parseInt(originalOptions);
			try {
				int mqcFieldValue = getMqcFieldValue(option);
				parseInt |= mqcFieldValue;

				return Integer.toString(parseInt);
			} catch (Exception ex) {
				throw new CoreException("Could not determine MQC field - " + option);
			}
		} catch (NumberFormatException ex) {
			String[] options = tokinize(originalOptions);
			int binarySearch = Arrays.binarySearch(options, option);
			StringBuilder stringBuilder = new StringBuilder();
			if(binarySearch < 0) {
				for(String opt : options) {
					stringBuilder.append(opt + ",");
				}
				stringBuilder.append(option);
			}
			else {
				return originalOptions;
			}

			return stringBuilder.toString();
		}
	}
}
