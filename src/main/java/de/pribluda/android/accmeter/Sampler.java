package de.pribluda.android.accmeter;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * gather samples from accelerometer and pipe them into  sinks
 */
public class Sampler implements SensorEventListener {

    public static final String LOG_TAG = "strokeCounter.detector";

    private static Sampler instance;

    // window size for fft
    public static final int WINDOW_SIZE = 128;
    private SensorManager sensorManager;


    private final double[] buffer = new double[WINDOW_SIZE];
    // array index
    private int index;

    private final List<SampleSink> sinkList = new ArrayList<SampleSink>();

    private boolean active = false;

    public Sampler(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
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
                sensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
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
            buffer[index] = /*lastSample - */modulo;
            //   lastSample = modulo;
            //  Log.d(LOG_TAG,"sample:" + lastSample + " difference:" + buffer[index]) ;
            // advance index
            index++;
            index %= WINDOW_SIZE;
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

    }
}
