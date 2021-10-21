import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JsoupData {

    public static Connection createConnection(String url) {
        return Jsoup.connect(url).userAgent("searchEngineBot/0.1").referrer("http://www.google.com").ignoreHttpErrors(true).ignoreContentType(true);
    }

    public static int getResponseCode(Connection connection) throws IOException {
        Connection.Response response = connection.execute();
        return response.statusCode();
    }

    public static Elements getElements(Connection connection) throws InterruptedException, IOException {
        Document doc = connection.get();
        Elements link = doc.getElementsByTag("a");
        return link;
    }
}
