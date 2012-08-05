package de.pribluda.android.accmeter;

/**
 * receives samples for further processing
 */
public interface SampleSink {

    /**
     * push sample to sink for further processing
     * @param sample  sample for processing
     */
    public void put(Sample sample);
}
