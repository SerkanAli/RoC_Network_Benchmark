package RoC.NetworkProtocolsBench;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;

public class MQTTServer implements BaseServer, MqttCallback {


    MqttClient m_oClient;
    int m_nPort;
    String m_sTopic = "test/topic";
    @Override
    public void SetPort(int nPort) {
        m_nPort = nPort;
    }

    @Override
    public void ListentoPort() throws IOException {

        try {
           m_oClient = new MqttClient("tcp://192.168.178.60:1883", "Sending");
            m_oClient.connect();
            m_oClient.setCallback(this);
            m_oClient.subscribe(m_sTopic);

            System.out.println("Listening to Port ...");


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
