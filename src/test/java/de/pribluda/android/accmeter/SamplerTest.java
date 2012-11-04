package de.pribluda.android.accmeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * test capabilities of sampler
 *
 * @author Konstantin Pribluda
 */
public class SamplerTest {


    /**
     * shall create and start worker when sink is added,
     * another sink shall be added to worked
     * shall stop and dispose worker upon last sink removal
     */
    @Test
    public void testThatWorkerIsStartedUponSinkAdding(@Mocked final Sampler.Worker worker,
                                                      @Mocked(methods = {"addSink", "removeSink"}, inverse = true) final Sampler sampler,
                                                      @Mocked final SampleSink first,
                                                      @Mocked final SampleSink second,
                                                      @Mocked final SensorManager sensorManager) {

        final ArrayList<SampleSink> sinkList = new ArrayList<SampleSink>();

        Deencapsulation.setField(sampler, "sinkList", sinkList);
        Deencapsulation.setField(sampler, "updateDelay", 12345l);
        Deencapsulation.setField(sampler, "windowSize", 128);
        Deencapsulation.setField(sampler, "sensorDelay", SensorManager.SENSOR_DELAY_UI);
        Deencapsulation.setField(sampler, "sensorManager", sensorManager);


        new Expectations() {
            {
                Sampler.Worker worker = new Sampler.Worker(sensorManager, 128, SensorManager.SENSOR_DELAY_UI, 12345, sinkList);

                worker.start();

                worker.stop();
            }
        };


        sampler.addSink(first);
        sampler.addSink(first);
        sampler.addSink(second);

        assertEquals(2, ((Collection) Deencapsulation.getField(sampler, "sinkList")).size());

        sampler.removeSink(first);
        sampler.removeSink(second);


        assertNull(Deencapsulation.getField(sampler, "worker"));
    }

    /**
     * worker shall create proper buffers and FFT upon creation
     */
    @Test
    public void testProperWorkerInitialisation(@Mocked final SensorManager sensorManager) {

        ArrayList<SampleSink> sinkList = new ArrayList<SampleSink>();
        Sampler.Worker worker = new Sampler.Worker(sensorManager, 256, SensorManager.SENSOR_DELAY_FASTEST, 1000, sinkList);


        final double[] buffer = Deencapsulation.getField(worker, "buffer");
        assertEquals(256, buffer.length);


        final double[] real = Deencapsulation.getField(worker, "real");
        assertEquals(256, real.length);

        for (int i = 0; i < real.length; i++) {
            assertEquals(SensorManager.GRAVITY_EARTH, real[i], 0.00000001);
        }

        final double[] imaginary = Deencapsulation.getField(worker, "imaginary");
        assertEquals(256, imaginary.length);


        assertEquals(0, Deencapsulation.getField(worker, "index"));


        // shall create fft with proper size
        final FFT fft = Deencapsulation.getField(worker, "fft");
        assertNotNull(fft);
        assertEquals(256, Deencapsulation.getField(fft, "n"));

        assertEquals(Sampler.RunnerState.STOPPED, Deencapsulation.getField(worker, "state"));
    }


    /**
     * shall register itself as listener with proper parameters
     * shall fire up own thread
     */
    @Test
    public void testWorkerStartup(@Mocked final SensorManager sensorManager,
                                  @Mocked final Sensor sensor,
                                  @Mocked("startPusherThread") final Sampler.Worker ww
    ) {

        final Sampler.Worker worker = new Sampler.Worker(sensorManager, 128, SensorManager.SENSOR_DELAY_GAME, 1000, new ArrayList<SampleSink>());

        new Expectations() {
            {
                sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                returns(Arrays.asList(sensor));
                sensorManager.registerListener(worker, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        };


        worker.start();

        assertEquals(Sampler.RunnerState.RUNNING, Deencapsulation.getField(worker, "state"));

    }


    /**
     * samples shall be pushed to all file
     */
    @Test
    public void testThatSamplesArePushedToAllAddedSinks(@Mocked(methods = {"pushSample", "addSink", "removeSink"}, inverse = true) final Sampler.Worker worker,
                                                        @Mocked final SampleSink first,
                                                        @Mocked final SampleSink second,
                                                        @Mocked final Sample sample) {

        ArrayList<SampleSink> sampleSinks = new ArrayList<SampleSink>();
        Deencapsulation.setField(worker, "sinkList", sampleSinks);

        sampleSinks.add(first);
        sampleSinks.add(second);

        new Expectations() {
            {
                // both shall receive sample
                first.put(sample);
                second.put(sample);

            }
        };


        Deencapsulation.invoke(worker, "pushSample", sample);


    }


    /**
     * shall retrieve instance of accelerometer service on creation
     */
    @Test
    public void testCreation(@Mocked final Context context,
                             @Mocked final SensorManager sensorManager) {

        new Expectations() {
            {
                context.getSystemService(Context.SENSOR_SERVICE);
                returns(sensorManager);
            }
        };

        final Sampler sampler = new Sampler(context);
        assertSame(sensorManager, Deencapsulation.getField(sampler, "sensorManager"));

    }


    /**
     * in case no sensor was found shall not register itself
     */
    @Test
    public void testNoSensorFoundStartDoesNothing(@Mocked(methods = {"start"}, inverse = true) final Sampler sampler,
                                                  @Mocked final SensorManager sensorManager) {

        Deencapsulation.setField(sampler, "sensorManager", sensorManager);
        new Expectations() {
            {
                invoke(sampler, "reset");
                sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                returns(Collections.EMPTY_LIST);
            }
        };

        sampler.start();

        assertFalse((Boolean) Deencapsulation.getField(sampler, "active"));
    }


    /**
     * shall deregister itself aas listener on sensor stop
     */
    @Test
    public void testSensorStop(@Mocked(methods = {"stop"}, inverse = true) final Sampler.Worker worker,
                               @Mocked final SensorManager sensorManager) throws InterruptedException {

        Deencapsulation.setField(worker, "sensorManager", sensorManager);
        Deencapsulation.setField(worker, "state", Sampler.RunnerState.RUNNING);

        new Expectations() {
            {
                sensorManager.unregisterListener(worker);
            }
        };

        worker.stop();
        assertEquals(Sampler.RunnerState.STOPPING, Deencapsulation.getField(worker, "state"));
    }


}
