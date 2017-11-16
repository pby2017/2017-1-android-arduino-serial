package com.example.user.midifileplay;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DrumConn extends AppCompatActivity {

    // 연결 초기화
    private Context mContext = null;

    private SerialConnector mSerialConn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drum_conn);

        // 시스템
        mContext = getApplicationContext();

        // 커넥트 스타트
        mSerialConn = new SerialConnector(mContext);
        mSerialConn.initialize();

        // 소리 출력을 위해 설정
        final SoundPool sound = new SoundPool(1, AudioManager.STREAM_ALARM, 0);// maxStreams, streamType, srcQuality
        final int soundId = sound.load(this, R.raw.rudi2, 1);

        Button musicStart = findViewById(R.id.musicStart);
        musicStart.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                try{
                    Toast.makeText(getApplicationContext(), "음악 파일 시작됨.", Toast.LENGTH_LONG).show();
                    int streamId = sound.play(soundId, 1.0F, 1.0F,  1,  0,  1.0F);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSerialConn.finalize();
    }
}
