package com.jon.shclient.mqtt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jon.shclient.R;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import static com.jon.shclient.mqtt.AliMqttClient.MSG_CONNECTION_LOST;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_CONNECT_FAILED;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_CONNECT_SUCCESS;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_DELIVERY_COMPLETE;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_MESSAGE_ARRIVED;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_PUBLISH_FAILED;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_PUBLISH_SUCCESS;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_SUBSCRIBE_FAILED;
import static com.jon.shclient.mqtt.AliMqttClient.MSG_SUBSCRIBE_SUCCESS;
import static com.jon.shclient.mqtt.DeviceInfo.PUBLISH_TOPIC;
import static com.jon.shclient.mqtt.DeviceInfo.SUBSCRIBE_TOPIC;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MqttFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MqttFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MqttFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    AliMqttClient aliMqttClient;

    TextView textViewMessage;
    Button buttonConnect;
    Button buttonSend;
    Button buttonDisconnect;

    StringBuilder stringBuilder = new StringBuilder();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String tmpStr = "";
            switch (msg.what) {
                case MSG_CONNECTION_LOST:
                    tmpStr = "连接丢失！" + "\n";
                    break;
                case MSG_MESSAGE_ARRIVED:
                    tmpStr = "消息到达！" + "\n";
                    try {
                        MqttMessage message = (MqttMessage) msg.obj;
                        if ((message != null) && message.getPayload() != null) {
                            tmpStr += new String(message.getPayload()) + "\n";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    break;
                case MSG_DELIVERY_COMPLETE:
                    tmpStr = "发送完成" + "\n";

                    break;
                case MSG_CONNECT_SUCCESS:
                    tmpStr = "连接成功" + "\n";

                    break;
                case MSG_CONNECT_FAILED:
                    tmpStr = "连接失败" + "\n";

                    break;
                case MSG_SUBSCRIBE_SUCCESS:
                    tmpStr = "订阅成功" + "\n";
                    try {
                        String[] topics = (String[]) msg.obj;
                        if (topics != null) {
                            for(int i=0;i<topics.length;i++) {
                                tmpStr += topics[i] + "\n";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case MSG_SUBSCRIBE_FAILED:
                    tmpStr = "订阅失败" + "\n";

                    break;
                case MSG_PUBLISH_SUCCESS:
                    tmpStr = "发布成功" + "\n";
                    try {
                        MqttMessage message = (MqttMessage) msg.obj;
                        if( (message != null)&&(message.getPayload() != null) ) {
                            String payload = new String(message.getPayload());
                            tmpStr += payload + "\n";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case MSG_PUBLISH_FAILED:
                    tmpStr = "发布失败" + "\n";

                    break;

                default:
                    break;
            }

            stringBuilder.append(tmpStr);
            textViewMessage.setText(stringBuilder.toString());
        }
    };

    public MqttFragment() {


    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MqttFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MqttFragment newInstance(String param1, String param2) {
        MqttFragment fragment = new MqttFragment();
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
        View layout = inflater.inflate(R.layout.fragment_mqtt, container, false);
        textViewMessage = layout.findViewById(R.id.textView_message);
        textViewMessage.setMovementMethod(ScrollingMovementMethod.getInstance());


        buttonConnect = layout.findViewById(R.id.button_scan);
        buttonConnect.setOnClickListener(this);
        buttonSend = layout.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        buttonDisconnect = layout.findViewById(R.id.button_disconnect);
        buttonDisconnect.setOnClickListener(this);

        aliMqttClient = AliMqttClient.getInstance();
        aliMqttClient.setHandler(handler);
        //   test();

        return layout;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_scan:
                connect();
                break;
            case R.id.button_send:
                send();
                break;
            case R.id.button_disconnect:
                subscribe();
                break;
        }
    }

    public void connect() {
        aliMqttClient.connect(getActivity().getApplicationContext());
    }

    public void disconnect() {

    }

    public void send() {
        aliMqttClient.publishMessage(PUBLISH_TOPIC, getPayload());
    }

    public void subscribe() {
        aliMqttClient.subscribeTopic(SUBSCRIBE_TOPIC);
    }


    private String getPayload() {
        JSONObject params = new JSONObject();
        try {
            params.put("temperature", 24);
            params.put("humidity", 0.3);
            params.put("pm25", 98);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject payload = new JSONObject();
        try {
            payload.put("id", System.currentTimeMillis());
            payload.put("version", "1.0");
            payload.put("params", params);
            payload.put("method", "thing.event.property.post");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return payload.toString();
    }

}