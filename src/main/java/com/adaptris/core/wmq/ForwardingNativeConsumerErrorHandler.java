package com.adaptris.core.wmq;

import com.adaptris.annotation.Removal;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ProduceDestination;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.ibm.mq.MQMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A <code>NativeErrorHandler</code> implementation for errors on consuming.
 * <p>
 * It is possible to encounter errors where the message read from the Websphere MQ queue cannot be
 * translated to an AdaptrisMessage. Possible causes for this include; incorrect mapping
 * configuration or an incorrect message type configuration.
 * </p>
 * <p>
 * In these cases the message will effectively be lost. We have consumed the message from WMQ, which
 * means the message will be removed from the queue, but seeing as we cannot translate the message
 * an exception will be thrown, but the message will be gone. This error handler fixes this very
 * niche problem. Any errors encountered, once the message has been consumed from WMQ will be
 * forwarded to this error handler.
 * </p>
 * <p>
 * The specific implementation of this error handler is to forward the MQMessage to any queue you
 * specify in configuration. NOTE: The connection details to the broker will be taken from the
 * parent consumer. You may only specify queue name to forward error messages to.
 * </p>
 * <p>
 * <code>MessageOptions</code> are required to connect to a queue and send messages. This options
 * will be taken from the parent consumer, although you may override them in the configuration for
 * this error handler. Note that options that specify the message type will be ignored as the
 * message is already available.
 * </p>
 *
 * @config wmq-forwarding-native-consumer-error-handler
 * @deprecated since 3.11.1 without replacement since IBM recommend you use JMS instead
 * @author amcgrath
 * @since 2.8.1
 */
@XStreamAlias("wmq-forwarding-native-consumer-error-handler")
@Deprecated
@ConfigDeprecated(removalVersion = "4.0.0", message = "IBM recommends using JMS instead",
    groups = Deprecated.class)
@Removal(version = "4.0.0")
public class ForwardingNativeConsumerErrorHandler extends AbstractNativeErrorHandler {

	private transient MQMessageProducer producer;

	/**
	 * The name of the queue to forward messages that we have consumed and
	 * cannot translate.
	 */
	private ProduceDestination destination;
	/**
	 * If you need to override the <code>MessageOptions</code> that the parent
	 * consumer has configured, you can do so here.
	 */
	private MessageOptions options;

	public ForwardingNativeConsumerErrorHandler() {
		producer = new MQMessageProducer();
	}

	/**
	 * Accepts an <code>com.ibm.mq.MQMessage</code> message.
	 * This message has been consumed from the WMQ queue and cannot be translated.
	 * <code>onError</code> will be triggered when translation errors occur.
	 */
	@Override
  public void onError(MQMessage message) throws Exception {
    producer.setConnection(retrieveParentConsumer().retrieveConnection(NativeConnection.class));
    if(getOptions() == null) {
      producer.setOptions(retrieveParentConsumer().getOptions());
    }
    else {
      producer.setOptions(getOptions());
    }
    producer.produce(message, getDestination().getDestination(DefaultMessageFactory.getDefaultInstance().newMessage()));
	}

	public ProduceDestination getDestination() {
		return destination;
	}

	public void setDestination(ProduceDestination destination) {
		this.destination = destination;
	}

	public MessageOptions getOptions() {
		return options;
	}

	public void setOptions(MessageOptions options) {
		this.options = options;
	}
}
