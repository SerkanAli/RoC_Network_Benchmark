package RoC.NetworkProtocolsBench;

import java.io.IOException;

public interface BaseClient {



     void SetPort(int nPort);
     void SetIPAdress(String sIP);

     void SendStringCreateNewConnection(String sData) throws IOException;

     void CreateConnection();
     void SendStringOverConnection(String sData) throws IOException;
     void CloseConnection();

     String GetProtocolName();

     long GetServerBeginTime();
}
