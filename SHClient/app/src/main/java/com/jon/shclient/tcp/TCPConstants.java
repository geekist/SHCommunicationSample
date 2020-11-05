package com.jon.shclient.tcp;

public class TCPConstants {

    public TCPConstants() {

    }
    //server constant
    public static final int STATE_CLOSED = 1;
    public static final int STATE_STARTFAILED = 2;
    public static final int STATE_ACCEPTING = 3;
    public static final int STATE_ACCEPTED = 4;
    public static final int STATE_ACCEPTFAILED = 5;


    //both server and client
    public static final int STATE_DISCONNECTED = 3;


    //server message
    public static final int MSG_SERVER_START_FAILED = -1;
    public static final int MSG_SERVER_ACCEPTING = 1;
    public static final int MSG_SERVER_ACCEPTED = 2;
    public static final int MSG_SERVER_ACCEPTFAILED = 3;

    //both server and client
    public static final int MSG_SOCKET_INVALIDPARAMETER = -1;
    public static final int MSG_SOCKET_CONNECTING = 1;
    public static final int MSG_SOCKET_CONNECTED = 2;
    public static final int MSG_SOCKET_CONNECTFAILED = 3;
    public static final int MSG_SOCKET_DISCONNECTED = 4;

    public static final int MSG_SOCKET_SENDING = 5;
    public static final int MSG_SOCKET_SENDED = 6;
    public static final int MSG_SOCKET_SENDFAILED = 7;

    public static final int MSG_SOCKET_RECEIVING = 8;
    public static final int MSG_SOCKET_RECEIVEDATA = 9;

    public static final int MSG_SOCKET_RECEIVED = 10;
    public static final int MSG_SOCKET_RECEIVEFAILED = 11;

    public static final int MSG_SUBTHREAD_SEND = 3; //sub thread to send data

}
