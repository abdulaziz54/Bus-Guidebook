package amr.com.routing;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

class XMLParser {
    // names of the XML tags
    protected static final String MARKERS = "markers";
    protected static final String MARKER = "marker";

    private URL feedUrl;

    XMLParser(final String feedUrl) {
        try {
            this.feedUrl = new URL(feedUrl);
        } catch (MalformedURLException e) {
            Log.e("Routing Error", e.getMessage());
        }
    }

    InputStream getInputStream() {
        try {
            return feedUrl.openConnection().getInputStream();
        } catch (IOException e) {
            Log.e("Routing Error", e.getMessage());
            return null;
        }
    }
}