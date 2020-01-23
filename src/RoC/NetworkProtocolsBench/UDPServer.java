package RoC.NetworkProtocolsBench;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements BaseServer
{
    private int m_nPort;
    @Override
    public void SetPort(int nPort) {m_nPort = nPort;}

    @Override
    public void ListentoPort() throws IOException{
        // Step 1 : Create a socket to listen at port 1234
        DatagramSocket ds = new DatagramSocket(m_nPort);
        byte[] receive = new byte[65535];

        DatagramPacket DpReceive = null;
        while (true)
        {
            System.out.println("Listen to port...");
            // Step 2 : create a DatgramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);

            // Step 3 : revieve the data in byte buffer.
            ds.receive(DpReceive);

            System.out.println("Client:-" + data(receive));

            // Exit the server if the client sends "bye"
            if (data(receive).toString().equals("bye"))
            {
                System.out.println("Client sent bye.....EXITING");
                break;
            }

            // Clear the buffer after every message.
            receive = new byte[65535];
        }
    }

    @Override
    public void ShutDownServer(){}


    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    } }
