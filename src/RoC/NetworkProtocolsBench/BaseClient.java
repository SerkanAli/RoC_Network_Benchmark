package RoC.NetworkProtocolsBench;

import java.io.IOException;

public interface BaseClient {



     void SetPort(int nPort);
     void SetIPAdress(String sIP);

     String SendStringCreateNewConnection(String sData) throws IOException;

     void CreateConnection();
     String SendStringOverConnection(String sData) throws IOException;
     void CloseConnection();


}
