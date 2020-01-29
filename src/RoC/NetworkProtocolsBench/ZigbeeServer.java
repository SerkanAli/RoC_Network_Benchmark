package RoC.NetworkProtocolsBench;



import java.io.IOException;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.connection.serial.SerialPortRxTx;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee16BitAddress;


public class ZigbeeServer implements BaseServer {
    @Override
    public void SetPort(int nPort) {

    }

    @Override
    public void ListentoPort() throws IOException {
     //   SerialPortRxTx bka = new SerialPortRxTx("COM3", 9600);
        XBeeDevice myXBeeDevice = new XBeeDevice("COM3", 9600);

        try {
            myXBeeDevice.open();
            String sData = "hallo terminal";
            myXBeeDevice.sendBroadcastData(sData.getBytes());
        } catch (XBeeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ShutDownServer() {

    }

}
