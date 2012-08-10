package de.pribluda.android.accmeter.file;

import com.google.gson.stream.JsonWriter;
import de.pribluda.android.accmeter.Sample;
import de.pribluda.android.jsonmarshaller.JSONMarshaller;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

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
                                 @Mocked final Writer writer) {

        new Expectations() {
            {
                new JsonWriter(writer);

                jsonWriter.setLenient(true);
            }
        };

        new FileSink(writer);
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
                JSONMarshaller.marshall(jsonWriter,sample);
            }
        };


        sink.put(sample);
    }


    /**
     * test proper actionc
     */
    @Test
    public  void testClosing() {

    }
}
