package com.neltech.blutoothconnecttask;

import static android.app.ProgressDialog.show;
import static android.bluetooth.BluetoothProfile.GATT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.neltech.blutoothconnecttask.databinding.ActivityBluetoothBinding;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivityBluetooth extends AppCompatActivity {

    private ActivityBluetoothBinding binding;
    private BluetoothAdapter bluetoothAdap;
    private BluetoothManager btManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBluetoothBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.BTDeviceConnect.setOnClickListener((view) -> {
            funCheckPermission();
        });

    }

    private void funCheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            btLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                    /*Manifest.permission.BLUETOOTH_ADVERTISE,*/
                    Manifest.permission.BLUETOOTH_SCAN});
        } else {
            btLauncher.launch(new String[]{Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }

    }

    ActivityResultLauncher<Intent> enableBluetoothResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), results -> {

                if (results.getResultCode() != RESULT_OK) {
                    //not granted

                } else {
                    //granted
                    if (results.getData() != null)
                        if (results.getData().getIntExtra("Data", 0) == 0)
                            funConnectBT();
                }
            });

    ActivityResultLauncher<String[]> btLauncher = registerForActivityResult
            (new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Log.d("TAG", ": permissionxxx" + result.keySet() + " \n value :" + result.values());
                if (result.containsValue(false)) {
                    Log.i("MY_TAG", "At least one of the permissions was not granted.");
                    Toast.makeText(
                            this.getApplicationContext(),
                            "At least one of the permissions was not granted. Go to app settings and give permissions manually",
                            Toast.LENGTH_SHORT
                    ).show();
                    getLaunch(0);
                } else {
                    funConnectBT();
                }
            });


    private void funConnectBT() {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothAdap = btManager.getAdapter();
        } else {
            bluetoothAdap = BluetoothAdapter.getDefaultAdapter();
        }

        if (bluetoothAdap.isDiscovering()) {
            bluetoothAdap.cancelDiscovery();
        }
        bluetoothAdap.startDiscovery();
        if (bluetoothAdap.isEnabled()) {
            binding.BTDeviceConnect.setText("Connected");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getLaunch(1);
            } else {
                bluetoothAdap.disable();
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getLaunch(0);
            } else {
                bluetoothAdap.enable();
            }
            binding.BTDeviceConnect.setText("Connect");

        }
        if (bluetoothAdap != null && bluetoothAdap.isEnabled())
            getConnectedDeviceDetails();

    }

    private void getLaunch(int i) {
        enableBluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).putExtra("Data", i));
    }

    private void getConnectedDeviceDetails() {
        HashMap<Boolean, String> mapBtDevices = new HashMap<>();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;
        try {
            Method method = bluetoothAdapterClass
                    .getDeclaredMethod("getConnectionState", (Class[]) null);
            method.setAccessible(true);
            int state = (int) method.invoke(bluetoothAdap, (Object[]) null);
            Log.d("TAG***", "funCheckBTDevice: STATE =" + state);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Set<BluetoothDevice> btDevices = bluetoothAdap.getBondedDevices();
            if (btDevices == null) {
                Toast.makeText(this, "No Devices found....", Toast.LENGTH_SHORT).show();
            } else {
                for (BluetoothDevice bt : btDevices
                ) {
                    Method isConnectedMethod = BluetoothDevice.class
                            .getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(bt, (Object[]) null);
                    Log.d("TAG***", "getConnectedBluetooth() isConnected:" + isConnected
                            + " getAddress:" + bt.getAddress() + " getName:" + bt.getName());

                    mapBtDevices.put(isConnected, " DeviceName:\n" + bt.getName() + " \nAddress:\n" + bt.getAddress());

                }

                if (mapBtDevices.containsKey(true)) {
                    binding.TVBtDetails.setText(mapBtDevices.get(true) + "\n");

                } else {
                    binding.TVBtDetails.setText("No device found");

                }
            }
        } catch (Exception e) {
            Log.e("TAG***", "funCheckBTDevice: " + e.getMessage());
            e.printStackTrace();
        }

    }


}