import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class UrlParserThreadPool {
    public UrlParserThreadPool(String url) throws SQLException, IOException {
        Set<String> parsedURLs = Collections.synchronizedSet(new HashSet<>());
        parsedURLs.add(url.toLowerCase(Locale.ROOT));
        DataBase.createConnection();
        DataBase.createTables();
        Lemmatizer lemmatizer = new Lemmatizer();
        Void forkJoinPool = new ForkJoinPool().invoke(new UrlParser(url, url.toLowerCase(Locale.ROOT),
                parsedURLs, lemmatizer));
    }
}
