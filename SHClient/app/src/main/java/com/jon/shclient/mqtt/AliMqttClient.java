package com.jon.shclient.mqtt;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.jon.shclient.mqtt.DeviceInfo.DEVICENAME;
import static com.jon.shclient.mqtt.DeviceInfo.DEVICESECRET;
import static com.jon.shclient.mqtt.DeviceInfo.DEVICE_HOST;
import static com.jon.shclient.mqtt.DeviceInfo.PRODUCTKEY;

public class AliMqttClient {
    private static final String TAG = "AliMqttClient";

    public static final int MSG_CONNECTION_LOST = 1;
    public static final int MSG_MESSAGE_ARRIVED = 2;

    public static final int MSG_DELIVERY_COMPLETE = 3;
    public static final int MSG_CONNECT_SUCCESS = 4;
    public static final int MSG_CONNECT_FAILED = 5;
    public static final int MSG_SUBSCRIBE_SUCCESS = 6;
    public static final int MSG_SUBSCRIBE_FAILED = 7;
    public static final int MSG_PUBLISH_SUCCESS = 8;
    public static final int MSG_PUBLISH_FAILED = 9;


    private String userName;
    private String passWord;
    private String clientId;

    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;

    private static AliMqttClient instance;

    private Handler handler;

    private AliMqttClient() {
        prepareClientInfo(PRODUCTKEY,DEVICENAME,DEVICESECRET);
    }

    public static AliMqttClient getInstance() {
        if (instance == null) {
            synchronized (AliMqttClient.class) {
                if (instance == null) {
                    instance = new AliMqttClient();
                }
            }
        }
        return instance;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void connect(Context context) {

        /* 创建MqttAndroidClient对象，并设置回调接口。 */
        mqttAndroidClient = new MqttAndroidClient(context, DEVICE_HOST, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
                handler.sendEmptyMessage(MSG_CONNECTION_LOST);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                Message msg = new Message();
                msg.what = MSG_MESSAGE_ARRIVED;
                msg.obj = message;
                handler.sendMessage(msg);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
                handler.sendEmptyMessage(MSG_DELIVERY_COMPLETE);

            }
        });

        /* 建立MQTT连接。 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");
                    handler.sendEmptyMessage(MSG_CONNECT_SUCCESS);

                    //subscribeTopic(SUB_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                    handler.sendEmptyMessage(MSG_CONNECT_FAILED);

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    String[] topics = asyncActionToken.getTopics();

                    Message msg = new Message();
                    msg.what = MSG_SUBSCRIBE_SUCCESS;
                    msg.obj = topics;
                    Log.i(TAG, "subscribed succeed");
                    handler.sendMessage(msg);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                    handler.sendEmptyMessage(MSG_SUBSCRIBE_FAILED);

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");

                    Message msg = new Message();
                    msg.what = MSG_PUBLISH_SUCCESS;
                    msg.obj = message;
                    handler.sendMessage(msg);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                    handler.sendEmptyMessage(MSG_PUBLISH_FAILED);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private void prepareClientInfo(String productKey,String deviceName,String deviceSecret) {
        /* 获取MQTT连接信息clientId、username、password。 */
        AiotMqttOption aiotMqttOption = new AiotMqttOption()
                .getMqttOption(productKey, deviceName, deviceSecret);
        if (aiotMqttOption == null) {
            Log.e(TAG, "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
            userName = aiotMqttOption.getUsername();
            passWord = aiotMqttOption.getPassword();
        }

        /* 创建MqttConnectOptions对象，并配置username和password。 */
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());
    }
}
