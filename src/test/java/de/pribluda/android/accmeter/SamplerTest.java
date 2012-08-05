package de.pribluda.android.accmeter;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import java.util.ArrayList;

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

        Deencapsulation.setField(sampler, "sinkList" , new ArrayList<SampleSink>());

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

        Deencapsulation.invoke(sampler,"pushSample",sample);


    }
}
