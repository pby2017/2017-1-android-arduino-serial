package com.example.user.midifileplay;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 2017-11-11.
 */

public class SerialConnector{

    private Context mContext;

    // 계속 입력 받기 위해
    private SerialMonitorThread mSerialThread;

    private UsbSerialDriver driver;
    private UsbSerialPort port;

    public static final int BAUD_RATE = 115200;

    // 소리 초기화
    private SoundPool sound = null;
    private int soundId = 0;

    // 소리 테스트
//    AudioSynthesisTask audioSynth;

    public SerialConnector(Context c)
    {
        mContext = c;
    };

    public void initialize(){
        // 아두이노 연결을 위해 설정
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(mContext.getApplicationContext(), "availableDrivers 비었음.", Toast.LENGTH_LONG).show();
            return;
        }
        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);
        if(driver == null) {
            Toast.makeText(mContext.getApplicationContext(), "driver NULL.", Toast.LENGTH_LONG).show();
            return;
        }
        // 처음 연결
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            Toast.makeText(mContext.getApplicationContext(), "connection NULL", Toast.LENGTH_LONG).show();
            return;
        }
        port = driver.getPorts().get(0);
        if(port == null) {
            Toast.makeText(mContext.getApplicationContext(), "port NULL", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            Toast.makeText(mContext.getApplicationContext(), "연결설정 실패.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        sound = new SoundPool(1, AudioManager.STREAM_ALARM, 0);// maxStreams, streamType, srcQuality
        soundId = sound.load(mContext, R.raw.rudi2, 1);

        startThread();
    }

    public void finalize() {
        try {
            driver = null;
            stopThread();

            port.close();
            port = null;
        } catch(Exception ex) {
            Toast.makeText(mContext.getApplicationContext(), "Cannot finalize serial connector.", Toast.LENGTH_LONG).show();
        }
    }

    // start thread
    private void startThread() {
        Toast.makeText(mContext.getApplicationContext(), "Start serial monitoring thread.", Toast.LENGTH_LONG).show();
        if(mSerialThread == null) {
            mSerialThread = new SerialMonitorThread();
            mSerialThread.run();
        }
    }
    // stop thread
    private void stopThread() {
        if(mSerialThread != null && mSerialThread.isAlive())
            mSerialThread.interrupt();
        if(mSerialThread != null) {
            mSerialThread.setKillSign(true);
            mSerialThread = null;
        }
    }

    public class SerialMonitorThread extends Thread {
        // Thread status
        private boolean mKillSign = false;

        private void initializeThread() {
            // This code will be executed only once.
        }

        private void finalizeThread() {
        }

        // stop this thread
        public void setKillSign(boolean isTrue) {
            mKillSign = isTrue;
        }

        /**
         *	Main loop
         **/
        @Override
        public void run()
        {
            byte buffer[] = new byte[128];

            while(!Thread.interrupted())
            {
                if(port != null) {
                    Arrays.fill(buffer, (byte)0x00);

                    try {
                        // Read received buffer
                        int numBytesRead = port.read(buffer, 1000);
                        if(numBytesRead > 0) {
                            Toast.makeText(mContext.getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                            int streamId = sound.play(soundId, 1.0F, 1.0F,  1,  0,  1.0F);
//                            audioSynth = new AudioSynthesisTask();
//                            audioSynth.execute();
                        } // End of if(numBytesRead > 0)
                    }
                    catch (IOException e) {
                        Toast.makeText(mContext.getApplicationContext(), "Error # run.", Toast.LENGTH_LONG).show();
                        mKillSign = true;
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if(mKillSign)
                    break;

            }	// End of while() loop

            // Finalize
            finalizeThread();

        }	// End of run()

    }	// End of SerialMonitorThread

    private class AudioSynthesisTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final int SAMPLE_RATE = 11025;

            int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minSize,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();


            short[] buffer = {
                    8130, 15752, 32695, 12253, 4329,
                    -3865, -19032, -32722, -16160, -466,
                    8130, 15752, 22389, 27625, 31134, 32695, 32210,
                    29711, 25354, 19410, 12253, 4329, -3865, -11818, -19032,
                    -25055, -29511, -32121, -32722, -31276, -27874, -22728,
                    -16160, -8582, -466
            };

            /*short[] buffer = {
                                8130, 15752, 32695, 12253, 4329,
                                -3865, -19032, -32722, -16160, -466
                            };*/
            /*
            short[] buffer = { 8130, 15752, 22389, 27625, 31134, 32695, 32210,
                    29711, 25354, 19410, 12253, 4329, -3865, -11818, -19032,
                    -25055, -29511, -32121, -32722, -31276, -27874, -22728,
                    -16160, -8582, -466 };
            */

//            while (keepGoing) {
//                audioTrack.write(buffer, 0, buffer.length);
//            }

            return null;
        }
    }
}
