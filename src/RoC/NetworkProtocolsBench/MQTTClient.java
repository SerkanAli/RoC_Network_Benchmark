package RoC.NetworkProtocolsBench;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;

public class MQTTClient implements BaseClient{
    String m_sBroker = "tcp://localhost:1883";
    int m_nPort = 0;
    MqttClient m_oMqttClient;
    boolean m_bIsConnected = false;
    String m_sTopicName = "test/topic";

    @Override
    public void SetPort(int nPort) {
        m_nPort = nPort;
    }

    @Override
    public void SetIPAdress(String sIP) {
        m_sBroker = "tcp://" + sIP + ":1883";
    }

    @Override
    public void SendStringCreateNewConnection(String sData) throws IOException {

        int qos = 1;

        try {
            MqttClient mqttClient = new MqttClient(m_sBroker,String.valueOf(System.nanoTime()));

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); //no persistent session

            mqttClient.connect(connOpts);

            MqttMessage message = new MqttMessage(sData.getBytes());
            mqttClient.publish(m_sTopicName,message);

            mqttClient.close(true);
            
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void CreateConnection() {
        try {
            m_oMqttClient = new MqttClient(m_sBroker,String.valueOf(System.nanoTime()));

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true); //no persistent session
            m_oMqttClient.connect(connOpts);
            m_bIsConnected = true;

        } catch (MqttException e) {
            e.printStackTrace();
            m_bIsConnected = false;
        }
    }

    @Override
    public void SendStringOverConnection(String sData) throws IOException {
        if(!m_bIsConnected)
            return;
        MqttMessage message = new MqttMessage(sData.getBytes());
        try {
            m_oMqttClient.publish(m_sTopicName,message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void CloseConnection() {
        try {
            m_oMqttClient.close(true);
            m_bIsConnected = false;
        } catch (MqttException e) {
            e.printStackTrace();
            m_bIsConnected = true;
        }
    }

    @Override
    public String GetProtocolName() {
        return "MQTT";
    }

    @Override
    public long GetServerBeginTime() {
        return 0;
    }
}
