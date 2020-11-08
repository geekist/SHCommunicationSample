package com.jon.shserver.tcp;

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

import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_ACCEPTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_ACCEPTFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_ACCEPTING;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_STARTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_START_FAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_STOPPED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_DISCONNECTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVEDATA;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVEFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVING;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_SENDED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_SENDFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_SENDING;
import static com.jon.shserver.tcp.TCPConstants.STATE_CLOSED;
import static com.jon.shserver.tcp.TCPConstants.STATE_STARTED;

import static com.jon.shserver.tcp.TCPConstants.MSG_SUBTHREAD_READ;
import static com.jon.shserver.tcp.TCPConstants.MSG_SUBTHREAD_WRITE;


public class TCPServer {
    private final String TAG = "TcpServer";

    private int port; //服务端端口
    private Handler handler;

    private ServerSocket serverSocket;
    public Socket socket;
    private InputStream mInStream;
    private OutputStream mOutStream;

    private HandlerThread handlerThread;
    private Handler subThreadHandler;

    private Thread listenThread;

    private int socketConnectState = STATE_CLOSED;

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
//                    case MSG_SUBTHREAD_WRITE:
//                        writeToOutputStream((byte[]) msg.obj);
//                        break;
                    case MSG_SUBTHREAD_READ:
                        readFromInputStream();
                        String str = "how are you , I received from you!";
                        writeToOutputStream(str.getBytes());
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

    public volatile boolean exit = false;

    public void listen() {
        exit = false;
        if (listenThread != null) {
            stopListen();
        }

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"start");
                try {
                    Log.d(TAG,"socket");

                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                    socketConnectState = STATE_CLOSED;
                    handler.sendEmptyMessage(MSG_SERVER_START_FAILED);
                    return;
                }

                socketConnectState = STATE_STARTED;
                handler.sendEmptyMessage(MSG_SERVER_STARTED);

                while (!exit) {
                    try {
                        handler.sendEmptyMessage(MSG_SERVER_ACCEPTING);
                        socket = serverSocket.accept();
                        mInStream = socket.getInputStream();
                        mOutStream = socket.getOutputStream();

                        read();




                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(MSG_SERVER_ACCEPTFAILED);
                        return;
                    }
                    Log.i(TAG, "accept success");
                    handler.sendEmptyMessage(MSG_SERVER_ACCEPTED);
                }

            }
        });

        listenThread.start();

    }

    public synchronized void stopListen() {
        if (listenThread == null) return;
        handler.sendEmptyMessage(MSG_SERVER_STOPPED);

        exit = true;
        try {
            if (listenThread != null) {
                if(serverSocket != null){
                    serverSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // receiveThread.stop();
        listenThread = null;
    }

    public void write(byte[] data) {
        if (socket == null) {
            //  Toast.makeText(this, "没有客户端连接", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((data == null) || (data.length == 0)) {
            return;
        }
        Message msg = new Message();
        msg.what = MSG_SUBTHREAD_READ;
        msg.obj = data;
        subThreadHandler.sendMessage(msg);
    }

    public void read() {
        Message msg = new Message();
        msg.what = MSG_SUBTHREAD_READ;
        subThreadHandler.sendMessage(msg);
    }

    public void stopServer() {
        closeConnect();
    }

    public void destory() {
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
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
                socketConnectState = STATE_CLOSED;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.sendEmptyMessage(MSG_SOCKET_DISCONNECTED);
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


    public void readFromInputStream() {
        byte[] buffer = new byte[1024];
        try { //读取数据，返回值表示读到的数据长度。-1表示结束
            handler.sendEmptyMessage(MSG_SOCKET_RECEIVING);

            int count = mInStream.read(buffer);
            if (count == -1) {
                Log.i(TAG, "read read -1");
                handler.sendEmptyMessage(MSG_SOCKET_RECEIVED);
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


