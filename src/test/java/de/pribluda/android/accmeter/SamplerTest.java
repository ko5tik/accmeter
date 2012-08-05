package de.pribluda.android.accmeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * test capabilities of sampler
 *
 * @author Konstantin Pribluda
 */
public class SamplerTest {


    /**
     * samples shall be pushed to all sinks
     */
    @Test
    public void testThatSamplesArePushedToAllAddedSinks(@Mocked(methods = {"pushSample", "addSink", "removeSink"}, inverse = true) final Sampler sampler,
                                                        @Mocked final SampleSink first,
                                                        @Mocked final SampleSink second,
                                                        @Mocked final Sample sample) {

        Deencapsulation.setField(sampler, "sinkList", new ArrayList<SampleSink>());

        sampler.addSink(first);
        sampler.addSink(second);

        new Expectations() {
            {
                // both shall receive sample
                first.put(sample);
                second.put(sample);

                // first is removed
                second.put(sample);
            }
        };


        Deencapsulation.invoke(sampler, "pushSample", sample);

        sampler.removeSink(first);

        Deencapsulation.invoke(sampler, "pushSample", sample);

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
     * on start counting it shall:
     * - reset state
     * - activate sensor manager with some (TBD) precision
     * - use itself as a listener
     * <p/>
     * but only in case sensor was found
     */
    @Test
    public void testCounterStarting(@Mocked(methods = {"start"}, inverse = true) final Sampler sampler,
                                    @Mocked final SensorManager sensorManager,
                                    @Mocked final Sensor sensor) {

        Deencapsulation.setField(sampler, "sensorManager", sensorManager);
        new Expectations() {
            {
                invoke(sampler, "reset");
                sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                returns(Arrays.asList(sensor));
                sensorManager.registerListener(sampler, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                invoke(sampler, "startPusherThread");
            }
        };


        sampler.start();

        assertTrue((Boolean) Deencapsulation.getField(sampler, "active"));
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
     * shall do nothing is already started
     *
     * @param sampler
     */
    @Test
    public void testNothingHappensIfAlreadyStarted(@Mocked(methods = {"start"}, inverse = true) final Sampler sampler) {
        Deencapsulation.setField(sampler, "active", true);

        sampler.start();
        new FullVerifications() {
            {

            }
        };
    }

    /**
     * shall deregister itself aas listener on sensor stop
     */
    @Test
    public void testSensorStop(@Mocked(methods = {"stop"}, inverse = true) final Sampler sampler,
                               @Mocked final SensorManager sensorManager) {

        Deencapsulation.setField(sampler, "sensorManager", sensorManager);
        Deencapsulation.setField(sampler, "active", true);
        new Expectations() {
            {
                sensorManager.unregisterListener(sampler);
            }
        };


        sampler.stop();

        assertFalse((Boolean) Deencapsulation.getField(sampler, "active"));
    }


    /**
     * if not started,  shall do nothing on stop
     */
    @Test
    public void testThatNothingIsDoneIfNotStarted(@Mocked(methods = {"stop"}, inverse = true) final Sampler sampler) {


        sampler.stop();


        new FullVerifications() {
            {

            }
        };

    }


    /**
     * shall initialise internal buffers on reset
     */
    @Test
    public void testProperResetting(@Mocked(methods = {"reset", "setWindowSize"}, inverse = true) final Sampler sampler) {


        sampler.setWindowSize(256);
        Deencapsulation.invoke(sampler,"reset");

        final double[] buffer = Deencapsulation.getField(sampler, "buffer");
        assertEquals(256,buffer.length);


        final double[] real = Deencapsulation.getField(sampler, "real");
        assertEquals(256,real.length);


        final double[] imaginary = Deencapsulation.getField(sampler, "imaginary");
        assertEquals(256,imaginary.length);



        assertEquals(0,Deencapsulation.getField(sampler,"index"));




        // shall create fft with proper size
        final FFT fft = Deencapsulation.getField(sampler, "fft");
        assertNotNull(fft);
        assertEquals(256,Deencapsulation.getField(fft,"n"));
    }
}
