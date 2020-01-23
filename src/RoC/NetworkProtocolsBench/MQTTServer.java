package RoC.NetworkProtocolsBench;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;

public class MQTTServer implements BaseServer, MqttCallback {
    @Override
    public void SetPort(int nPort) {

    }

    @Override
    public void ListentoPort() throws IOException {

        try {
            //We're using eclipse paho library  so we've to go with MqttCallback
            MqttClient client = new MqttClient("tcp://localhost:1883","clientid");
            client.setCallback(this);
            MqttConnectOptions mqOptions=new MqttConnectOptions();
            mqOptions.setCleanSession(true);
            client.connect(mqOptions);      //connecting to broker
            client.subscribe("test/topic"); //subscribing to the topic name  test/topic
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void ShutDownServer() {

    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("message is : "+message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
