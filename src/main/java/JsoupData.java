import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

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

    public static String getSearchedTextInHtml(String htmlText, String searchQuery) {
        Document doc = Jsoup.parse(htmlText);
        String textOfSearchQuery = doc.getElementsContainingOwnText(searchQuery).text();

        if (!textOfSearchQuery.isEmpty()) {
            int firstIndexOfSnippet = textOfSearchQuery.indexOf(searchQuery) > 20 ?
                    textOfSearchQuery.indexOf(searchQuery) - 20 : 0;
            int lastIndexOfSnippet = firstIndexOfSnippet + searchQuery.length() + 30 < textOfSearchQuery.length() - 1 ?
                    firstIndexOfSnippet + searchQuery.length() + 30 : textOfSearchQuery.length() - 1;

            return "<p>" + textOfSearchQuery.substring(firstIndexOfSnippet, lastIndexOfSnippet)
                    .replace(searchQuery, "<b>" + searchQuery + "</b>") + "</p>";
        }

        StringBuilder snippetBuilder = new StringBuilder();
        String[] queryWords = searchQuery.split(" ");

        for (String word : queryWords) {
            String substring = word.substring(0, word.length() - 2);
            String textOfSearchWord = doc.getElementsContainingOwnText(substring).text();
            int firstIndexOfSnippet = textOfSearchWord.indexOf(word) > 20 ?
                    textOfSearchWord.indexOf(word) - 20 : 0;
            int lastIndexOfSnippet = firstIndexOfSnippet + word.length() + 37 < textOfSearchWord.length() - 1 ?
                    firstIndexOfSnippet + word.length() + 37 : textOfSearchWord.length() - 1;
            String snippetPart = "<p>" + textOfSearchWord.substring(firstIndexOfSnippet, lastIndexOfSnippet)
                    .replaceAll("(?i)\\w?" + substring, "<b>" + substring + "</b>") + "</p>";
            snippetBuilder.append(snippetPart);
        }

        return snippetBuilder.toString();
    }

    public static String getTitle(String htmlText){
        return Jsoup.parse(htmlText).title();
    }
}
