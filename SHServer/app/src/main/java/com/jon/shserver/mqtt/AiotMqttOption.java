package com.jon.shserver.mqtt;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
    * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
    */
class AiotMqttOption {
    private String username = "";
    private String password = "";
    private String clientId = "";
    
    public String getUsername() { return this.username;}
    public String getPassword() { return this.password;}
    public String getClientId() { return this.clientId;}

    /**
        * 获取Mqtt建连选项对象
        * @param productKey 产品秘钥
        * @param deviceName 设备名称
        * @param deviceSecret 设备机密
        * @return AiotMqttOption对象或者NULL
        */
    public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
        if (productKey == null || deviceName == null || deviceSecret == null) {
            return null;
        }

        try {
            String timestamp = Long.toString(System.currentTimeMillis());

            // clientId
            this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
                    ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";

            // userName
            this.username = deviceName + "&" + productKey;

            // password
            String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
                    deviceName + "productKey" + productKey + "timestamp" + timestamp;
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
            mac.init(secretKeySpec);
            byte[] macRes = mac.doFinal(macSrc.getBytes());
            password = String.format("%064x", new BigInteger(1, macRes));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return this;
    }
}