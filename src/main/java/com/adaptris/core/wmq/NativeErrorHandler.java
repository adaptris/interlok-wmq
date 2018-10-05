package com.adaptris.core.wmq;

import com.ibm.mq.MQMessage;

/**
 * Handle errors from native WMQ when consuming messages.
 * 
 * @author amcgrath
 * @since 2.8.1
 */
public interface NativeErrorHandler {

	/**
	 * Accepts an <code>com.ibm.mq.MQMessage</code> message.
	 * This message has been consumed from the WMQ queue and cannot be translated.
	 * <code>onError</code> will be triggered when translation errors occur.
	 */
	void onError(MQMessage message) throws Exception;
}
