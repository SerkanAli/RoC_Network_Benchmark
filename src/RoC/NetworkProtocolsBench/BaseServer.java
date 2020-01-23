package RoC.NetworkProtocolsBench;

import java.io.IOException;

public interface BaseServer {

    void SetPort(int nPort);

    void ListentoPort() throws IOException;
    void ShutDownServer();
}
