import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;



public class UrlParser extends RecursiveAction {
    static ArrayList<String> allLinks = new ArrayList<>();
    private final String url;
    private final static String fileRegex = "(?i).*\\.(doc|pdf|xml|xls|xlsx|jpg|jpeg|gif|png|rar|zip|exe|bin|ppt|" +
            "apk|jar|mp3|aac|csv|json|)$";

    public UrlParser(String url) {
        this.url = url;
    }

    @Override
    protected void compute() {
        List<UrlParser> tasks = new LinkedList<>();

        if (!allLinks.contains(url)) {
            allLinks.add(url);
            try {
                String urlToBase = url.substring(Main.rootUrl.length() - 1);

                Connection connection = JsoupData.createConnection(url);
                Thread.sleep(400);

                int statusCode = JsoupData.getResponseCode(connection);
                String content = connection.get().html();
                DataBase.insertPage(Main.rootUrl, urlToBase, statusCode, content);

                Elements tagA = JsoupData.getElements(connection);

                for (Element element : tagA) {
                    if (element.absUrl("href").contains(Main.rootUrl) &&
                            !element.absUrl("href").matches(fileRegex)) {
                        UrlParser subTask = new UrlParser(element.absUrl("href"));
                        subTask.fork();
                        tasks.add(subTask);
                    }
                }
            } catch (IOException | InterruptedException | SQLException e) {
                e.printStackTrace();
            }
        }
        for (UrlParser parser : tasks) {
            parser.join();
        }
    }
}
