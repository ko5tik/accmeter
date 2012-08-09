package de.pribluda.android.accmeter.file;

import com.google.gson.stream.JsonReader;
import de.pribluda.android.accmeter.Sample;
import de.pribluda.android.jsonmarshaller.JSONUnmarshaller;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * reads samples out of reader
 *
 * @author Konstantin Pribluda
 */
public class SampleReader {

    static List<Sample> readSamples(Reader reader) {
        final ArrayList<Sample> samples = new ArrayList<Sample>();

        final JsonReader jsonReader = new JsonReader(reader);
        try {
        while (jsonReader.hasNext()) {
                samples.add(JSONUnmarshaller.unmarshall(jsonReader, Sample.class));
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  samples;
    }
}
