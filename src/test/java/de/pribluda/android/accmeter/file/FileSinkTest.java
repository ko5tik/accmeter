package de.pribluda.android.accmeter.file;

import com.google.gson.stream.JsonWriter;
import de.pribluda.android.accmeter.Sample;
import de.pribluda.android.jsonmarshaller.JSONMarshaller;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

/**
 * test capabilities of file sink
 *
 * @author Konstantin Pribluda
 */
public class FileSinkTest {


    /**
     * shall create file sink and prepare to write  JSON objects
     */
    @Test
    public void testSinkCreation(@Mocked final JsonWriter jsonWriter,
                                 @Mocked final File destFile,
                                 @Mocked final FileWriter fileWriter) throws IOException {

        new Expectations() {
            {

                new FileWriter(destFile);

                //result = fileWriter;

                new JsonWriter((Writer) any);

                jsonWriter.setLenient(true);

                jsonWriter.beginArray();
            }
        };

        new FileSink(destFile);
    }


    /**
     * shall marshall object into writer
     */
    @Test
    public void testObjectWriting(@Mocked final JsonWriter jsonWriter,
                                  @Mocked final JSONMarshaller marshaller,
                                  @Mocked final Sample sample,
                                  @Mocked(methods = {"put"}, inverse = true) final FileSink sink) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Deencapsulation.setField(sink, "jsonWriter", jsonWriter);

        new Expectations() {
            {
                JSONMarshaller.marshall(jsonWriter, sample);
            }
        };


        sink.put(sample);
    }


    /**
     * test proper actionc
     */
    @Test
    public void testClosing(@Mocked final JsonWriter jsonWriter,
                            @Mocked final FileWriter fileWriter,
                            @Mocked(methods = {"close"}, inverse = true) final FileSink fileSink) throws IOException {


        Deencapsulation.setField(fileSink, "destination", fileWriter);
        Deencapsulation.setField(fileSink, "jsonWriter", jsonWriter);
        new Expectations() {
            {

                jsonWriter.endArray();
                fileWriter.close();
            }
        };

        fileSink.close();
    }
}
