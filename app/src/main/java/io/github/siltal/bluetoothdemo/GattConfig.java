package io.github.siltal.bluetoothdemo;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GattConfig extends AppCompatActivity {
    static BluetoothDevice bluetoothDevice;
    public int intWaveAX = 0;
    public int intWaveAY = 0;
    public int intWaveAZ = 0;
    public int intWaveBX = 0;
    public int intWaveBY = 0;
    public int intWaveBZ = 0;
    BluetoothGatt bluetoothGatt;
    TextView batteryLevel; //电池电量
    TextView deviceName; //设备名称
    int intPowerA = 0;//内部表示
    int intPowerB = 0; //内部表示
    Button setPowerA; //设置强度A的按钮
    Button setPowerB; //设置强度B的按钮
    EditText powerAValue; //强度A编辑
    EditText powerBValue; //强度B编辑
    Handler handler; // 异步处理
    TextView gotPowerA; // 从机器获取的强度A
    TextView gotPowerB; // 从机器获取的强度B
    Button waveHoldA; // 发送波形A
    Button waveHoldB; // 发送波形B
    EditText waveAXValue; // 波形AX编辑
    EditText waveAYValue; // 波形AY编辑
    EditText waveAZValue; // 波形AZ编辑
    EditText waveBXValue; // 波形BX编辑
    EditText waveBYValue; // 波形BY编辑
    EditText waveBZValue; // 波形BZ编辑

    boolean wavingA = false;
    boolean wavingB = false;
    String DG_BASE_UUID = "955A%X-0FE2-F5AA-A094-84B8D4F3E8AD";
    List<BluetoothGattCharacteristic> characteristicList = new ArrayList<>();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_config);

        batteryLevel = findViewById(R.id.batteryLevel);
        deviceName = findViewById(R.id.deviceName);
        setPowerA = findViewById(R.id.setPowerA);
        setPowerB = findViewById(R.id.setPowerB);
        powerAValue = findViewById(R.id.powerAValue);
        powerBValue = findViewById(R.id.powerBValue);
        gotPowerA = findViewById(R.id.gotPowerA);
        gotPowerB = findViewById(R.id.gotPowerB);
        waveHoldA = findViewById(R.id.waveHoldA);
        waveHoldB = findViewById(R.id.waveHoldB);
        waveAXValue = findViewById(R.id.waveAx);
        waveAYValue = findViewById(R.id.waveAy);
        waveAZValue = findViewById(R.id.waveAz);
        waveBXValue = findViewById(R.id.waveBx);
        waveBYValue = findViewById(R.id.waveBy);
        waveBZValue = findViewById(R.id.waveBz);


        if (bluetoothDevice == null)
            return;

        String address = bluetoothDevice.getAddress();
        handler = new Handler(Looper.getMainLooper());


        bluetoothGatt = bluetoothDevice.connectGatt(this, false, new BtTransfer(this));


        showToast("Connected");


    }

    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取蓝牙设备的电量
     */
    public void getBatteryLevel() {
        BluetoothGattCharacteristic characteristic = characteristicList.get(4);
        @SuppressLint("MissingPermission") boolean b = bluetoothGatt.readCharacteristic(characteristic);
        Log.i("battery", b + "");

    }

    /**
     * 获取名称
     */
    @SuppressLint("MissingPermission")
    public void getName() {
        String name = bluetoothDevice.getName();
        Log.i("name", name);
        handler.post(() -> deviceName.setText(name));
    }

    @SuppressLint("MissingPermission")
    public void setPowerAB(int pa, int pb) {
        /*
        00AAAAAA AAAAABBB BBBBBBBB
        BBBBBBBB AAAAABBB 00AAAAAA
         */
        if (pa != 0) pa = pa + 63;
        if (pb != 0) pb = pb + 63;
        byte[] bytes = new byte[3];
        bytes[0] = (byte) (pb % 256);
        bytes[1] = (byte) ((pb >> 8) + ((pa % 32) << 3));
        bytes[2] = (byte) (pa >> 5);


        BluetoothGattCharacteristic characteristic = characteristicList.get(8);
        characteristic.setValue(bytes);
        boolean b = bluetoothGatt.writeCharacteristic(characteristic);
        Log.i("setPowerAB", Arrays.toString(bytes) + b);
    }

    public void getPowerAB() {
        handler.post(() -> {
            BluetoothGattCharacteristic characteristic = characteristicList.get(8);
            @SuppressLint("MissingPermission") boolean b = bluetoothGatt.readCharacteristic(characteristic);
            Log.i("getPowerAB!!!", b + "");
        });

    }

    @SuppressLint("MissingPermission")
    public void setWaveA(int x, int y, int z) {
        /**
         *         [00000000,00000000,00000000]
         *         [0000ZZZZ,ZYYYYYYY,YYYXXXXX]
         */
        byte[] bytes = new byte[3];
        bytes[0] = (byte) (z >> 1);
        bytes[1] = (byte) ((z & 1) + (y >> 3));
        bytes[2] = (byte) (((y % 8) << 5) + (x % 32));
        BluetoothGattCharacteristic characteristic = characteristicList.get(10);
        characteristic.setValue(bytes);
        boolean b = bluetoothGatt.writeCharacteristic(characteristic);
        Log.i("setWaveA", b + "");


    }

    @SuppressLint("MissingPermission")
    public void setWaveB(int x, int y, int z) {
        /*
         *         [00000000,00000000,00000000]
         *         [0000ZZZZ,ZYYYYYYY,YYYXXXXX]
         *
         */
        byte[] bytes = new byte[3];
        bytes[0] = (byte) (z >> 1);
        bytes[1] = (byte) ((z & 1) + (y >> 3));
        bytes[2] = (byte) ((y % 8 << 5) + (x % 32));
        BluetoothGattCharacteristic characteristic = characteristicList.get(9);
        characteristic.setValue(bytes);
        boolean b = bluetoothGatt.writeCharacteristic(characteristic);
        Log.i("setWaveB", b + "");
    }
}


class BtTransfer extends BluetoothGattCallback {

    GattConfig gattConfig;

    public BtTransfer(GattConfig gattConfig) {
        this.gattConfig = gattConfig;

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == STATE_CONNECTED) {
            gatt.discoverServices();//发现远程设备提供的服务及其特征和描述符。
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            gattConfig.finish();
        }
    }

    //当服务发现完成时调用
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            Log.d(" service", service.getUuid().toString());
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                Log.d("  characteristic", characteristic.getUuid().toString());
                gattConfig.characteristicList.add(characteristic);
            }
        }
        gattConfig.getBatteryLevel();
        gattConfig.getName();
        View.OnClickListener onSetPowerAB = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strPowerA = gattConfig.powerAValue.getText().toString();
                strPowerA = strPowerA.equals("") ? "0" : strPowerA;
                String strPowerB = gattConfig.powerBValue.getText().toString();
                strPowerB = strPowerB.equals("") ? "0" : strPowerB;
                gattConfig.intPowerA = Integer.parseInt(strPowerA);
                gattConfig.intPowerB = Integer.parseInt(strPowerB);
                if (gattConfig.intPowerA >= 2048)
                    gattConfig.intPowerA = 2047;
                if (gattConfig.intPowerB >= 2048)
                    gattConfig.intPowerB = 2047;

                Log.e("onClick: ", String.valueOf(gattConfig.intPowerA));
                Log.e("onClick: ", String.valueOf(gattConfig.intPowerB));
                gattConfig.setPowerAB(gattConfig.intPowerA, gattConfig.intPowerB);
            }
        };
        gattConfig.setPowerA.setOnClickListener(onSetPowerAB);
        gattConfig.setPowerB.setOnClickListener(onSetPowerAB);
        gattConfig.waveHoldA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // TODO 可切换波形循环等模式
                        if (gattConfig.wavingA) {
                            try {
                                gattConfig.intWaveAX = Integer.parseInt(gattConfig.waveAXValue.getText().toString());
                                gattConfig.intWaveAY = Integer.parseInt(gattConfig.waveAYValue.getText().toString());
                                gattConfig.intWaveAZ = Integer.parseInt(gattConfig.waveAZValue.getText().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            gattConfig.setWaveA(gattConfig.intWaveAX, gattConfig.intWaveAY, gattConfig.intWaveAZ);
                            gattConfig.handler.postDelayed(this, 100);
                        }
                    }
                };
                if (!gattConfig.wavingA) {//开启
                    gattConfig.wavingA = true;
                    gattConfig.handler.post(runnable);
                    gattConfig.waveHoldA.setText("哒咩");
                } else {//关闭
                    gattConfig.wavingA = false;
                    gattConfig.handler.removeCallbacks(runnable);
                    gattConfig.waveHoldA.setText("电电");
                }
            }
        });
        gattConfig.waveHoldB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gattConfig.wavingB = !gattConfig.wavingB;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (gattConfig.wavingB) {
                            // TODO 可切换波形循环等模式
                            try {
                                gattConfig.intWaveBX = Integer.parseInt(gattConfig.waveBXValue.getText().toString());
                                gattConfig.intWaveBY = Integer.parseInt(gattConfig.waveBYValue.getText().toString());
                                gattConfig.intWaveBZ = Integer.parseInt(gattConfig.waveBZValue.getText().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            gattConfig.setWaveB(gattConfig.intWaveBX, gattConfig.intWaveBY, gattConfig.intWaveBZ);
                            gattConfig.handler.postDelayed(this, 100);
                        }
                    }
                };
                if (!gattConfig.wavingB) {//开启
                    gattConfig.wavingB = true;
                    gattConfig.handler.post(runnable);
                    gattConfig.waveHoldB.setText("哒咩");
                } else {//关闭
                    gattConfig.wavingB = false;
                    gattConfig.handler.removeCallbacks(runnable);
                    gattConfig.waveHoldB.setText("电电");
                }
            }
        });

        gattConfig.setPowerAB(gattConfig.intPowerA, gattConfig.intPowerB);

    }

    //接收到数据
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            byte[] value = characteristic.getValue();
            //case byte to int
            if (gattConfig.characteristicList.get(8).equals(characteristic)) {//PowerAB
                //BBBBBBBB AAAAABBB 00AAAAAA
                //00000000 00000000 00000010
                int pa = ((value[2] & 0x3F) << 5) + ((value[1] & 0xf8) >> 3);
                int pb = ((value[1] & 0x07) << 8) + (value[0] & 0xff);


                gattConfig.handler.postDelayed(() -> gattConfig.gotPowerA.setText(String.valueOf(Math.max(pa - 63, 0))), 10);
                gattConfig.handler.postDelayed(() -> gattConfig.gotPowerB.setText(String.valueOf(Math.max(pb - 63, 0))), 10);
            } else if (gattConfig.characteristicList.get(4).equals(characteristic)) {//电量
                for (byte b : value) {
                    gattConfig.handler.post(() -> gattConfig.batteryLevel.setText(String.valueOf(b)));
                }
            }
            Log.e("onCharacteristicRead: ", Arrays.toString(value));
            Log.e("onCharacteristicRead", characteristic.getUuid().toString() + ":" + characteristic.getStringValue(0));
        }


    }

    //写入数据
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (gattConfig.characteristicList.get(8).equals(characteristic)) {
                Log.e("onCharacteristicWrite: ", "PowerAB");
                gattConfig.getPowerAB();
            }
            for (byte b : characteristic.getValue()) {
                Log.e("onCharacteristicWrite: ", String.valueOf(b & 0xff));
            }

            Log.e("onCharacteristicWrite: ", characteristic.getUuid().toString() + ":" + characteristic.getStringValue(0));
        }
        Log.w("  characteristicW", characteristic.getUuid().toString() + characteristic.getStringValue(0));
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        Log.w("  characteristicC", characteristic.getUuid().toString());
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        Log.d("  descriptor", descriptor.getUuid().toString());
    }


}