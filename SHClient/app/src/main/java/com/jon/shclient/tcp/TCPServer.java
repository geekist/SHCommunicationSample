package com.jon.shclient.tcp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.jon.shclient.tcp.TCPConstants.MSG_SERVER_ACCEPTED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SERVER_ACCEPTFAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SERVER_ACCEPTING;
import static com.jon.shclient.tcp.TCPConstants.MSG_SERVER_START_FAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_DISCONNECTED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVEDATA;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVEFAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_RECEIVING;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_SENDED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_SENDFAILED;
import static com.jon.shclient.tcp.TCPConstants.MSG_SOCKET_SENDING;
import static com.jon.shclient.tcp.TCPConstants.MSG_SUBTHREAD_SEND;
import static com.jon.shclient.tcp.TCPConstants.STATE_ACCEPTED;
import static com.jon.shclient.tcp.TCPConstants.STATE_ACCEPTFAILED;
import static com.jon.shclient.tcp.TCPConstants.STATE_ACCEPTING;
import static com.jon.shclient.tcp.TCPConstants.STATE_CLOSED;
import static com.jon.shclient.tcp.TCPConstants.STATE_DISCONNECTED;
import static com.jon.shclient.tcp.TCPConstants.STATE_STARTFAILED;

public class TCPServer {
    private final String TAG = "TcpServer";

    private int port; //服务端端口
    private Handler handler;

    private ServerSocket serverSocket;
    public Socket socket;
    private InputStream mInStream;
    private OutputStream mOutStream;

    private SocketAcceptThread acceptThread;
    private SocketReceiveThread receiveThread;

    private HandlerThread handlerThread;
    private Handler subThreadHandler;

    private int socketConnectState = STATE_DISCONNECTED;

    private static TCPServer instance;

    public static TCPServer getInstance() {
        if (instance == null) {
            synchronized (TCPServer.class) {
                if (instance == null) {
                    instance = new TCPServer();
                }
            }
        }
        return instance;
    }

    private TCPServer() {
        initHandlerThread();
    }

    private void initHandlerThread() {
        handlerThread = new HandlerThread("handler_thread");
        handlerThread.start();

        Looper loop = handlerThread.getLooper();
        subThreadHandler = new Handler(loop) {
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

    public void setPort(int port) {
        this.port = port;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void startServer() {
        if (socketConnectState != STATE_CLOSED) {
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            socketConnectState = STATE_STARTFAILED;
            handler.sendEmptyMessage(MSG_SERVER_START_FAILED);
            return;
        }

        acceptThread = new SocketAcceptThread();
        acceptThread.start();
    }

    private void send(byte[] data) {
        if (socket == null) {
            //  Toast.makeText(this, "没有客户端连接", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((data == null) || (data.length == 0)) {
            return;
        }
        Message msg = new Message();
        msg.what = MSG_SUBTHREAD_SEND;
        msg.obj = data;
        subThreadHandler.sendMessage(msg);
    }

    public void stopServer() {
        closeConnect();
        closeAccept();
    }

    public void destory() {
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }
    }

    class SocketAcceptThread extends Thread {
        @Override
        public void run() {
            try {
                socketConnectState = STATE_ACCEPTING;
                handler.sendEmptyMessage(MSG_SERVER_ACCEPTING);

                //等待客户端的连接，Accept会阻塞，直到建立连接，
                //所以需要放在子线程中运行。
                socket = serverSocket.accept();
                mInStream = socket.getInputStream();
                mOutStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                socketConnectState = STATE_ACCEPTFAILED;
                handler.sendEmptyMessage(MSG_SERVER_ACCEPTFAILED);
                return;
            }
            Log.i(TAG, "accept success");
            socketConnectState = STATE_ACCEPTED;
            handler.sendEmptyMessage(MSG_SERVER_ACCEPTED);
        }
    }

    private void closeConnect() {
        try {
            if (mOutStream != null) {
                mOutStream.close();
            }
            if (mInStream != null) {
                mInStream.close();
            }
            if (socket != null) {
                socket.close();  //关闭socket
                socket = null;
            }

            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (receiveThread != null) {
            receiveThread.threadExit();
            receiveThread = null;
        }
        socketConnectState = STATE_DISCONNECTED;
        handler.sendEmptyMessage(MSG_SOCKET_DISCONNECTED);
    }

    private void closeAccept() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                try { //读取数据，返回值表示读到的数据长度。-1表示结束
                    handler.sendEmptyMessage(MSG_SOCKET_RECEIVING);

                    int count = mInStream.read(buffer);
                    if (count == -1) {
                        Log.i(TAG, "read read -1");
                        handler.sendEmptyMessage(MSG_SOCKET_RECEIVED);
                        break;
                    } else {
                        String receiveData;
                        receiveData = new String(buffer, 0, count);
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