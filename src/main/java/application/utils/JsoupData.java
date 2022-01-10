package application.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsoupData {


    public static Connection createConnection(String url) {
        return Jsoup.connect(url).userAgent("searchEngineBot/0.1").referrer("http://www.google.com")
                .ignoreHttpErrors(true).ignoreContentType(true);
    }

    public static int getResponseCode(Connection connection) throws IOException {
        Connection.Response response = connection.execute();
        return response.statusCode();
    }

    public static Elements getElementsByTagA(Connection connection) throws InterruptedException, IOException {
        return connection.get().getElementsByTag("a");
    }

    public static String getTitleText(Connection connection) throws IOException {
        return connection.get().title();
    }

    public static String getBodyText(Connection connection) throws IOException {
        return connection.get().body().text();
    }

    public static String getSnippetInHtml(String htmlText, String searchQuery) {
        Document doc = Jsoup.parse(htmlText);
        String textOfSearchQuery = doc.getElementsContainingOwnText(searchQuery).text();

        if (!textOfSearchQuery.isEmpty()) {
            int firstIndexOfSnippet = textOfSearchQuery.indexOf(searchQuery) > 40 ?
                    textOfSearchQuery.indexOf(searchQuery) - 40 : 0;
            int lastIndexOfSnippet = Math.min(firstIndexOfSnippet + searchQuery.length() + 40,
                    textOfSearchQuery.length() - 1);

            return textOfSearchQuery.substring(firstIndexOfSnippet, lastIndexOfSnippet)
                    .replace(searchQuery, "<b>" + searchQuery + "</b>");
        }

        StringBuilder snippetBuilder = new StringBuilder();
        String[] queryWords = searchQuery.split(" ");

        for (String word : queryWords) {
            String substring = word.substring(0, word.length() - 2);
            String textOfSearchWord = doc.getElementsContainingOwnText(substring).text();
            int firstIndexOfSnippet = textOfSearchWord.indexOf(word) > 40 ?
                    textOfSearchWord.indexOf(word) - 40 : 0;
            int lastIndexOfSnippet = Math.min(firstIndexOfSnippet + word.length() + 40, textOfSearchWord.length() - 1);
            String snippetPart = textOfSearchWord.substring(firstIndexOfSnippet, lastIndexOfSnippet)
                    .replaceAll("(?i)\\w?" + substring, "<b>" + substring + "</b>...");
            snippetBuilder.append(snippetPart);
        }

        return snippetBuilder.toString();
    }

    public static String getTitle(String htmlText) {
        return Jsoup.parse(htmlText).title();
    }
}
