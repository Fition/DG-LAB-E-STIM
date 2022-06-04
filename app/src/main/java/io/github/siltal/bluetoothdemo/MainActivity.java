package io.github.siltal.bluetoothdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 3000;
    private final String deviceMark = "D-LAB ESTIM01";
    TextView textView;
    TextView targetDeviceInfo;
    ListView listView;
    BluetoothAdapter bluetoothAdapter;//获取蓝牙适配器
    LeScanCallback leScanCallback; //扫描结果回调
    Set<BluetoothDevice> bluetoothDevices = new HashSet<>(10);
    BluetoothDevice targetDevice;
    private Handler handler;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        textView = findViewById(R.id.tv_log);
        targetDeviceInfo = findViewById(R.id.tv_targetDeviceInfo);
        listView = findViewById(R.id.listView);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);//获取蓝牙管理器
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (!checkPermission())
            return;


        //初始化工作线程处理器
        handler = new Handler(Looper.getMainLooper());

        //设置扫描后的回调
        leScanCallback = new LeScanCallback() {
            @SuppressLint({"SetTextI18n"})
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //扫描到设备后的回调
                bluetoothDevices.add(device);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT);
                }
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                textView.append("\n" + device.getName() + " " + device.getAddress());
                if (device.getName() != null && device.getName().contains(deviceMark)) {
                    targetDevice = device;
                    // stopScan
                    scanLeDevice(false);
                    showScannedDevicesList();

                    targetDeviceInfo.setText("找到目标设备：" + targetDevice.getName() + " " + targetDevice.getAddress());
                    goActivity(GattConfig.class);

                }
            }

            private void showScannedDevicesList() {
                handler.post(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT);
                    }
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                    for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                        textView.append("\n" + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                    }
                });
            }
        };

        //查找 BLE 设备
        scanLeDevice(true);
    }

    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 扫描BLE设备
     *
     * @param enable 是否扫描
     */
    @SuppressLint("MissingPermission")
    public void scanLeDevice(boolean enable) {

        if (enable) {
            bluetoothAdapter.startLeScan(leScanCallback);
            textView.append("开始扫描\n");
            handler.postDelayed(() -> {
                //设置延迟执行此处
                bluetoothAdapter.stopLeScan(leScanCallback);
                textView.append("\n扫描结束\n");
                showToast("扫描结束");
//                goActivity(GattConfig.class);
            }, SCAN_PERIOD);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    public void goActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        GattConfig.bluetoothDevice=targetDevice;
        this.finish();
        startActivity(intent);
    }

    /**
     * 检查权限
     *
     * @return 是否有授权
     */

    private boolean checkPermission() {
        textView.append("检查权限\n");
        //检查设备是否支持蓝牙
        if (bluetoothAdapter == null) {
            textView.append("设备不支持蓝牙\n");
            return false;
        }
        textView.append("设备支持蓝牙\n");

        //检查是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            textView.append("设备不支持BLE\n");
            return false;
        }
        textView.append("设备支持BLE\n");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            textView.append("没有权限\n");
            Toast.makeText(this, "请授予权限:忽略", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    "android.permission.BLUETOOTH_CONNECT",
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, 1);
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        }
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        textView.append("有权限 ( Android11 无需检查此权限 Manifest.permission.BLUETOOTH_CONNECT )\n");

        //检查蓝牙是否打开
        if (!bluetoothAdapter.isEnabled()) {
            textView.append("蓝牙未打开\n");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }
        textView.append("蓝牙已打开\n");
        return true;

    }
}

