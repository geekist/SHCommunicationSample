package com.jon.shserver.network;

public class TCPConstants {

    public TCPConstants() {

    }
    //server constant
    public static final int STATE_CLOSED = 1;
    public static final int STATE_STARTFAILED = 2;
    public static final int STATE_STARTED = 3;

    public static final int STATE_ACCEPTING = 4;
    public static final int STATE_ACCEPTED = 5;
    public static final int STATE_ACCEPTFAILED = 6;


    //both server and client
    public static final int STATE_DISCONNECTED = 7;


    //server message
    public static final int MSG_SERVER_START_FAILED = 8;
    public static final int MSG_SERVER_STARTED = 8001;

    public static final int MSG_SERVER_ACCEPTING = 9;
    public static final int MSG_SERVER_ACCEPTED = 10;
    public static final int MSG_SERVER_ACCEPTFAILED = 11;

    //both server and client
    public static final int MSG_SOCKET_INVALIDPARAMETER = 12;
    public static final int MSG_SOCKET_CONNECTING = 13;
    public static final int MSG_SOCKET_CONNECTED = 14;
    public static final int MSG_SOCKET_CONNECTFAILED = 15;
    public static final int MSG_SOCKET_DISCONNECTED = 16;

    public static final int MSG_SOCKET_SENDING = 17;
    public static final int MSG_SOCKET_SENDED = 18;
    public static final int MSG_SOCKET_SENDFAILED = 19;

    public static final int MSG_SOCKET_RECEIVING = 20;
    public static final int MSG_SOCKET_RECEIVEDATA = 21;

    public static final int MSG_SOCKET_RECEIVED = 22;
    public static final int MSG_SOCKET_RECEIVEFAILED = 23;

    public static final int MSG_SUBTHREAD_SEND = 24; //sub thread to send data

}
