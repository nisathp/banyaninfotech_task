package com.neltech.banyaninfotechtask;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.neltech.banyaninfotechtask.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding = null;

    String SSID = null;
    String PASSWORD = null;
    int endTime = 20;
    int progress;
    CountDownTimer countDownTimer;
    private WifiManager wifiManager;
    private ActivityResultLauncher<Intent> launcher;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        launcher = wifiDisconnect();

        binding.BTConnect.setOnClickListener(v -> {
            if (!wifiManager.isWifiEnabled()) {
                Log.d("TAG", "onFinish: " + wifiManager.isWifiEnabled());
                wifiManager.setWifiEnabled(true);
            }
            SSID = binding.ETSsid.getText().toString();
            PASSWORD = binding.ETPassword.getText().toString();
            if (SSID == null || SSID.isEmpty()) {
                binding.ETSsid.setError("Enter valid SSID!");
                return;
            }
            if (PASSWORD == null || PASSWORD.isEmpty()) {
                binding.ETPassword.setError("Enter valid Password!");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                funConnectWifiAbove10(SSID, PASSWORD);
            } else {
                funConnectWifi(SSID, PASSWORD);
            }
            funProgressAnimationSetter();
            funWifiAutoOn(SSID, PASSWORD);
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void funConnectWifiAbove10(String SSID, String PASSWORD) {
        WifiNetworkSpecifier wifiNetSpec = new WifiNetworkSpecifier.Builder()
                .setSsid(SSID)
                .setWpa2Passphrase(PASSWORD)
                .build();

        NetworkRequest netReq = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetSpec)
                .build();

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        connectivityManager.requestNetwork(netReq, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d("TAG", "onAvailable: ");

            }
        });

    }

    private void funWifiAutoOn(String SSID, String PASSWORD) {

    }

    private String funConnectWifi(String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        //remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);

        boolean isConnectionSuccessful = wifiManager.reconnect();

        if (isConnectionSuccessful) {
            return "connection successful";
        } else {
            binding.ETPassword.setError("Enter valid Password!");
            return "invalid credential";
        }
    }


    private void funProgressAnimationSetter() {
        RotateAnimation verticalAnimation = new RotateAnimation(
                0, -90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        verticalAnimation.setFillAfter(true);
        binding.progressbarTimerview.startAnimation(verticalAnimation);
        binding.progressbarTimerview.setSecondaryProgress(endTime);
        binding.progressbarTimerview.setProgress(0);
        funCountDownTimer();
    }

    private void funCountDownTimer() {
        progress = 1;
        countDownTimer = new CountDownTimer(endTime * 1000L, 1000) {
            @Override
            public void onTick(long millisec) {
                Log.d("TAG", "onTick: " + millisec);
                progress = progress + 1;
                setProgress(progress, endTime);
                int seconds = (int) (millisec / 1000) % 60;
                binding.tvTimer.setText(seconds + "");

            }

            @Override
            public void onFinish() {
                Log.d("TAG", "onFinish: ");
                if (wifiManager.isWifiEnabled()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                        // startActivityForResult(panelIntent, 1, new Bu);
                        launcher.launch(panelIntent);

                    }else{
                        wifiManager.setWifiEnabled(false);
                        Log.d("TAG", "onFinish: " + wifiManager.isWifiEnabled());
                        wifiManager.disconnect();
                    }



                }


                runOnUiThread(()->{
                    setProgress(progress, endTime);
                });

            }


        };

        countDownTimer.start();
    }
    private ActivityResultLauncher<Intent> wifiDisconnect() {
        ActivityResultLauncher<Intent> actResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode()==RESULT_OK){
                            Log.d("TAG", "onActivityResult: ");
                        }
                    }
                });
        return actResult;
    }
    public void setProgress(int startTime, int endTime) {
        binding.progressbarTimerview.setMax(endTime);
        binding.progressbarTimerview.setSecondaryProgress(endTime);
        binding.progressbarTimerview.setProgress(startTime);

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.ACTION_WIFI_SCAN_AVAILABILITY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MAIN", "onCreate: ");
            registerReceiver(wifiReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(wifiReceiver, intentFilter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(wifiReceiver);
        unregisterReceiver(wifiReceiver);
    }


    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER", "onReceive:  " + intent.getAction());

            int wifi_action = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifi_action) {
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d("RECEIVER", "onReceive: WIFI is On ");
                    Toast.makeText(context, "WIFI is On....", Toast.LENGTH_SHORT).show();
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d("RECEIVER", "onReceive: WIFI is Off ");
                    Toast.makeText(context, "WIFI is Off....", Toast.LENGTH_SHORT).show();
                    break;
                case WifiManager.WIFI_MODE_SCAN_ONLY:
                    Log.d("RECEIVER", "onReceive: Searching ");
                    Toast.makeText(context, "Searching....", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.d("RECEIVER", "onReceive: Something wrong :" + wifi_action);
                    Toast.makeText(context, "Something wrong....", Toast.LENGTH_SHORT).show();

            }
        }
    };

}