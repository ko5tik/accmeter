package de.pribluda.android.accmeter.file;

import com.google.gson.stream.JsonWriter;
import de.pribluda.android.accmeter.Sample;
import de.pribluda.android.accmeter.SampleSink;
import de.pribluda.android.jsonmarshaller.JSONMarshaller;

import java.io.Writer;

/**
 * saves incoming samples into file on storage  for later use
 *
 * @author Konstantin Pribluda
 */
public class FileSink implements SampleSink {

    private final Writer destination;
    private final JsonWriter jsonWriter;
    private int amount;
    /**
     * create file sink and prepare to write
     *
     * @param destination
     */
    public FileSink(Writer destination) {
        this.destination = destination;
        jsonWriter = new JsonWriter(destination);
        amount = 0;
    }


    @Override
    public void put(Sample sample) {
        try {
            JSONMarshaller.marshall(jsonWriter,sample);
            amount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAmount() {
        return amount;
    }

}
