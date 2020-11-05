package com.jon.shclient.tcp;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;


public class IPUtils {

    public static String getBoardcastIp(Context mContext) throws SocketException {
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