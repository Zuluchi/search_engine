import java.io.IOException;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        UrlParserThreadPool parserThreadPool = new UrlParserThreadPool("https://www.svetlovka.ru/");
    }
}
