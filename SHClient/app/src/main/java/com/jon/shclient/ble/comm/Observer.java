package com.jon.shclient.ble.comm;


import com.jon.blelib.data.BleDevice;

public interface Observer {

    void disConnected(BleDevice bleDevice);
}
