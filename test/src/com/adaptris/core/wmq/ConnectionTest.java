package com.adaptris.core.wmq;

import static org.mockito.Mockito.doNothing;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.wmq.NativeConnection.WebsphereProperty;
import com.adaptris.util.KeyValuePair;
import com.ibm.mq.MQC;
import com.ibm.mq.MQQueueManager;

public class ConnectionTest extends TestCase{

	private AttachedConnection attConn;
	private DetachedConnection detConn;
	@Mock private MQQueueManager mqQueueManager;

	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		createAttachedConnection();
	}
	public void testConnections() throws Exception{
		//Check .init with various settings
    LifecycleHelper.init(attConn);
		attConn.setRedirectExceptionLogging(true);
    LifecycleHelper.init(attConn);

		//KeyValuePairSet of WebsphereProperties

		try{
      LifecycleHelper.start(attConn);
			fail("QueueManager values incorrect but exception not thrown");
		}
		catch(Exception e){
		}

		//Null Pointer thrown but exceptions should be caught and logged
    LifecycleHelper.stop(attConn);

		try{
			detConn.connect();
			fail("QueueManager values incorrect but exception not thrown");
		}
		catch(Exception e){
		}

		detConn.disconnect(null);

		doNothing().when(mqQueueManager).disconnect();
		detConn.disconnect(mqQueueManager);
	}
	public void testWebsphereProperties(){
		Hashtable<String, Integer> p = new Hashtable<String, Integer>();

		WebsphereProperty.CCSID.addTo(p, "1");
		WebsphereProperty.connectOptions.addTo(p, "2");
		WebsphereProperty.port.addTo(p, "3");
		WebsphereProperty.sslFipsRequired.addTo(p, "4");

		assertEquals(new Integer(1), p.get(MQC.CCSID_PROPERTY));
		assertEquals(new Integer(2), p.get(MQC.CONNECT_OPTIONS_PROPERTY));
		assertEquals(new Integer(3), p.get(MQC.PORT_PROPERTY));
		assertEquals(new Boolean(false), p.get(MQC.SSL_FIPS_REQUIRED_PROPERTY));

		WebsphereProperty.sslFipsRequired.addTo(p, "true");
		assertEquals(new Boolean(true), p.get(MQC.SSL_FIPS_REQUIRED_PROPERTY));
	}
	private void createAttachedConnection() {
		attConn = new AttachedConnection();
		attConn.setQueueManager("your_Q_Manager");
		attConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
		attConn.setWorkersFirstOnShutdown(true);
		attConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_NULL_MD5"));

		detConn = new DetachedConnection();
		detConn.setQueueManager("your_Q_Manager");
		detConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.CCSID_PROPERTY, "MyCCSID"));
		detConn.setWorkersFirstOnShutdown(true);
		detConn.getEnvironmentProperties().addKeyValuePair(new KeyValuePair(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_NULL_MD5"));
	}

}
