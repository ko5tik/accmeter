package de.pribluda.android.accmeter;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * gather samples from accelerometer and pipe them into  sinks
 */
public class Sampler implements SensorEventListener {

    public static final String LOG_TAG = "strokeCounter.detector";

    private static Sampler instance;

    // delay between updates
    private long updateDelay = 1000;
    // window size for fft
    private int windowSize = 128;
    private SensorManager sensorManager;

    private long lastEvent;


    FFT fft;

    private double[] buffer;
    private double real[];
    private double imaginary[];
    // array index
    private int index;

    private final List<SampleSink> sinkList = new ArrayList<SampleSink>();

    private boolean active = false;
    private long eventFrequency;
    private  int sensorDelay;

    public Sampler(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorDelay = SensorManager.SENSOR_DELAY_GAME;
    }


    public static Sampler getInstance(Context context) {
        if (instance == null) {
            instance = new Sampler(context);
        }
        return instance;
    }

    /**
     * start sensor data acquisition, processing and pushing to  destinations
     */
    public void start() {
        if (!active) {
            reset();

            final List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (!sensorList.isEmpty()) {
                active = true;
                sensorManager.registerListener(this, sensorList.get(0), sensorDelay);
                startPusherThread();
            }
        }
    }

    /**
     * start thread pushing processed data to sinks
     */
    private void startPusherThread() {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        long scheduled = System.currentTimeMillis() + updateDelay;
                        while (active) {
                            try {
                                long delay = scheduled - System.currentTimeMillis();
                                if (delay < 0) {
                                    delay = 0;
                                    scheduled = System.currentTimeMillis();
                                }
                                Thread.sleep(delay);
                                scheduled = scheduled + updateDelay;
                            } catch (InterruptedException e) {
                                // ignore it
                            }
                            updateData();
                        }

                    }
                }
        ).start();

    }

    /**
     * compute fft and update  sinks
     */
    private void updateData() {

        // calculate fft
        // System.arraycopy(buffer, 0, real, 0, windowSize);
        for(int i = 0; i < buffer.length; i++) {
            real[i] = buffer[ (index + i + 1) % buffer.length];
        }

        Arrays.fill(imaginary, 0);

        fft.fft(real, imaginary);

        // create sample object
        final Sample sample = new Sample();
        //  can not use arrays, as androis sports only 1.5 without copy of
        sample.setReal(new double[real.length]);
        System.arraycopy(real,0,sample.getReal(),0,real.length);
        sample.setImaginary(new double[imaginary.length]);
        System.arraycopy(imaginary,0,sample.getImaginary(),0,imaginary.length);

        sample.setTimestamp(System.currentTimeMillis());
        sample.setSampleRate(eventFrequency);

        pushSample(sample);
    }


    public void stop() {
        if (active) {
            sensorManager.unregisterListener(this);
            active = false;
        }
    }

    /**
     * receive sensor event and place it into  buffer
     *
     * @param sensorEvent
     */
    public void onSensorChanged(SensorEvent sensorEvent) {

        // we are only interested in accelerometer events
        if (Sensor.TYPE_ACCELEROMETER == sensorEvent.sensor.getType()) {
            // compute modulo
            double modulo = Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0] + sensorEvent.values[1] * sensorEvent.values[1] + sensorEvent.values[2] * sensorEvent.values[2]);
            // store difference
            buffer[index] = modulo;

            index++;
            index %= windowSize;

            // calculate delay of last event  and event rate
            final long thisEvent = sensorEvent.timestamp;
            long delay = thisEvent - lastEvent;
            lastEvent = thisEvent;

            if (0 != delay) {
                System.err.println("set delay:" + delay);
                eventFrequency = 1000000000 / delay;
            }

        }
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * access to sample buffer
     *
     * @return
     */
    public double[] getBuffer() {
        return buffer;
    }

    public void addSink(SampleSink sink) {
        if (!sinkList.contains(sink)) {
            sinkList.add(sink);
        }
    }

    public void removeSink(SampleSink sink) {
        sinkList.remove(sink);
    }


    /*
     * push samples to registered sink
     * @param  sample
     */
    private void pushSample(Sample sample) {
        for (SampleSink sink : sinkList) {
            sink.put(sample);
        }
    }

    /**
     * reset sampler state
     */
    private void reset() {
        buffer = new double[windowSize];
        real = new double[windowSize];
        imaginary = new double[windowSize];
        fft = new FFT(windowSize);
        index = 0;

        lastEvent = System.nanoTime();
    }


    /**
     * configured window size
     *
     * @return
     */
    public int getWindowSize() {
        return windowSize;
    }

    /**
     * adjust window size. needs restart to take effect. Only power of 2 ( 32, 64 ... 512 ) values are acceptable
     * for FFT ( 1024 is out of bounds ) ,  most useful values are 64 / 128 / 256
     *
     * @param windowSize
     */
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public long getUpdateDelay() {
        return updateDelay;
    }

    public void setUpdateDelay(long updateDelay) {
        this.updateDelay = updateDelay;
    }

    /**
     * configured sensor delay.  use constants from  SensorManager class.  Changes will be actived after restart
     * DELAY_GAME (default) seems to be most uniform,  but does not send events if there is no changes below some
     * threshold, DELAY_FASTEST sends always but is not uniform.
     * @return
     */
    public int getSensorDelay() {
        return sensorDelay;
    }

    public void setSensorDelay(int sensorDelay) {
        this.sensorDelay = sensorDelay;
    }
}
