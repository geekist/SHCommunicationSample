package com.jon.shclient.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.jon.shclient.bean.BroadcastData;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UPDSender {
    private static final int UDP_RECEIVE_TIMEOUT = 10000; //miliseconds

    public static final int SENDED = 1001;
    public static final int RECEIVED = 1002;
    public static final int TIMEOUT = 1003;
    public static final int EXCEPTION = 1004;

    private String ip;
    private int port;
    byte[] data;
    Handler handler;

    public UPDSender(String ip, int port, byte[] data, Handler handler) {
        this.ip = ip;
        this.port = port;
        this.data = data;
        this.handler = handler;
    }

    public UPDSender(Context context,int port, byte[] data, Handler handler) {
        try {
            ip = IPUtils.getBoardcastIp(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.port = port;
        this.data = data;
        this.handler = handler;
    }

    public void sendData() {
        new Thread(new Runnable() {
            DatagramSocket socket;

            public void run() {
                try {
                    InetAddress ipAddress = InetAddress.getByName(ip);
                    DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
                    socket = new DatagramSocket();
                    socket.setSoTimeout(UDP_RECEIVE_TIMEOUT);
                    socket.send(packet);

                    sendMessage(packet,SENDED, data);

                    byte[] data2 = new byte[1024];
                    DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
                    socket.receive(packet2);

                    sendMessage(packet,RECEIVED, data2);

                    socket.close();

                } catch (SocketTimeoutException e) {
                    socket.close();
                    sendMessage(null,TIMEOUT, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(null,EXCEPTION, null);
                }
            }
        }).start();
    }

    private void sendMessage(DatagramPacket packet,int what, byte[] data) {
        String hostIP = "";
        if(packet != null) {
            hostIP = packet.getAddress().toString();
           // hostIP = address.getHostAddress();
        }
        Message message = new Message();
        message.what = what;

        BroadcastData broadcastData = new BroadcastData(hostIP,port,data);
        message.obj = broadcastData;

        handler.sendMessage(message);
    }
}
