package com.jon.shserver.tcp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.jon.shserver.R;

import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_ACCEPTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_ACCEPTFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_ACCEPTING;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_STARTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SERVER_START_FAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_CONNECTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_CONNECTFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_CONNECTING;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_DISCONNECTED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_INVALIDPARAMETER;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVEDATA;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVEFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_RECEIVING;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_SENDED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_SENDFAILED;
import static com.jon.shserver.tcp.TCPConstants.MSG_SOCKET_SENDING;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TcpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TcpFragment extends Fragment implements View.OnClickListener {

    TextView textViewMessage;
    TextView textViewIp;
    EditText editTextPort;
    EditText editTextMessage;
    Button buttonStartServer;
    Button buttonSend;
    Button buttonStopServer;

    TCPServer tcpServer = TCPServer.getInstance();

    private StringBuilder stringBuilder = new StringBuilder();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String tmpStr = "";
            switch (msg.what) {
                case MSG_SERVER_START_FAILED:
                    tmpStr = "服务启动失败！" + "\n";
                    break;

                case MSG_SERVER_STARTED:
                    tmpStr = "服务启动成功！" + "\n";
                    break;

                case MSG_SERVER_ACCEPTING:
                    tmpStr = "启动监听......" + "\n";
                    break;

                case MSG_SERVER_ACCEPTFAILED:
                    tmpStr = "连接失败！" + "\n";
                    break;
                case MSG_SERVER_ACCEPTED:
                    tmpStr = "连接成功！" + "\n";
                    break;

                case MSG_SOCKET_INVALIDPARAMETER:
                    tmpStr = "IP地址或端口号错误！" + "\n";
                    break;
                case MSG_SOCKET_CONNECTING:
                    tmpStr = "正在连接中......" + "\n";

                    break;
                case MSG_SOCKET_CONNECTED:
                    tmpStr = "已连接" + "\n";

                    break;
                case MSG_SOCKET_CONNECTFAILED:
                    tmpStr = "连接失败" + "\n";

                    break;
                case MSG_SOCKET_DISCONNECTED:
                    tmpStr = "连接已断开" + "\n";

                    break;
                case MSG_SOCKET_SENDING:
                    tmpStr = "发送数据中......" + "\n";

                    break;
                case MSG_SOCKET_SENDED:
                    tmpStr = "发送数据成功" + "\n";

                    break;
                case MSG_SOCKET_SENDFAILED:
                    tmpStr = "发送数据失败" + "\n";

                    break;
                case MSG_SOCKET_RECEIVING:
                    tmpStr = "接收数据中......" + "\n";

                    break;
                case MSG_SOCKET_RECEIVEDATA:
                    tmpStr = "接收数据块--" + "\n";

                    break;
                case MSG_SOCKET_RECEIVED:
                    tmpStr = "接收数据成功" + "\n";

                    break;
                case MSG_SOCKET_RECEIVEFAILED:
                    tmpStr = "接收数据失败" + "\n";

                    break;
                default:
                    break;
            }

            stringBuilder.append(tmpStr);
            textViewMessage.setText(stringBuilder.toString());
        }
    };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public TcpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TcpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TcpFragment newInstance(String param1, String param2) {
        TcpFragment fragment = new TcpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_tcp, container, false);

        textViewMessage = layout.findViewById(R.id.textView_message);
        textViewMessage.setMovementMethod(ScrollingMovementMethod.getInstance());

        textViewIp = layout.findViewById(R.id.textView_ipadress);
        editTextPort = layout.findViewById(R.id.editText_port);
        editTextMessage = layout.findViewById(R.id.editText_message);
        buttonStartServer = layout.findViewById(R.id.button_start);
        buttonStartServer.setOnClickListener(this);

        buttonSend = layout.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        buttonStopServer = layout.findViewById(R.id.button_stop);
        buttonStopServer.setOnClickListener(this);

        try {
            String ip = IPUtils.getLocalIP2(getContext());
            textViewIp.setText(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return layout;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                startServer();
                break;
            case R.id.button_send:
                send();
                break;
            case R.id.button_stop:
                stopServer();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tcpServer.destory();
        tcpServer = null;
    }


    private void startServer() {
       int port = 8080;
        String strPort = editTextPort.getText().toString();
        if(strPort == null || strPort.length() == 0) {
            Toast.makeText(getContext(),"port is null",Toast.LENGTH_LONG).show();
            return;
        }
        try{
            port = Integer.parseInt(strPort);
        }catch (Exception e) {
            e.printStackTrace();
        }

        //for test
        if(port >0) {
            tcpServer.setPort(port);
            tcpServer.setHandler(handler);
        }
        tcpServer.listen();
    }

    private void send() {
        /*
        String strData = editTextMessage.getText().toString();

        strData = "hello,world";
        byte[] data = strData.getBytes();
        tcpServer.send(data);*/
    }

    private void stopServer() {
        tcpServer.stopServer();
    }

}