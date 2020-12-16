package com.jon.shclient.base;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


/**
 * 简单的TcpServer Demo，目前只支持一个client连接
 */
public class TCPServer {
    public interface OnStartCallback {
        void onStartSuccess();
        void onStartFailure();
    }

    public interface OnAcceptCallback {
        void onAcceptSuccess();

        void onAcceptFailure();
    }

    public interface OnSendCallback {
        void onSendSuccess();

        void onSendFailure();
    }

    public interface OnReceiveCallback {
        void onReceiveSuccess(String str);

        void onReceiveFailure();

        void onDisconnected();
    }

    private static final String TAG = TCPServer.class.getSimpleName();
    private static TCPServer instance;

    private int port;
    private int timeout = 2000;

    private OnStartCallback onStartCallback = null;
    private OnAcceptCallback onAcceptCallback = null;
    private OnReceiveCallback onReceiveCallback = null;
    private OnSendCallback onSendCallback = null;

    private ServerSocket serverSocket;
    private Socket acceptSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    SocketReceiveThread receiveThread;

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

    public TCPServer init(int port, int timeout) {
        this.port = port;
        this.timeout = timeout;
        return this;
    }

    public TCPServer setStartCallback(OnStartCallback onStartCallback) {
        this.onStartCallback = onStartCallback;
        return this;
    }

    public TCPServer setAcceptCallback(OnAcceptCallback onAcceptCallback) {
        this.onAcceptCallback = onAcceptCallback;
        return this;
    }

    public TCPServer setSendCallback(OnSendCallback onSendCallback) {
        this.onSendCallback = onSendCallback;
        return this;
    }

    public TCPServer setReceiveCallback(OnReceiveCallback onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
        return this;
    }

    public void startServer() {
        startServerInternal();
    }

    public void stopServer() {
        stopServerInternal();
    }

    public void disconnect() {
        disconnectFromClient();
    }

    public void sendData(byte[] data) {
        sendDataToClient(data);
    }


    /**---------------------------------------------------------------------------- **/
    private void startServerInternal() {
        if(!isParameterCorrect()) {
            return;
        }
        try {
            //开启服务、指定端口号
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (port <= 0) {
                        onAcceptCallback.onAcceptFailure();
                        return;
                    }

                    acceptSocket = serverSocket.accept();
                    inputStream = acceptSocket.getInputStream();
                    outputStream = acceptSocket.getOutputStream();
                    if (acceptSocket != null) {
                        outputStream = acceptSocket.getOutputStream();
                        inputStream = acceptSocket.getInputStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onAcceptCallback.onAcceptFailure();
                    return;
                }
                Log.i(TAG, "connect success");
                onAcceptCallback.onAcceptSuccess();

                receiveThread = new SocketReceiveThread();
                receiveThread.start();
            }
        }).start();

    }

    private void stopServerInternal() {
        disconnectFromClient();
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectFromClient() {
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
            if (acceptSocket != null) {
                acceptSocket.close();  //关闭socket
                acceptSocket = null;
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

    private void sendDataToClient(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (data == null || outputStream == null || acceptSocket == null) {
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
        return (port > 0)
                && (onStartCallback != null)
                && (onAcceptCallback != null)
                && (onReceiveCallback != null)
                && (onSendCallback != null);
    }
}
