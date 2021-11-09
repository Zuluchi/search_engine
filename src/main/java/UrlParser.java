import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.RecursiveAction;


public class UrlParser extends RecursiveAction {
    private final static String urlRegex = "(?i).*(\\.(doc|pdf|xml|xls|xlsx|jpg|jpeg|gif|png|rar|zip|exe|bin|ppt|apk|" +
            "jar|mp3|aac|csv|json|eps|nc|fig)|/{3,}|#+).*$";
    private final Set<String> urlSet;
    private final String url;
    private final String rootUrl;

    public UrlParser(String url, String rootUrl, Set<String> urlSet) {
        this.url = url.toLowerCase(Locale.ROOT);
        this.urlSet = urlSet;
        this.rootUrl = rootUrl;
    }

    @Override
    protected void compute() {
        List<UrlParser> tasks = new LinkedList<>();
        try {
            Connection connection = JsoupData.createConnection(url);
            Thread.sleep(400);
            DataBase.insertPage(rootUrl, url.substring(rootUrl.length() - 1),
                    JsoupData.getResponseCode(connection), connection.get().html());

            Elements tagA = JsoupData.getElements(connection);

            for (Element element : tagA) {
                String lowerCaseElementUrl = element.absUrl("abs:href").toLowerCase(Locale.ROOT);
                if (isUrlCorrect(lowerCaseElementUrl)) {
                    urlSet.add(lowerCaseElementUrl);
                    UrlParser subTask = new UrlParser(lowerCaseElementUrl, rootUrl, urlSet);
                    subTask.fork();
                    tasks.add(subTask);
                }
            }
        } catch (IOException | InterruptedException | SQLException e) {
            System.err.println(url);
            System.out.println(e.getMessage());
        }
        for (UrlParser parser : tasks) {
            parser.join();
        }
    }

    private boolean isUrlCorrect(String url) {
        return url.startsWith(rootUrl) &&
                !url.matches(urlRegex) &&
                !urlSet.contains(url);
    }
}