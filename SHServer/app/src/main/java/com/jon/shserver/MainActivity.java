package com.jon.shserver;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import com.jon.shserver.ble.BleFragment;
import com.jon.shserver.mqtt.MqttFragment;
import com.jon.shserver.tcp.TcpFragment;
import com.jon.shserver.udp.UDPFragment;

public class MainActivity extends AppCompatActivity {
    private final int DEFAULT_FRAGMENT = 0;
    private int lastFragmentIndex = 0;//用于记录上个选择的Fragment
    private static final String LAST_FRAGMENT_INDEX = "last_fragment_index";

    private static final String TCP = "tcp";
    private static final String UDP = "udp";
    private static final String BLE = "ble";
    private static final String MQTT = "mqtt";
    private static final String[] TAGS = {TCP, UDP, BLE, MQTT};

    private TcpFragment tcpFragment;
    private com.jon.shserver.udp.UDPFragment UDPFragment;
    BleFragment bleFragment;
    MqttFragment mqttFragment;
    private ArrayList<Fragment> fragments = new ArrayList<>(4);

    private BottomNavigationView bottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new OnSwitchListener());

        if (savedInstanceState == null) {
            lastFragmentIndex = DEFAULT_FRAGMENT; //默认为0
        } else {//内存被回收了，fragments的list也被回收了，重新add进去
            lastFragmentIndex = savedInstanceState.getInt(LAST_FRAGMENT_INDEX);
        }
        buildFragmentList();
        setDefaultFragment(lastFragmentIndex);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_FRAGMENT_INDEX, lastFragmentIndex);
    }

    class OnSwitchListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_tcp:
                    setTitle(TCP);
                    switchFragment(0);
                    return true;
                case R.id.navigation_udp:
                    setTitle(UDP);
                    switchFragment(1);
                    return true;
                case R.id.navigation_ble:
                    setTitle(BLE);
                    switchFragment(2);
                    return true;
                case R.id.navigation_mqtt:
                    setTitle(MQTT);
                    switchFragment(3);
                    return true;
            }
            return true;
        }
    }

    private void buildFragmentList() {
        tcpFragment = (TcpFragment) getSupportFragmentManager().findFragmentByTag(TAGS[0]);
        if (tcpFragment == null) {
            tcpFragment = new TcpFragment();
        }
        fragments.add(tcpFragment);

        UDPFragment = (UDPFragment) getSupportFragmentManager().findFragmentByTag(TAGS[1]);
        if (UDPFragment == null) {
            UDPFragment = new UDPFragment();
        }
        fragments.add(UDPFragment);

        bleFragment = (BleFragment) getSupportFragmentManager().findFragmentByTag(TAGS[2]);
        if (bleFragment == null) {
            bleFragment = new BleFragment();
        }
        fragments.add(bleFragment);

        mqttFragment = (MqttFragment) getSupportFragmentManager().findFragmentByTag(TAGS[3]);
        if (mqttFragment == null) {
            mqttFragment = new MqttFragment();
        }
        fragments.add(mqttFragment);
    }

    private void switchFragment(int pos) {
        if (pos == lastFragmentIndex) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment from = fragments.get(lastFragmentIndex);
        Fragment to = fragments.get(pos);
        if (!to.isAdded()) {
            transaction.hide(from)
                    .add(R.id.fragment_container, fragments.get(pos), TAGS[pos])
                    .commit();
        } else {
            transaction.hide(from)
                    .show(to)
                    .commit();
        }

        lastFragmentIndex = pos;
    }

    //设置默认
    private void setDefaultFragment(int pos) {
        Fragment now = fragments.get(pos);
        if (!now.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragments.get(lastFragmentIndex), TAGS[pos])
                    .show(fragments.get(lastFragmentIndex))
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(now)
                    .commit();
        }
        bottomNavigationView.getMenu().getItem(pos).setChecked(true);
        setTitle(TAGS[pos]);

    }

    public void setTitle(String tag) {
        if (getSupportActionBar() == null) {
            return;
        }
        String title = getPackageName();
        if (tag.equals(TCP)) {
            title = "TCPServer";
        } else if (tag.equals(UDP)) {
            title = "UDPBroadcast";
        } else if (tag.equals(BLE)) {
            title = "Bluetooth";
        } else if (tag.equals(MQTT)) {
            title = "MQTT";
        }

        getSupportActionBar().setTitle(title);

    }
}