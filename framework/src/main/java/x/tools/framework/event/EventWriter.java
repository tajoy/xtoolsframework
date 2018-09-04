package x.tools.framework.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EventWriter {
    private final OutputStream outputStream;
    EventWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeEvent(Event event) throws IOException {

    }
}
