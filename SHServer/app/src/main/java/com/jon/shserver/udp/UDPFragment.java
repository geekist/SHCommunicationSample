package com.jon.shserver.udp;

import android.content.Context;
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

import androidx.fragment.app.Fragment;


import com.jon.shserver.R;
import com.jon.shserver.bean.BroadcastData;
import com.jon.shserver.tcp.IPUtils;

import java.lang.ref.WeakReference;

import static com.jon.shserver.udp.UdpSender.EXCEPTION;
import static com.jon.shserver.udp.UdpSender.RECEIVED;
import static com.jon.shserver.udp.UdpSender.SENDED;
import static com.jon.shserver.udp.UdpSender.STARTED;
import static com.jon.shserver.udp.UdpSender.STOPPED;
import static com.jon.shserver.udp.UdpSender.TIMEOUT;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UDPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UDPFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView textViewMessage;
    EditText editTextIP;
    EditText editTextPort;
    EditText editTextMessage;
    Button buttonSend;

    Button buttonStart;
    Button buttonStop;

    public StringBuilder textMessage;

    MyHandler handler;

    UdpSender sender;

    public UDPFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UDPFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UDPFragment newInstance(String param1, String param2) {
        UDPFragment fragment = new UDPFragment();
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
        View layout = inflater.inflate(R.layout.fragment_udp, container, false);

        textViewMessage = layout.findViewById(R.id.textView_display);
        textViewMessage.setMovementMethod(ScrollingMovementMethod.getInstance());

        // editTextIP = layout.findViewById(R.id.editText_ip);
        editTextPort = layout.findViewById(R.id.editText_port);
        editTextMessage = layout.findViewById(R.id.editText_message);

//        buttonStart = layout.findViewById(R.id.button_start);
//        buttonStart.setOnClickListener(this);
        buttonSend = layout.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
//        buttonStop = layout.findViewById(R.id.button_stop);
//        buttonStop.setOnClickListener(this);

        textMessage = new StringBuilder();

        handler = new MyHandler(this);

        sender = UdpSender.getInstance();
        sender.initialize(getContext(),handler,8900);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        sender.destory();
        sender = null;
    }

    @Override
    public void onClick(View v) {
        int port = Integer.parseInt(editTextPort.getText().toString());

        switch(v.getId()) {
            case R.id.button_start:
                //  sender.startListen(port);
                break;
            case R.id.button_send:
                sender.initialize(getContext(),handler,port);
                sendUDPMessage();

                break;
            case R.id.button_stop:
                sender.stopListen();
                break;
        }
    }

    //Handler静态内部类
    private static class MyHandler extends Handler {
        //弱引用
        WeakReference<UDPFragment> weakReference;

        public MyHandler(UDPFragment fragment) {
            weakReference = new WeakReference<UDPFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            UDPFragment fragment = weakReference.get();
            String strData = "";
            byte[] data;

            if (fragment != null) {
                switch (msg.what) {
                    case STARTED:
                        strData = "启动监听广播！\n";
                        fragment.textMessage.append(strData);
                        break;
                    case STOPPED:
                        strData = "停止监听广播！\n";
                        fragment.textMessage.append(strData);
                        break;
                    case SENDED:
                        strData = receivedString(fragment,msg);
                        fragment.textMessage.append(strData);
                        break;
                    case RECEIVED:
                        strData = receivedString(fragment,msg);
                        fragment.textMessage.append(strData);
                        break;
                    case TIMEOUT:
                        strData = receivedString(fragment,msg);
                        fragment.textMessage.append(strData);
                        break;
                    case EXCEPTION:
                        strData = receivedString(fragment,msg);
                        fragment.textMessage.append(strData);
                        break;
                }
                fragment.textViewMessage.setText(fragment.textMessage.toString());
            }
        }
    }



    private static String receivedString(Fragment fragment, Message msg) {
        BroadcastData broadcastData = (BroadcastData) msg.obj;
        String strMessage = "";
        switch (msg.what) {

            case SENDED:
                try {
                    strMessage = "\n[send:"
                            + IPUtils.getLocalIp(fragment.getContext())
                            + "]"
                            + " "
                            + broadcastData.getStrData()
                            + "\n";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case RECEIVED:
                String ip = broadcastData.getIp();
                try {
                  //  String broadcastIp = IPUtils.getBoardcastIp(fragment.getContext());
                   // if (ip.equals(broadcastIp))
                    strMessage = "\n[receive:" + ip
                            + "]"
                            + " "
                            + broadcastData.getStrData()
                            + "\n";
                }catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TIMEOUT:
                strMessage = "\n[send:"
                        + broadcastData.getPort()
                        + "]"
                        + "time out !!!"
                        + "\n";
                break;
            case EXCEPTION:
                strMessage = "\n[send:"
                        + broadcastData.getPort()
                        + "]"
                        + "exception !!!"
                        + "\n";
                break;
        }

        return strMessage;
    }

    private void sendUDPMessage() {
        /*
        String ip = "";
        try {
            ip = IPUtils.getIp(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        int port = Integer.parseInt(editTextPort.getText().toString());

        String localIp = "";
        try {
            localIp = IPUtils.getLocalIp(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = editTextMessage.getText().toString();
        byte[] data = message.getBytes();

        sender.sendData(port,data);
    }
}