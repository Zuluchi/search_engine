package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataBase {
    private static final String dbName = "search_engine";
    private static final String dbUser = "root";
    private static final String dbPass = "Tenzosix34";
    private static Connection connection;

    public static void createConnection() {

        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/" + dbName +
                                "?user=" + dbUser + "&password=" + dbPass
                                + "&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&" +
                                "useUnicode=true&characterEncoding=UTF-8");
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


//    public static Map<String, Integer> insertLemmsAndGetId(Set<String> lemmaSet) throws SQLException {
//        String insertQuery = "INSERT INTO _lemma"
//                + " (lemma, frequency) VALUES ( ?, ?) ON DUPLICATE KEY UPDATE frequency = frequency + 1;";
//
//        PreparedStatement insertLemmaData = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
//        for (String lemma : lemmaSet) {
//            insertLemmaData.setString(1, lemma);
//            insertLemmaData.setInt(2, 1);
//            insertLemmaData.addBatch();
//        }
//        insertLemmaData.executeBatch();
//        connection.commit();
//
//        ResultSet insertedIds = insertLemmaData.getGeneratedKeys();
//        Map<String, Integer> lemmaMap = new HashMap<>();
//        for (String lemma : lemmaSet) {
//            insertedIds.next();
//            lemmaMap.put(lemma, insertedIds.getInt(1));
//        }
//        insertedIds.close();
//        insertLemmaData.close();
//
//        return lemmaMap;
//    }

    public static void insertIndex(int pageId, Map<String, Float> lemmasAndRank,
                                   Map<String, Integer> lemmasId,
                                   Map<String, Float> titleLemms,
                                   Map<String, Float> bodyLemms) throws SQLException {
        PreparedStatement insertIndex = connection.prepareStatement("INSERT INTO _index"
                + " (page_id, field_id, lemma_id, lemma_rank) VALUES ( ?, ?, ?, ?);");
        for (Map.Entry<String, Float> lemma : titleLemms.entrySet()) {
            addBatchForIndex(pageId, 1, lemmasId.get(lemma.getKey()),
                    lemmasAndRank.get(lemma.getKey()), insertIndex);
        }
        for (Map.Entry<String, Float> lemma : bodyLemms.entrySet()) {
            addBatchForIndex(pageId, 2, lemmasId.get(lemma.getKey()),
                    lemmasAndRank.get(lemma.getKey()), insertIndex);
        }

        insertIndex.executeBatch();
        connection.commit();
        insertIndex.close();
    }

    private static void addBatchForIndex(int pageId, int fieldId, int lemmaId, float rank,
                                         PreparedStatement statement) throws SQLException {
        statement.setInt(1, pageId);
        statement.setInt(2, fieldId);
        statement.setInt(3, lemmaId);
        statement.setFloat(4, rank);
        statement.addBatch();
    }

    public static ResultSet getLemmas(Set<String> lemmas) throws SQLException {

        String query = createMultipleLemmaQuery(lemmas.size());
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        int parameterIndex = 1;
        for (String lemma : lemmas) {
            preparedStatement.setString(parameterIndex++, lemma);
        }

        return preparedStatement.executeQuery();
    }

    private static String createMultipleLemmaQuery(int length) {
        String query = "SELECT id FROM _lemma WHERE lemma IN (";
        StringBuilder queryBuilder = new StringBuilder(query);
        appendValueForQuery(queryBuilder, length);
        queryBuilder.append(" ) ORDER BY frequency;");
        return queryBuilder.toString();
    }


    public static ArrayList<Integer> getPagesOfFirstLemma(int lemmaId) throws SQLException {
        String query = "SELECT id FROM _page WHERE id in (SELECT page_id FROM _index where lemma_id = " + lemmaId + ")";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet pagesResultSet = preparedStatement.executeQuery();
        ArrayList<Integer> pagesIdList = resultSetToList(pagesResultSet);
        preparedStatement.close();

        return pagesIdList;
    }

    public static ArrayList<Integer> getSearchedPages(int lemmaId, ArrayList<Integer> pagesIdList) throws SQLException {

        String query = createMultiplePageFromLemmaQuery(pagesIdList.size());
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, lemmaId);

        int parameterIndex = 2;
        for (int pageId : pagesIdList) {
            preparedStatement.setInt(parameterIndex++, pageId);
        }
        ResultSet pagesResultSet = preparedStatement.executeQuery();

        ArrayList<Integer> filteredPagesIdList = resultSetToList(pagesResultSet);
        preparedStatement.close();

        return filteredPagesIdList;
    }

    private static String createMultiplePageFromLemmaQuery(int length) {
        String query = "SELECT id FROM _page WHERE id IN (SELECT page_id FROM _index WHERE lemma_id = ? " +
                "AND page_id in (";
        StringBuilder queryBuilder = new StringBuilder(query);
        appendValueForQuery(queryBuilder, length);
        queryBuilder.append(" ))");
        return queryBuilder.toString();
    }

    public static ArrayList<PageSearchData> getPageRelevanceAndPathAndHtml(ArrayList<Integer> pageIdList,
                                                              ArrayList<Integer> lemmaIdList) throws SQLException {
        String firstPartOFQueryRel = "SELECT page_id, sum(lemma_rank)/relrev.maxrev as relevance from _index join " +
                "(select max(absrev) as maxrev from (select page_id, sum(lemma_rank) as absrev from _index ";
        String wherePartOfQueryRel = createMultipleRelevanceQuery(pageIdList.size(), lemmaIdList.size());
        StringBuilder queryBuilder = new StringBuilder(firstPartOFQueryRel);
        queryBuilder.append(wherePartOfQueryRel);
        queryBuilder.append(" group by page_id) as result) as relrev ");
        queryBuilder.append(wherePartOfQueryRel);
        queryBuilder.append(" group by page_id order by page_id");

        PreparedStatement relevancePrepStatement = connection.prepareStatement(queryBuilder.toString());
        int parameterIndexForRel = 1;
        for (int pageId : pageIdList) {
            relevancePrepStatement.setInt(parameterIndexForRel++, pageId);
        }
        for (int pageId : lemmaIdList) {
            relevancePrepStatement.setInt(parameterIndexForRel++, pageId);
        }
        for (int pageId : pageIdList) {
            relevancePrepStatement.setInt(parameterIndexForRel++, pageId);
        }
        for (int pageId : lemmaIdList) {
            relevancePrepStatement.setInt(parameterIndexForRel++, pageId);
        }

        String getPageData = createMultiplePageQuery(pageIdList.size());
        PreparedStatement pageDataPrepStatement = connection.prepareStatement(getPageData);
        int parameterIndexForPageData = 1;
        for (int pageId : pageIdList) {
            pageDataPrepStatement.setInt(parameterIndexForPageData++, pageId);
        }

        ResultSet pagesRelResultSet = relevancePrepStatement.executeQuery();
        ResultSet pageDataResultSet = pageDataPrepStatement.executeQuery();

        ArrayList<PageSearchData> pageSearchDataList = new ArrayList<>();
        while (pagesRelResultSet.next()){
            pageDataResultSet.next();
            PageSearchData page = new PageSearchData(pagesRelResultSet.getInt(1),
                    pageDataResultSet.getString(2), pageDataResultSet.getString(3),
                    pagesRelResultSet.getFloat(2));
            pageSearchDataList.add(page);
        }

        pagesRelResultSet.close();
        relevancePrepStatement.close();
        pageDataResultSet.close();
        pageDataPrepStatement.close();
        return pageSearchDataList;
    }

    private static String createMultipleRelevanceQuery(int pageIdLength, int lemmaIdLength) {
        String query = "WHERE page_id IN (";
        StringBuilder queryBuilder = new StringBuilder(query);
        appendValueForQuery(queryBuilder, pageIdLength);
        queryBuilder.append(" ) AND lemma_id IN (");
        appendValueForQuery(queryBuilder, lemmaIdLength);
        queryBuilder.append(")");

        return queryBuilder.toString();
    }

    private static String createMultiplePageQuery(int length) {
        String query = "SELECT id, `path`, content from _page WHERE id IN(";
        StringBuilder queryBuilder = new StringBuilder(query);
        appendValueForQuery(queryBuilder, length);
        queryBuilder.append(" ) ORDER BY id");
        return queryBuilder.toString();
    }

    private static void appendValueForQuery(StringBuilder queryBuilder, int length) {
        for (int i = 0; i < length; i++) {
            queryBuilder.append(" ?");
            if (i != length - 1)
                queryBuilder.append(",");
        }
    }

    public static ArrayList<Integer> resultSetToList(ResultSet resultSet) throws SQLException {
        ArrayList<Integer> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(resultSet.getInt(1));
        }
        resultSet.close();
        return list;
    }

}
