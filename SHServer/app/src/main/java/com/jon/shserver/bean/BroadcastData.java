package com.jon.shserver.bean;

public class BroadcastData {
    private String ip;
    private int port;
    private String strData;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }


    public BroadcastData(String ip, int port, String strData) {
        this.ip = ip;
        this.port = port;
        this.strData = strData;
    }

    public BroadcastData(String ip, int port, byte[] data) {
        this.ip = ip;
        this.port = port;
        if(data != null) {
            this.strData = new String(data, 0, data.length);
        }
    }

    public void appendStrData(String appendix) {
        if((strData != null)&&(appendix != null) ) {
            strData = strData + appendix;
        }
    }
}
