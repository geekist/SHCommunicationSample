package com.jon.shclient.base;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class UdpHub {
    public interface OnSendCallback {
        void onSuccess(byte[] data);

        void onException(Exception e);

        void onTimeout();
    }

    public interface OnReceiveCallback {
        void onReceive(InetAddress address, byte[] data);

        void onException(Exception e);
    }

    private static final int UDP_RECEIVE_TIMEOUT = 10000; //milliseconds
    private OnSendCallback onSendCallback = null;
    private OnReceiveCallback onReceiveCallback = null;
    private DatagramSocket receiveSocket = null;
    private ReceiveThread receiveThread;

    private InetAddress remoteAddress; //收到消息后保存对方IP，用来发送消息
    private int sendPort;
    private int receivePort;

    private static UdpHub instance;

    public static UdpHub getInstance() {
        if (instance == null) {
            synchronized (UdpHub.class) {
                if (instance == null) {
                    instance = new UdpHub();
                }
            }
        }
        return instance;
    }

    private UdpHub() {

    }

    public UdpHub init(int sendPort, int ReceivePort) {
        this.sendPort = sendPort;
        this.receivePort = ReceivePort;
        return this;
    }

    public UdpHub setOnSendCallback(OnSendCallback onSendCallback) {
        this.onSendCallback = onSendCallback;
        return this;
    }

    public UdpHub setOnReceiveCallback(OnReceiveCallback onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
        return this;
    }

    public void startReceive() {
        try {
            if (receivePort < 0) {
                return;
            }
            stopReceive();

            receiveThread = new ReceiveThread();
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopReceive() {
        if (receiveThread != null) {
            receiveThread.stopThread();
            receiveThread = null;
        }
        if (receiveSocket != null) {
            receiveSocket.close();
            receiveSocket = null;
        }
    }

    public void broadcast(Context context, byte[] data) {
        try {
            String broadcastIp = getBroadcastIp(context);
            InetAddress ipAddress = InetAddress.getByName(broadcastIp);
            send(context, ipAddress, sendPort, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBack(Context context, byte[] data) {
        try {
            String ip = remoteAddress.getHostAddress();
            send(context, remoteAddress, sendPort, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send(Context context, InetAddress address, int port, byte[] data) {
        new Thread(new Runnable() {
            DatagramSocket socket = null;

            public void run() {
                try {

                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket = new DatagramSocket();
                    socket.setSoTimeout(UDP_RECEIVE_TIMEOUT);
                    socket.send(packet);

                    onSendCallback.onSuccess(data);
                } catch (SocketTimeoutException e) {
                    onSendCallback.onTimeout();
                } catch (Exception e) {
                    onSendCallback.onException(e);
                } finally {
                    socket.close();
                    socket = null;
                }
            }
        }).start();
    }

    private class ReceiveThread extends Thread {
        public volatile boolean exit = false;
        DatagramPacket packetReceive;

        public void stopThread() {
            exit = true;
        }

        public void run() {
            if(receiveSocket != null) {
                receiveSocket.close();
                receiveSocket = null;
            }
            try {
                receiveSocket = new DatagramSocket(receivePort);
                byte[] preparedBuffer = new byte[1024];
                packetReceive = new DatagramPacket(preparedBuffer, preparedBuffer.length);
            }catch(Exception e) {
                e.printStackTrace();
                return;
            }

            while (!exit) {
                try {
                    // receiveSocket.setSoTimeout(UDP_RECEIVE_TIMEOUT);

                    receiveSocket.receive(packetReceive);

                    byte[] receivedData = Arrays.copyOfRange(packetReceive.getData(),
                            packetReceive.getOffset(),
                            packetReceive.getOffset() + packetReceive.getLength());
                    InetAddress remoteAddress = packetReceive.getAddress();

                    UdpHub.this.remoteAddress = remoteAddress;
                    onReceiveCallback.onReceive(remoteAddress, receivedData);
                    packetReceive.setLength(1024);
                } catch (Exception e) {
                    onReceiveCallback.onException(e);
                }
            }
        }
    }

    /********----------------------------------------------------------------------------*********/
    //通过wifi得到广播地址，需要权限申明
    //context 需要application context 以避免内存泄漏
    public static String getBroadcastIp(Context mContext) throws SocketException {
        String ip = "";
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            String dhcpInfos = intToIp(dhcpInfo.netmask);
            String[] split = intToIp(ipAddress).split("\\.");
            ip = split[0] + "." + split[1] + "." + split[2] + "." + (255 - Integer.parseInt(dhcpInfos.split("\\.")[3]));//根据子网掩码获取广播的IP地址
        } else {
            String asd = getInfo();
            String[] split = asd.split(",");
            String ipStr = split[0];
            String NetMask = split[1];
            String[] split1 = ipStr.split("\\.");
            ip = split1[0] + "." + split1[1] + "." + split1[2] + "." + (255 - Integer.parseInt(NetMask.split("\\.")[3]));//根据子网掩码获取广播的IP地址
        }
        return ip;
    }

    //通过GPS获得IP地址，需要权限声明
    public static String getLocalIP2(Context context) {
        try {

            Enumeration<NetworkInterface> en2 = NetworkInterface.getNetworkInterfaces();
            NetworkInterface intf1 = en2.nextElement();
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static String getLocalIp(Context mContext) throws SocketException {
        String ip = "";
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            String dhcpInfos = intToIp(dhcpInfo.netmask);
            String[] split = intToIp(ipAddress).split("\\.");
            ip = split[0] + "." + split[1] + "." + split[2] + "." + split[3];
        } else {
            String asd = getInfo();
            String[] split = asd.split(",");
            String ipStr = split[0];
            String NetMask = split[1];
            String[] split1 = ipStr.split("\\.");
            ip = split1[0] + "." + split1[1] + "." + split1[2] + "." + split1[3];
        }
        return ip;
    }


    private static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }


    public static String getInfo() throws SocketException {
        String ipAddress = "";
        String maskAddress = "";

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            List<InterfaceAddress> mList = intf.getInterfaceAddresses();
            for (InterfaceAddress l : mList) {
                InetAddress inetAddress = l.getAddress();
                if (!inetAddress.isLoopbackAddress()) {
                    String hostAddress = inetAddress.getHostAddress();
                    if (hostAddress.indexOf(":") > 0) {
                        continue;
                    } else {
                        ipAddress = hostAddress;
                        maskAddress = calcMaskByPrefixLength(l.getNetworkPrefixLength());
                    }
                }
            }
        }
        return ipAddress + "," + maskAddress;
    }


    private static String calcMaskByPrefixLength(int length) {
        int mask = -1 << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }

}
