package com.adaptris.core.wmq;

import com.ibm.mq.MQMessage;

/**
 * Abstract implementation of {@link NativeErrorHandler}
 * 
 * @author amcgrath
 * @since 2.8.1
 */
public abstract class AbstractNativeErrorHandler implements NativeErrorHandler {

	/**
	 * A reference to the <code>NativeConsumer</code> object that this
	 * error handler has been configured for.
	 * With a reference to the parent consumer, we have access to the
	 * WMQ connection details and <code>MessageOptions</code> (which may be overridden).
	 */
	private transient NativeConsumer parentConsumer;

	@Override
	public abstract void onError(MQMessage message) throws Exception;

	public NativeConsumer retrieveParentConsumer() {
		return parentConsumer;
	}

	public void registerParentConsumer(NativeConsumer parentConsumer) {
		this.parentConsumer = parentConsumer;
	}

}
