import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class UrlParserThreadPool {
    public UrlParserThreadPool(String url) throws SQLException {
        Set<String> parsedURLs = Collections.synchronizedSet(new HashSet<>());
        parsedURLs.add(url.toLowerCase(Locale.ROOT));
        DataBase.getDBConnection();
        DataBase.createTableForPage(url);
        Void forkJoinPool = new ForkJoinPool().invoke(new UrlParser(url, url.toLowerCase(Locale.ROOT), parsedURLs));
    }
}
