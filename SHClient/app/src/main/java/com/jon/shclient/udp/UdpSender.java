package com.jon.shclient.udp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.jon.shclient.bean.BroadcastData;
import com.jon.shclient.tcp.IPUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UdpSender {
    private static final String TAG = UdpSender.class.getSimpleName();
    private static final int UDP_RECEIVE_TIMEOUT = 10000; //miliseconds

    public static final int SENDED = 1001;
    public static final int RECEIVED = 1002;
    public static final int TIMEOUT = 1003;
    public static final int EXCEPTION = 1004;
    public static final int STARTED = 1005;
    public static final int STOPPED = 1006;


    private String ip;

    Handler handler;
    private Context context;
    private int port;

    private Thread receiveThread;
    private DatagramSocket receiveSocket;


    private static UdpSender instance;

    public static UdpSender getInstance() {
        if (instance == null) {
            synchronized (UdpSender.class) {
                if (instance == null) {
                    instance = new UdpSender();
                }
            }
        }
        return instance;
    }

    public void initialize(Context context, Handler handler, int port) {
        this.context = context;
        this.handler = handler;
        this.port = port;
        listen(port);
    }

    public void destory() {
        stopListen();
    }

    public void sendData(int port, byte[] data) {
        new Thread(new Runnable() {
            DatagramSocket socket;

            public void run() {
                try {
                    ip = IPUtils.getBoardcastIp(context);

                    InetAddress ipAddress = InetAddress.getByName(ip);
                    DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
                    socket = new DatagramSocket();
                    socket.setSoTimeout(UDP_RECEIVE_TIMEOUT);
                    socket.send(packet);
                    sendMessage(packet, SENDED, data);

                    byte[] data2 = new byte[1024];
                    DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
                    socket.receive(packet2);

                    String receiveIp = packet.getAddress().getHostAddress();
                    String localIp = IPUtils.getLocalIp(context);
                    if (!receiveIp.equals(localIp)) {
                        sendMessage(packet, RECEIVED, data2);
                        socket.close();
                    }
                } catch (SocketTimeoutException e) {
                    socket.close();
                    sendMessage(null, TIMEOUT, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(null, EXCEPTION, null);
                }
            }
        }).start();
    }

    public volatile boolean exit = false;

    private void listen(int port) {
        exit = false;
        if(receiveThread != null) {
            return;
        }
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    receiveSocket = new DatagramSocket(port);
                    sendMessage(null, STARTED, null);
                    while (!exit) {
                        byte[] data = new byte[1024];//
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        receiveSocket.receive(packet);//
                        String receiveIp = packet.getAddress().getHostAddress();
                        String localIp = IPUtils.getLocalIp(context);
                        if (!receiveIp.equals(localIp)) {  //mask local broadcast

                            sendMessage(packet, RECEIVED, data);
                            String info = new String(data, 0, packet.getLength());

                            //send message
                            InetAddress address = packet.getAddress();
                            int port = packet.getPort();
                            String strData = "how are you !: you say: [" + info + "]";
                            byte[] data2 = strData.getBytes();
                            DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);
                            receiveSocket.send(packet2);
                        }

                    }

                    receiveSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    //  sendMessage(null, EXCEPTION, null);
                }
            }
        });

        receiveThread.start();
    }

    public synchronized void stopListen() {
        if (receiveThread == null) return;
        sendMessage(null, STOPPED, null);

        exit = true;
        try {
            if (receiveThread != null) {
                receiveSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // receiveThread.stop();
        receiveThread = null;
    }


    private void sendMessage(DatagramPacket packet, int what, byte[] data) {
        String hostIP = "";
        if (packet != null) {
            hostIP = packet.getAddress().getHostAddress();
            // hostIP = address.getHostAddress();
        }
        Message message = new Message();
        message.what = what;

        BroadcastData broadcastData = new BroadcastData(hostIP, 0, data);
        message.obj = broadcastData;

        handler.sendMessage(message);
    }
}