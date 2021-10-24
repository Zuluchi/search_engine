import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.RecursiveAction;


public class UrlParser extends RecursiveAction {
    private final static String fileTypeRegex = "(?i).*\\.(doc|pdf|xml|xls|xlsx|jpg|jpeg|gif|png|rar|zip|exe|bin|ppt|" +
            "apk|jar|mp3|aac|csv|json|)$";
    private static final Set<String> parsedURLs = new HashSet<>();
    private final String url;

    public UrlParser(String url) {
        this.url = url;
    }

    @Override
    protected void compute() {
        List<UrlParser> tasks = new LinkedList<>();
        if (addUrlIfNotContains(url)) {
            try {
                Connection connection = JsoupData.createConnection(url);
                Thread.sleep(400);
                DataBase.insertPage(Main.rootUrl, url.substring(Main.rootUrl.length() - 1),
                        JsoupData.getResponseCode(connection), connection.get().html());

                Elements tagA = JsoupData.getElements(connection);

                for (Element element : tagA) {
                    if (element.absUrl("href").contains(Main.rootUrl) &&
                            !element.absUrl("href").matches(fileTypeRegex)) {
                        UrlParser subTask = new UrlParser(element.absUrl("href"));
                        subTask.fork();
                        tasks.add(subTask);
                    }
                }
            } catch (IOException | InterruptedException | SQLException e) {
                e.printStackTrace();
            }
            for (UrlParser parser : tasks) {
                parser.join();
            }
        }
    }

    private synchronized boolean addUrlIfNotContains(String url) {
        if (!parsedURLs.contains(url)) {
            parsedURLs.add(url);
            return true;
        }
        return false;
    }
}
