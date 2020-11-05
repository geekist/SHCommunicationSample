package com.jon.shserver.mqtt;

public class DeviceInfo {

    public static final String PRODUCTKEY = "a1nSfZI6Eip";
    public static final String DEVICENAME = "phone1";
    public static final String DEVICESECRET = "c2b553aba4d4188cb1a4a439f8716d3e";

    //publish topic
    public static final String PUBLISH_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/phone";

    //subscribe topic
    public static final String SUBSCRIBE_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/temperature_info";

    public static final String DEVICE_HOST = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";
}
