package de.pribluda.android.accmeter.file;

import com.google.gson.stream.JsonWriter;
import de.pribluda.android.accmeter.Sample;
import de.pribluda.android.accmeter.SampleSink;
import de.pribluda.android.jsonmarshaller.JSONMarshaller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private boolean active = true;

    /**
     * create file sink and prepare to write
     *
     * @param destinationFile shall be writeable
     */
    public FileSink(File destinationFile) throws IOException {
        this.destination = new FileWriter(destinationFile);
        jsonWriter = new JsonWriter(destination);
        jsonWriter.setLenient(true);

        jsonWriter.beginArray();

        amount = 0;
    }


    @Override
    public synchronized void put(Sample sample) {
        if (active) {
            try {
                JSONMarshaller.marshall(jsonWriter, sample);
                amount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getAmount() {
        return amount;
    }


    /**
     * close writer
     */
    public synchronized void close() throws IOException {
        active = false;
        jsonWriter.endArray();
        destination.close();
    }
}
