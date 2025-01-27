package emuNet;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MyMqttClient {
	
    private static final int qos = 2;
    private static final String clientId = "Injection Tool";
    
    private String broker;
    private MemoryPersistence persistence;
    private MqttClient sampleClient;
    private MqttConnectOptions connOpts;
	
    

	
	public MyMqttClient(String brokerID) throws MqttException {
		this.persistence = new MemoryPersistence();
		this.broker = brokerID;
		try {
			sampleClient = new MqttClient(broker, clientId, persistence);
			connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
		} catch (MqttException me) {
			// TODO Something.... Give some geedback to the user
			//Remove the System.out
			System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            throw me;
		}
	}
	
	
	
	/**
	 * Establish a connection with the broker
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	public void connectToBroker() throws MqttSecurityException, MqttException {
		sampleClient.connect(connOpts);
		System.out.println("connected");
	}

	
	
	/**
	 * Send a message to the broker through a topic
	 * @param msg
	 * @param topic
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void sendMessage(String msg, String topic) throws MqttPersistenceException, MqttException {
		MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qos);
        sampleClient.publish(topic, message);
        System.out.println("message sended");
	}

	/**
	 * End the connection between the client and the broker
	 * @throws MqttException
	 */
	public void disconect() throws MqttException {
		sampleClient.disconnect();
		System.out.println("disconected");
	}

}
