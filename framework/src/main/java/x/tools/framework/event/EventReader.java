package x.tools.framework.event;

import java.io.IOException;
import java.io.InputStream;

public class EventReader {

    private final InputStream inputStream;
    EventReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Event readEvent() throws IOException {
        return null;
    }

}
