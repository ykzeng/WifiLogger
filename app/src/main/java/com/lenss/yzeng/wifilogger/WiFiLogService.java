package com.lenss.yzeng.wifilogger;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.lenss.yzeng.wifilogger.util.Utils;
import com.lenss.yzeng.wifilogger.wifi.WiFiDetailClosure;
import com.lenss.yzeng.wifilogger.wifi.WifiCalc;
import com.lenss.yzeng.wifilogger.wifi.model.WiFiDetail;
import com.lenss.yzeng.wifilogger.wifi.model.WiFiSignal;
import com.lenss.yzeng.wifilogger.wifi.model.WiFiWidth;

import org.apache.commons.collections4.IterableUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;

/**
 * Created by yukun on 3/21/2018.
 */

public class WiFiLogService extends Service {
    WifiManager wifiManager = null;

    FileOutputStream fout = null;
    OutputStreamWriter out = null;
    String fileName = null;

    public WiFiLogService(){
        super();
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        // Normally we would do some work here, like download a file.
//        // For our sample, we just sleep for 5 seconds.
//        while (true){
//            System.out.println("lalala");
//            performWiFiScan();
//            try{
//                sleep(5000);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification();
        startForeground(101, notification);

        Thread logTh = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    System.out.println("lalala");
                    performWiFiScan();
                    try{
                        sleep(5000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        logTh.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = "wifilog_" + timestamp + ".txt";
        fout = Utils.setupFile(this,"/Movies/", fileName);
        out = new OutputStreamWriter(fout);
    }

    private List<WiFiDetail> resultToDetails(List<ScanResult> scanResults){
        List<WiFiDetail> wiFiDetails = new ArrayList<WiFiDetail>();
        for (int i = 0; i < scanResults.size(); i ++){
            String bssid = scanResults.get(i).BSSID;
            String ssid = scanResults.get(i).SSID;
            String sigStrength = String.valueOf(scanResults.get(i).level);
            WiFiWidth wifiWidth = WifiCalc.getWiFiWidth(scanResults.get(i));
            String capabilities = scanResults.get(i).capabilities;
            int centerFreq = WifiCalc.getCenterFrequency(scanResults.get(i), wifiWidth);
            WiFiSignal wiFiSignal = new WiFiSignal(scanResults.get(i).frequency, centerFreq, wifiWidth, Integer.valueOf(sigStrength));
            WiFiDetail detail = new WiFiDetail(ssid, bssid, capabilities, wiFiSignal);
            wiFiDetails.add(detail);
        }
        return  wiFiDetails;
    }

    public void performWiFiScan() {
        //List<ScanResult> scanResults = Collections.emptyList();
        //WifiInfo wifiInfo = null;
        //List<WifiConfiguration> configuredNetworks = null;
        try {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            wifiManager.startScan();
            // Registering Wifi Receiver
            BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    retrieveResults();
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
//            wifiInfo = wifiManager.getConnectionInfo();
//            configuredNetworks = wifiManager.getConfiguredNetworks();
        } catch (Exception e) {
            // critical error: set to no results and do not die
        }



    }

    public void retrieveResults(){
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            //do your check here
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this.getBaseContext().getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permission denied!");
                    return;
                }
            }
        }
        List<ScanResult> scanResults = wifiManager.getScanResults();
        String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
        List<WiFiDetail> wifiDetails = resultToDetails(scanResults);
        final StringBuilder result = new StringBuilder();
        result.append(
                String.format(Locale.ENGLISH,
                        "Time Stamp|SSID|BSSID|Strength|Primary Channel|Primary Frequency|Center Channel|Center Frequency|Width (Range)|Distance|Security%n"));
        IterableUtils.forEach(wifiDetails, new WiFiDetailClosure(timestamp, result));

        String wifiHistData = (result.toString() + "\n");


        // to prevent string overflow, set a max string length of 4MB
        try{
            out.append(wifiHistData);
            out.flush();
            fout.flush();
            wifiHistData = "";
        }catch (IOException e){
            e.printStackTrace();
        }
        Toast.makeText(this, "wifi info logged into " + fileName, Toast.LENGTH_SHORT);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}