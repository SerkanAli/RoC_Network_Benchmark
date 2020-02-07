package RoC.NetworkProtocolsBench;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;

public class MQTTServer implements BaseServer, MqttCallback {


    MqttClient m_oClient;
    int m_nPort;
    String m_sTopic = "test/topic";
    WorkerRunnable.LatencyWriter m_oWriter;


    @Override
    public void SetPort(int nPort) {
        m_nPort = nPort;
    }

    @Override
    public void ListentoPort(WorkerRunnable.LatencyWriter oWriter) {

        try {
            m_oClient = new MqttClient("tcp://192.168.178.60:1883", "Sending");
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false); //no persistent sess// on
            connOpts.setMaxInflight(65000 );
            m_oClient.connect(connOpts);
            m_oClient.setCallback(this);
            m_oClient.subscribe(m_sTopic);

            System.out.println("Listening to Port MQTT ...");

            m_oWriter = new WorkerRunnable.LatencyWriter("MQTT"+ String.valueOf(BenchNetworkTime.GetCurrentTime()));
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean NeedLoop() {
        return false;
    }





    @Override
    public void connectionLost(Throwable cause) {


    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        long nBeginTime = 0;
        long nBenchID = 0;
        long nLatency = BenchNetworkTime.GetCurrentTime() - nBeginTime;
        for(int i = 0; i < 8; i++)
        {
            nBeginTime <<= 8;
            nBenchID <<= 8;
            nBeginTime |= (message.getPayload()[i] &0xFF);
            nBenchID |= (message.getPayload()[i+8] &0xFF);
        }
        //bFirstMsg = false;
        System.out.println("##########################");
        System.out.println("ID : "+nBenchID);
        m_oWriter.WriteResultstoFile("MQTT",nBenchID,  (BenchNetworkTime.GetCurrentTime() - nBeginTime), nLatency);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("+++++++Delivery Complete:++++++++++");
    }
}
