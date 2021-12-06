import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class SearchSystem {
    private final Lemmatizer lemmatizer;

    public SearchSystem() throws IOException {
        this.lemmatizer = new Lemmatizer();
    }

    public ArrayList<PageSearchData> search(String findQuery) throws SQLException {
        Set<String> findQueryLemms = lemmatizer.getLemmaSet(findQuery);
        ArrayList<Integer> lemmasIdList = new ArrayList<>();

        ResultSet lemmasId = DataBase.getLemmas(findQueryLemms);
        lemmasId.next();
        lemmasIdList.add(lemmasId.getInt(1));

        ArrayList<Integer> searchedPagesId = DataBase.getPagesOfFirstLemma(lemmasId.getInt(1));
        while (lemmasId.next()) {
            lemmasIdList.add(lemmasId.getInt(1));
            searchedPagesId = DataBase.getSearchedPages(lemmasId.getInt(1), searchedPagesId);
        }

        ArrayList<PageSearchData> searchedPageData = DataBase.getPageRelevanceAndPathAndHtml(searchedPagesId, lemmasIdList);
        searchedPageData.forEach(page -> page.setSnippet(findQuery));
        Collections.sort(searchedPageData);

        return searchedPageData;
    }
}
