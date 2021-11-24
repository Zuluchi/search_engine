import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.RecursiveAction;


public class UrlParser extends RecursiveAction {
    private final static String urlRegex = "(?i).*(\\.(doc|pdf|xml|xls|xlsx|jpg|jpeg|gif|png|rar|zip|exe|bin|ppt|apk|" +
            "jar|mp3|aac|csv|json|eps|nc|fig)|/{3,}|#+).*$";
    private final Set<String> urlSet;
    private final String url;
    private final String rootUrl;
    private final Lemmatizer lemmatizer;

    public UrlParser(String url, String rootUrl, Set<String> urlSet, Lemmatizer lemmatizer) {
        this.url = url.toLowerCase(Locale.ROOT);
        this.urlSet = urlSet;
        this.rootUrl = rootUrl;
        this.lemmatizer = lemmatizer;
    }

    @Override
    protected void compute() {
        List<UrlParser> tasks = new LinkedList<>();
        try {
            Connection connection = JsoupData.createConnection(url);
            Thread.sleep(400);
            insertData(connection);

            Elements tagA = JsoupData.getElementsByTagA(connection);

            for (Element element : tagA) {
                String lowerCaseElementUrl = element.absUrl("href").toLowerCase(Locale.ROOT);
                if (isUrlCorrect(lowerCaseElementUrl)) {
                    urlSet.add(lowerCaseElementUrl);
                    UrlParser subTask = new UrlParser(lowerCaseElementUrl, rootUrl, urlSet, lemmatizer);
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

    private void insertData(Connection connection) throws IOException, SQLException, InterruptedException {
        int responseCode = JsoupData.getResponseCode(connection);
        int pageId = DataBase.insertPageAndGetId(url.substring(rootUrl.length() - 1),
                responseCode, connection.get().html());
        String bodyText = JsoupData.getBodyText(connection);
        String titleText = JsoupData.getTitleText(connection);

        if (responseCode != 404 || responseCode != 500) {
            Map<String, Integer> lemmasId = DataBase
                    .insertLemmsAndGetId(lemmatizer.getLemmaSet(bodyText + " " + titleText));
            Map<String, Float> titleLemmasCount = lemmatizer.countLemmasOnField(titleText);
            Map<String, Float> bodyLemmasCount = lemmatizer.countLemmasOnField(bodyText);
            Map<String, Float> lemmasAndRank = lemmatizer.calculateLemmasRank(lemmasId, titleLemmasCount,
                    bodyLemmasCount);
            DataBase.insertIndex(pageId, lemmasAndRank, lemmasId, titleLemmasCount, bodyLemmasCount);
        }
    }

    private boolean isUrlCorrect(String url) {
        return url.startsWith(rootUrl) &&
                !url.matches(urlRegex) &&
                !urlSet.contains(url);
    }
}