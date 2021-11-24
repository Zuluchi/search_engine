import java.sql.*;
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

    private static void createTablePage() throws SQLException {
        String dropIndexQuery = "DROP TABLE IF EXISTS _index;";
        String dropTableQuery = "DROP TABLE IF EXISTS _page;";
        String createTableQuery = "CREATE TABLE _page " +
                "(id INT NOT NULL AUTO_INCREMENT, " +
                "path TEXT NOT NULL, " +
                "code INT NOT NULL, " +
                "content MEDIUMTEXT NOT NULL, " +
                "PRIMARY KEY(id), " +
                "UNIQUE KEY path(path(500)));";

        Statement dropAndCreateTables = connection.createStatement();
        dropAndCreateTables.addBatch(dropIndexQuery);
        dropAndCreateTables.addBatch(dropTableQuery);
        dropAndCreateTables.addBatch(createTableQuery);
        dropAndCreateTables.executeBatch();
        connection.commit();
        dropAndCreateTables.close();
    }

    private static void createTableFieldWithDefaultValues() throws SQLException {
        String dropField = "DROP TABLE IF EXISTS _field;";
        String createField = "CREATE TABLE _field " +
                "(id INT NOT NULL AUTO_INCREMENT, " +
                "name VARCHAR(255) NOT NULL, " +
                "selector VARCHAR(255) NOT NULL, " +
                "weight FLOAT NOT NULL," +
                "PRIMARY KEY(id));";
        String insertIntoField = "INSERT INTO _field " +
                "(name, selector, weight) VALUES ('title', 'title', 1.0),('body', 'body', 0.8);";

        Statement dropAndCreateField = connection.createStatement();
        dropAndCreateField.addBatch(dropField);
        dropAndCreateField.addBatch(createField);
        dropAndCreateField.addBatch(insertIntoField);
        dropAndCreateField.executeBatch();
        connection.commit();
        dropAndCreateField.close();
    }

    private static void createTableLemma() throws SQLException {
        String dropLemma = "DROP TABLE IF EXISTS _lemma;";
        String createLemma = "CREATE TABLE _lemma " +
                "(id INT NOT NULL AUTO_INCREMENT, " +
                "lemma VARCHAR(255) NOT NULL, " +
                "frequency INT NOT NULL," +
                "PRIMARY KEY(id)," +
                "UNIQUE KEY lemma(lemma(255)));";

        Statement dropAndCreateLemma = connection.createStatement();
        dropAndCreateLemma.addBatch(dropLemma);
        dropAndCreateLemma.addBatch(createLemma);
        dropAndCreateLemma.executeBatch();
        connection.commit();
        dropAndCreateLemma.close();
    }

    private static void createTableIndex() throws SQLException {
        String createIndex = "CREATE TABLE _index" +
                "(id INT NOT NULL AUTO_INCREMENT, " +
                "page_id INT NOT NULL, " +
                "field_id INT NOT NULL, " +
                "lemma_id INT NOT NULL, " +
                "lemma_rank float NOT NULL," +
                "PRIMARY KEY(id)," +
                "FOREIGN KEY (page_id) REFERENCES _page(id)," +
                "FOREIGN KEY (field_id) REFERENCES _field(id)," +
                "FOREIGN KEY (lemma_id) REFERENCES _lemma(id));";

        Statement createIndexSt = connection.createStatement();
        createIndexSt.execute(createIndex);
        connection.commit();
        createIndexSt.close();
    }

    public static void createTables() throws SQLException {
        createTablePage();
        createTableFieldWithDefaultValues();
        createTableLemma();
        createTableIndex();
    }

    public static int insertPageAndGetId(String path, int code, String content) throws SQLException {
        String query = "INSERT INTO _page "
                + " (path, code, content) VALUES ( ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, path);
        preparedStatement.setInt(2, code);
        preparedStatement.setString(3, content);
        preparedStatement.execute();
        ResultSet pageIdResultSet = preparedStatement.getGeneratedKeys();
        pageIdResultSet.next();
        int pageId = pageIdResultSet.getInt(1);
        pageIdResultSet.close();
        preparedStatement.close();

        connection.commit();
        return pageId;
    }

    public static Map<String, Integer> insertLemmsAndGetId(Set<String> lemmaSet) throws SQLException {
        Map<String, Integer> lemmaMap = new HashMap<>();
        String insertQuery = "INSERT INTO _lemma"
                + " (lemma, frequency) VALUES ( ?, ?) ON DUPLICATE KEY UPDATE frequency = frequency + 1;";

        PreparedStatement insertLemmaData = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
        for (String lemma : lemmaSet) {
            insertLemmaData.setString(1, lemma);
            insertLemmaData.setInt(2, 1);
            insertLemmaData.addBatch();
        }
        insertLemmaData.executeBatch();
        connection.commit();

        ResultSet insertedIds = insertLemmaData.getGeneratedKeys();
        for (String lemma : lemmaSet) {
            insertedIds.next();
            lemmaMap.put(lemma, insertedIds.getInt(1));
        }
        insertedIds.close();
        insertLemmaData.close();

        return lemmaMap;
    }

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
}
