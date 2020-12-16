package com.jon.shclient.base;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TCPClient {
    public interface OnConnectCallback {
        void onConnectSuccess();

        void onConnectFailure();
    }

    public interface OnReceiveCallback {
        void onReceiveSuccess(String str);

        void onReceiveFailure();

        void onDisconnected();
    }

    public interface OnSendCallback {
        void onSendSuccess();

        void onSendFailure();
    }

    private static final String TAG = TCPClient.class.getSimpleName();
    private static TCPClient instance;

    private String ip;
    private int port;

    private int timeout = 2000;

    private OnConnectCallback onConnectCallback = null;
    private OnReceiveCallback onReceiveCallback = null;
    private OnSendCallback onSendCallback = null;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    SocketReceiveThread receiveThread;

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

    public TCPClient init(String ip, int port, int timeout) {
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
        return this;
    }

    public TCPClient setConnectCallback(OnConnectCallback onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
        return this;
    }

    public TCPClient setReceiveDataCallback(OnReceiveCallback onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
        return this;
    }

    public TCPClient setSendDataCallback(OnSendCallback onSendCallback) {
        this.onSendCallback = onSendCallback;
        return this;
    }

    public void connect() {
        connectToServer();
    }

    public void disconnect() {
        disconnectFromServer();
    }

    public void sendData(byte[] data) {
        sendDataToServer(data);
    }


    //**----------------------------private method--------------------------------------------**//
    private void connectToServer() {
        if (!isParameterCorrect()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //连接服务端，指定服务端ip地址和端口号。
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    //获取输出流、输入流
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    onConnectCallback.onConnectSuccess();

                    receiveThread = new SocketReceiveThread();
                    receiveThread.start();

                    onConnectCallback.onConnectSuccess();
                } catch (IOException e) {
                    onConnectCallback.onConnectFailure();
                }
            }
        }).start();
    }

    private void disconnectFromServer() {
        try {
            if (receiveThread != null) {
                receiveThread.threadExit();
                receiveThread = null;
            }
            if (outputStream != null) {
                outputStream.close(); //关闭输出流
                outputStream = null;
            }
            if (inputStream != null) {
                inputStream.close(); //关闭输入流
                inputStream = null;
            }
            if (socket != null) {
                socket.close();  //关闭socket
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SocketReceiveThread extends Thread {
        private boolean threadExit;

        public SocketReceiveThread() {
            Log.i(TAG, "new a receive thread");
            threadExit = false;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            while (!threadExit) {
                Log.i(TAG, "i am running");

                if (inputStream == null) {
                    threadExit = true;
                    return;
                }
                try {
                    //读取数据，返回值表示读到的数据长度。-1表示结束
                    int length = inputStream.read(buffer);//阻塞直到接收
                    if (length == -1) {
                        Log.i(TAG, "read read -1");
                        onReceiveCallback.onDisconnected();
                        break;
                    } else {
                        String receiveData = new String(buffer, 0, length);
                        Log.i(TAG, "read buffer:" + receiveData + ",count=" + length);
                        onReceiveCallback.onReceiveSuccess(receiveData);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    onReceiveCallback.onReceiveFailure();
                } catch (IOException e) {
                    Log.i(TAG, "read buffer:error");
                    e.printStackTrace();
                    onReceiveCallback.onReceiveFailure();
                }
            }
        }

        void threadExit() {
            threadExit = true;
        }
    }

    private void sendDataToServer(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (data == null || outputStream == null || socket == null) {
                    onSendCallback.onSendFailure();
                    return;
                }
                try {
                    outputStream.write(data);
                    outputStream.flush();
                    Log.i(TAG, "发送成功");
                    onSendCallback.onSendSuccess();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "发送失败");
                    onSendCallback.onSendFailure();
                }
            }
        }).start();
    }

    private boolean isParameterCorrect() {
        return (ip != null)
                && (port > 0)
                && (onConnectCallback != null)
                && (onReceiveCallback != null)
                && (onSendCallback != null);
    }
}
