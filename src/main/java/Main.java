import org.jsoup.internal.StringUtil;
import org.jsoup.select.Elements;

import javax.swing.text.Element;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        //UrlParserThreadPool parserThreadPool = new UrlParserThreadPool("https://www.svetlovka.ru/");
        SearchSystem searchSystem = new SearchSystem();
        DataBase.createConnection();
        ArrayList<PageSearchData> pagesid = searchSystem.search("волшебные иллюстрации");
        for (PageSearchData page : pagesid){
            System.out.println("ID - " + page.getPageId() +"| Title - " + page.getTitle() + "| path - " + page.getPath()
                   + "| relevance - " + page.getRelevance() + "| snippet - " + page.getSnippet());
        }
    }
}
