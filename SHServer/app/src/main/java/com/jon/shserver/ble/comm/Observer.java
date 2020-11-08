package com.jon.shserver.ble.comm;


import com.jon.blelib.data.BleDevice;

public interface Observer {

    void disConnected(BleDevice bleDevice);
}
