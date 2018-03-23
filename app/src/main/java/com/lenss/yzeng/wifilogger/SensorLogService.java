package com.lenss.yzeng.wifilogger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.lenss.yzeng.wifilogger.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yukun on 3/22/2018.
 */

public class SensorLogService extends Service implements SensorEventListener2 {
    SensorManager manager = null;
    String fileName = "sensor_log_";
    String filePath = "/Movies/";
    FileOutputStream fout = null;
    OutputStreamWriter writer = null;
    @Override
    public void onCreate(){
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = fileName + timestamp + ".csv";
        try{
            fout = Utils.setupFile(this,filePath, fileName);
            writer = new OutputStreamWriter(fout);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        // new sensing capability
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(SensorLogService.this,
                manager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            this.writer.close();
            this.fout.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent evt) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        //long real_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String timestamp = Utils.convertTime(real_stamp);
        try {
            switch(evt.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    writer.write(String.format("%s; ACC; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    writer.write(String.format("%s; GYRO_UN; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], evt.values[3], evt.values[4], evt.values[5]));
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    writer.write(String.format("%s; GYRO; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    writer.write(String.format("%s; MAG; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    writer.write(String.format("%s; MAG_UN; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    writer.write(String.format("%s; ROT; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], evt.values[3], 0.f, 0.f));
                    break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    writer.write(String.format("%s; GAME_ROT; %f; %f; %f; %f; %f; %f\n", timestamp, evt.values[0], evt.values[1], evt.values[2], evt.values[3], 0.f, 0.f));
                    break;
                case  Sensor.TYPE_AMBIENT_TEMPERATURE:
                    writer.write(String.format("%s; TEMP; %f; \n", timestamp, evt.values[0]));
                    break;
                case  Sensor.TYPE_LIGHT:
                    writer.write(String.format("%s; LIGHT; %f; \n", timestamp, evt.values[0]));
                    break;
                case  Sensor.TYPE_PRESSURE:
                    writer.write(String.format("%s; PRESSURE; %f;\n", timestamp, evt.values[0]));
                    break;
                case  Sensor.TYPE_PROXIMITY:
                    writer.write(String.format("%s; PROXIMITY; %f;\n", timestamp, evt.values[0]));
                    break;
            }
            writer.flush();
            fout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onFlushCompleted(Sensor sensor) {

    }
}
