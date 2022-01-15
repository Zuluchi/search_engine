package application.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

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
        String[] queryWords = searchQuery.split("\\s+");

        if (!textOfSearchQuery.isEmpty()) {
            int firstIndexOfSnippet = textOfSearchQuery.indexOf(searchQuery) > 80 ?
                    textOfSearchQuery.indexOf(searchQuery) - 80 : 0;
            int lastIndexOfSnippet = Math.min(firstIndexOfSnippet + searchQuery.length() + 160,
                    textOfSearchQuery.length());

            String firstWordSnippet = textOfSearchQuery.substring(firstIndexOfSnippet, lastIndexOfSnippet)
                    .replaceAll(createSnippetRegex(queryWords[0]), "<b>" + queryWords[0]);

            return firstWordSnippet.replaceAll(createSnippetRegex(queryWords[queryWords.length - 1]),
                    queryWords[queryWords.length - 1] + "</b>");
        } else {
            StringBuilder snippetBuilder = new StringBuilder();

            for (String word : queryWords) {
                String substring = word.substring(0, word.length() - 2);
                String textOfSearchWord = doc.getElementsContainingOwnText(substring).text();
                if (!textOfSearchWord.isEmpty()) {
                    int firstIndexOfSnippet = textOfSearchWord.indexOf(word) > 30 ?
                            textOfSearchWord.indexOf(word) - 30 : 0;
                    int lastIndexOfSnippet = Math.min(firstIndexOfSnippet + word.length() + 80, textOfSearchWord.length() - 1);
                    String snippetPart = textOfSearchWord.substring(firstIndexOfSnippet, lastIndexOfSnippet)
                            .replaceAll(createSnippetRegex(substring), "<b>" + substring + "</b>");
                    snippetBuilder.append(snippetPart);
                    snippetBuilder.append("...");
                }
            }

            return snippetBuilder.toString();
        }
    }

    public static String getTitle(String htmlText) {
        return Jsoup.parse(htmlText).title();
    }

    private static String createSnippetRegex(String word) {
        String firstChar = String.valueOf(word.charAt(0));
        return "(?i)([" + firstChar.toLowerCase(Locale.ROOT) + firstChar.toUpperCase(Locale.ROOT) + "]" +
                word.substring(1) + ")";
    }
}
