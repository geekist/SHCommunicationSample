package com.jon.shclient.mqtt;

public class DeviceInfo {

    public static final String PRODUCTKEY = "a1nSfZI6Eip";
    public static final String DEVICENAME = "phone2";
    public static final String DEVICESECRET = "2546e04dd5c5fa422e9afd38e9d6957d";

    //publish temperature
    public static final String PUBLISH_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/temperature";

    //subscribe phone info
    public static final String SUBSCRIBE_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/phone_info";

    public static final String DEVICE_HOST = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";

}
