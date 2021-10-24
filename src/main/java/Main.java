import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static String rootUrl;

    public static void main(String[] args) throws IOException, SQLException {
        rootUrl = "https://dimonvideo.ru/";
        DataBase.getDBConnection();
        DataBase.createTableForPage(rootUrl);
        Void forkJoinPool = new ForkJoinPool().invoke(new UrlParser(rootUrl));
    }
}
