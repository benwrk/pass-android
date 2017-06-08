package co.bwsc.pass.networking;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Scanner;

import co.bwsc.pass.utility.NetworkUtility;

/**
 * Created by Ben on 5/2/2017.
 */

public class PassNetworking {
    public static final String DELIMITER = "/!!/";
    private static final String TAG = PassNetworking.class.getSimpleName();
    private static final String HANDSHAKE_SIGNATURE = "PASS_HANDSHAKE" + DELIMITER;
    private static final String HANDSHAKE_RESPONSE_SIGNATURE = "PASS_BCRESP" + DELIMITER;
    private static final short PASS_SERVER_PORT = 1256;
    private static final String ETHERNET_OPERSTATE_PATH = "/sys/class/net/eth0/operstate";
    private String serverAddress = null;

    public static String getActiveInterfaceMacAddress() {
        if (isEthernetUp()) {
            return NetworkUtility.getMACAddress("eth0");
        } else {
            return NetworkUtility.getMACAddress("wlan0");
        }
    }

    public static boolean isEthernetUp() {
        File ethernetStateFile = new File(ETHERNET_OPERSTATE_PATH);
        if (!ethernetStateFile.exists()) {
            return false;
        }
        try {
            Scanner ethernetStateScanner = new Scanner(ethernetStateFile);
            try {
                String ethernetState = ethernetStateScanner.nextLine();
                Log.d(TAG, "ethernetState: " + ethernetState);
                ethernetStateScanner.close();
                return ethernetState.equalsIgnoreCase("UP");
            } catch (NoSuchElementException e) {
                ethernetStateScanner.close();
                e.printStackTrace();
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String performHandshakeWithServer() {

        // BROADCASTING
        DatagramSocket c;
        String serverAddress = null;
        // Find the server using UDP broadcast
        try {
            // Open a random port to send the package
            c = new DatagramSocket();
            c.setBroadcast(true);

            String activeInterfaceMacAddress = getActiveInterfaceMacAddress().replace(":", "");
            byte[] sendData = (HANDSHAKE_SIGNATURE + activeInterfaceMacAddress + DELIMITER).getBytes();

            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), PASS_SERVER_PORT);
                c.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, PASS_SERVER_PORT);
                        c.send(sendPacket);
                    } catch (Exception e) {
                        Log.d(TAG, "Error in sending broadcast message!", e);
                    }
                }
            }


            // RESPONSE HANDLING

//            //Wait for a response
//            byte[] recvBuf = new byte[1024];
//            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
//            c.receive(receivePacket);
//
//            //We have a response
//            System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
//
//            //Check if the message is correct
//            String message = new String(receivePacket.getData()).trim();
//
//            if (message.toLowerCase().startsWith(HANDSHAKE_RESPONSE_SIGNATURE.toLowerCase())) {
//                serverAddress = receivePacket.getAddress().toString();
//                setServerAddress(serverAddress);
//            }
//
//            //Close the port!
//            c.close();

        } catch (IOException ex) {
            Log.d(getClass().getName(), "Error: ", ex);
        }
        return serverAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
