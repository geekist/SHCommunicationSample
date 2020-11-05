package com.jon.shclient.tcp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_CONNECTED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_CONNECTFAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_CONNECTING;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_DISCONNECTED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_INVALIDPARAMETER;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVEDATA;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVEFAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVING;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_SENDED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_SENDFAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_SENDING;
import static com.jon.shclient.tcp.TCPConstants.MSG_SUBTHREAD_SEND;
import static com.jon.shclient.tcp.TCPConstants.STATE_DISCONNECTED;

public class TCPClient {
    private final String TAG = "TcpClient3";

    private String strIp;
    private int port;
    private Handler handler;

    public Socket mSocket;

    private static final String CLIENT_MESSAGETXT = "client_msgtxt";

    private OutputStream mOutStream;
    private InputStream mInStream;
    private SocketConnectThread mConnectThread;
    private SocketReceiveThread mReceiveThread;

    private HandlerThread mHandlerThread;
    private Handler mSubThreadHandler;//子线程中的Handler实例。

    private int mSocketConnectState = STATE_DISCONNECTED;

    private static TCPClient instance;

    public static TCPClient getInstance() {
        if (instance == null) {
            synchronized (TCPClient.class) {
                if (instance == null) {
                    instance = new TCPClient();
                }
            }
        }
        return instance;
    }

    private TCPClient() {
        initHandlerThraed();
    }

    private void initHandlerThraed() {
        mHandlerThread = new HandlerThread("handler_thread");
        mHandlerThread.start();

        Looper loop = mHandlerThread.getLooper();
        mSubThreadHandler = new Handler(loop) {
            public void handleMessage(Message msg) {
                Log.i(TAG, "mSubThreadHandler handleMessage thread:" + Thread.currentThread());
                switch (msg.what) {
                    case MSG_SUBTHREAD_SEND:
                        writeToOutputStream((byte[]) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void setIP(String ip) {
        this.strIp = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void connect() {
        Log.i(TAG, "startConnect");
        if (strIp == null || strIp.length() == 0 || port <= 0) {
            handler.sendEmptyMessage(MSG_SOCKET_INVALIDPARAMETER);
            return;
        }

        if (mSocketConnectState != STATE_DISCONNECTED) {
            handler.sendEmptyMessage(MSG_SOCKET_CONNECTED);
            return;
        }

        mConnectThread = new SocketConnectThread();
        mConnectThread.start();
        handler.sendEmptyMessage(MSG_SOCKET_CONNECTING);
    }

    public void disConnect() {
        closeConnect();
    }

    public void detory() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }

    private void closeConnect() {
        try {
            if (mOutStream != null) {
                mOutStream.close(); //关闭输出流
                mOutStream = null;
            }
            if (mInStream != null) {
                mInStream.close(); //关闭输入流
                mInStream = null;
            }
            if (mSocket != null) {
                mSocket.close();  //关闭socket
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mReceiveThread != null) {
            mReceiveThread.threadExit();
            mReceiveThread = null;
        }
        mSocketConnectState = STATE_DISCONNECTED;
        handler.sendEmptyMessage(MSG_SOCKET_DISCONNECTED);
    }

    class SocketConnectThread extends Thread {
        public void run() {
            try {
                mSocket = new Socket(strIp, port);
                if (mSocket != null) {
                    mOutStream = mSocket.getOutputStream();
                    mInStream = mSocket.getInputStream();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mSocketConnectState = STATE_DISCONNECTED;
                handler.sendEmptyMessage(MSG_SOCKET_CONNECTFAILED);
                return;
            }
            Log.i(TAG, "connect success");

            handler.sendEmptyMessage(MSG_SOCKET_CONNECTED);
        }
    }

    public void send(byte[] data) {
        if (mSocket == null) {
            return;
        }

        if ((data == null) || (data.length == 0)) {
            return;
        }

        Message msg = new Message();
        msg.what = MSG_SUBTHREAD_SEND;
        msg.obj = data;
        mSubThreadHandler.sendMessage(msg);
    }

    private void writeToOutputStream(byte[] data) {
        if (data == null || data.length == 0 || mOutStream == null)
            return;
        try {
            handler.sendEmptyMessage(MSG_SOCKET_SENDING);
            mOutStream.write(data);
            mOutStream.flush();
            handler.sendEmptyMessage(MSG_SOCKET_SENDED);
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(MSG_SOCKET_SENDFAILED);
        }
    }

    class SocketReceiveThread extends Thread {
        private boolean threadExit = false;

        public void run() {
            byte[] buffer = new byte[1024];
            while (threadExit == false) {
                try {
                    //读取数据，返回值表示读到的数据长度。-1表示结束
                    handler.sendEmptyMessage(MSG_SOCKET_RECEIVING);

                    int count = mInStream.read(buffer);
                    if (count == -1) {
                        Log.i(TAG, "read read -1");
                        handler.sendEmptyMessage(MSG_SOCKET_RECEIVED);

                        disConnect();
                        break;
                    } else {
                        String receiveData = new String(buffer, 0, count);
                        Log.i(TAG, "read buffer:" + receiveData + ",count=" + count);
                        Message msg = new Message();
                        msg.what = MSG_SOCKET_RECEIVEDATA;
                        msg.obj = receiveData;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    handler.sendEmptyMessage(MSG_SOCKET_RECEIVEFAILED);
                    e.printStackTrace();
                }
            }
        }

        void threadExit() {
            threadExit = true;
        }
    }
}
