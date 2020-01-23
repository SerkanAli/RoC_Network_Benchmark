package RoC.NetworkProtocolsBench;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;

public class MQTTClient implements BaseClient{
    @Override
    public void SetPort(int nPort) {

    }

    @Override
    public void SetIPAdress(String sIP) {

    }

    @Override
    public String SendStringCreateNewConnection(String sData) throws IOException {
        String broker = "tcp://localhost:1883";
        String topicName = "test/topic";
        int qos = 1;

        try {
            MqttClient mqttClient = new MqttClient(broker,String.valueOf(System.nanoTime()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
//Mqtt ConnectOptions is used to set the additional features to mqtt message

        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true); //no persistent session
        connOpts.setKeepAliveInterval(1000);


        MqttMessage message = new MqttMessage("Ed Sheeran".getBytes());

        return null;
    }

    @Override
    public void CreateConnection() {

    }

    @Override
    public String SendStringOverConnection(String sData) throws IOException {
        return null;
    }

    @Override
    public void CloseConnection() {

    }
}
