/** WebsphereMQ specific JMS implementations of {@link
		com.adaptris.core.jms.VendorImplementation}.

  <p>In version 6.0 of WebSphere MQ and previous you may have to
    install APAR IY81774 which introduces a system property
    <strong>activateExceptionListener</strong> that should be set when starting the
    adapter.</p>
  <p>If this property is set, all exceptions resulting from a broken
    connection are sent to the exception listener, regardless of the
    context in which they occur. If this is not set, then broken
    connections may not trigger the standard javax.jms.ExceptionListener
    interface which means the adapter is not notified of a broken
    connection to WebsphereMQ and subsequently cannot recover from a
    broken connection to WebsphereMQ.</p>

	<p>
		By default, all JMS clients to MQ Series will create what is known as
		an MQRFH2 Header that will form part of the Websphere MQ message. This
		is used to store (amongst other things) some of the JMS headers that
		you wanted to preserve using {@link
		com.adaptris.core.jms.MessageTypeTranslatorImp#setMoveJmsHeaders(Boolean)},
		and all the custom JMS properties that you may have chosen to preserve
		from AdaptrisMessage metadata by configuring {@link
		com.adaptris.core.jms.MessageTypeTranslatorImp#setMoveMetadata(Boolean)}
		to be true.<strong>This means that the message format
			internally within WebpshereMQ is MQRFH2 and not MQSTR format</strong>.
		Accordingly the receiving application needs to be able to parse MQRFH2
		headers which may not be possible.
	</p>

	<p>
		If the MQRFH2 Header/format is not required or you need to change the
		message type to MQSTR, then you need to tell MQSeries to omit the
		MQRFH2 Header; this will mean that you'll lose all the JMS properties
		that are <a
			href="http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/topic/com.ibm.mq.csqzaw.doc/uj25460_.htm">mapped
			into MQRFH2 as standard</a> by MQSeries and also any custom JMS
		Properties that you might be sending. To omit the MQRFH2 header, then
		you need to add
		<code>?targetClient=1</code>
		after the queue name in your {@link
		com.adaptris.core.ProduceDestination} implementation. For example, if
		the queue that you need to produce to is called SampleQ1 then the
		string you need to use is <strong>queue:///SampleQ1?targetClient=1</strong>.
		More information about the mapping of JMS messages onto MQ Messages
		can be found <a
			href="http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/topic/com.ibm.mq.csqzaw.doc/uj25430_.htm">at
			this link</a>
	</p>
	<p>
		More generally speaking, the more powerful form of specifying a
		destination using uniform resource identifiers (URIs) is preferred.
		This form allows you to specify remote queues (queues on a queue
		manager other than the one to which you are connected). It also allows
		you to set the other properties contained in a com.ibm.mq.jms.MQQueue
		object. The URI for a queue begins with the sequence queue://,
		followed by the name of the queue manager on which the queue resides.
		This is followed by a further /, the name of the queue, and
		optionally, a list of name-value pairs that set the remaining Queue
		properties. For example: <strong>queue://Some_Other_Queue_Manager/SampleQ1?key1=value1&key2=value2</strong>.
		If you don't specify a queue manager in the URI then it is interpreted
		to be the queue manager to which you are currently connected to e.g. <strong>queue:///SampleQ1?key1=value1&key2=value2</strong>).
	</p>
	<p>Various name value pairs can be used as part of the URI; these
		include, but is not limited to (some of these values may have a direct
		correlation to standard JMS headers, if you specify both, then
		behaviour is dependent on the order of precedence that WebsphereMQ
		specifies) :
	<table border=1>
		<thead>
			<th>Property</th>
			<th>Description</th>
			<th>Values</th>
		</thead>
		<tr>
			<td>CCSID</td>
			<td>Character set of the destination</td>
			<td>integers - valid values listed in base WebSphere MQ
				documentation</td>
		</tr>
		<tr>
			<td>encoding</td>
			<td>How to represent numeric fields</td>
			<td>An integer value as described in the base WebSphere MQ
				documentation</td>
		</tr>
		<tr>
			<td>expiry</td>
			<td>Lifetime of the message in milliseconds</td>
			<td>0 for unlimited, positive integers for timeout (ms) - This
				might interfere with any TTL value you configure in the producer.</td>
		</tr>
		<tr>
			<td>multicast</td>
			<td>Sets multicast mode for direct connections</td>
			<td>-1=ASCF, 0=DISABLED, 3=NOTR, 5=RELIABLE, 7=ENABLED</td>
		</tr>
		<tr>
			<td>persistence</td>
			<td>Whether the message should be <i>hardened</i> to disk
			</td>
			<td>1=non-persistent, 2=persistent, -1=QDEF (use the queue
				definition)</td>
		</tr>
		<tr>
			<td>priority</td>
			<td>Priority of the message</td>
			<td>0 through 9, -1=QDEF (use the queue definition)- This might
				interfere with any priority value you configure in the producer.</td>
		</tr>
		<tr>
			<td>targetClient</td>
			<td>Whether the receiving application is JMS compliant</td>
			<td>0=JMS, 1=MQ</td>
		</tr>
	</table>
*/
package com.adaptris.core.jms.wmq;