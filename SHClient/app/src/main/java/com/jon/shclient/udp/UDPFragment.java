package com.jon.shclient.udp;

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

import com.jon.shclient.R;
import com.jon.shclient.bean.BroadcastData;
import com.jon.shclient.tcp.IPUtils;

import java.lang.ref.WeakReference;

import static com.jon.shclient.udp.UPDSender.EXCEPTION;
import static com.jon.shclient.udp.UPDSender.RECEIVED;
import static com.jon.shclient.udp.UPDSender.SENDED;
import static com.jon.shclient.udp.UPDSender.TIMEOUT;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UDPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UDPFragment extends Fragment {

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

    public StringBuilder textMessage;

    MyHandler handler;

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

        buttonSend = layout.findViewById(R.id.button_send);

        textMessage = new StringBuilder();

        handler = new MyHandler(this);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUDPMessage();
            }
        });

        return layout;
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
                    String appendix = " ---from:" + IPUtils.getLocalIp(fragment.getContext());
                    broadcastData.appendStrData(appendix);
                    strMessage = "\n[send:"
                            + broadcastData.getPort()
                            + "]"
                            + " "
                            + broadcastData.getStrData()
                            + "\n";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case RECEIVED:
                strMessage = "\n[receive:"
                        + broadcastData.getPort()
                        + "]"
                        + " "
                        + broadcastData.getStrData()
                        + "\n";
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

        UPDSender sender = new UPDSender(getContext(), port, data, handler);
        sender.sendData();
    }
}