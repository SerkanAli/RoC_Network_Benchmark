package RoC.NetworkProtocolsBench;

import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;

public interface BaseClient {



     void SetPort(int nPort);
     void SetIPAdress(String sIP);

     String SendStringCreateNewConnection(String sData) throws IOException;

     void CreateConnection();
     @Benchmark
     String SendStringOverConnection(String sData) throws IOException;
     void CloseConnection();

     long GetServerBeginTime();
}
