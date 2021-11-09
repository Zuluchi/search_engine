import java.sql.*;
import java.util.ArrayList;

public class DataBase {
    private static final String dbName = "search_engine";
    private static final String dbUser = "root";
    private static final String dbPass = "password";
    private static Connection connection;

    public static Connection getDBConnection() {

        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/" + dbName +
                                "?user=" + dbUser + "&password=" + dbPass
                                + "&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&" +
                                "useUnicode=true&characterEncoding=UTF-8");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void createTableForPage(String url) throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS `" + url + "`");
        connection.createStatement().execute("CREATE TABLE `" + url +
                "` (id INT NOT NULL AUTO_INCREMENT, " +
                "path TEXT NOT NULL, " +
                "code INT NOT NULL, " +
                "content MEDIUMTEXT NOT NULL, " +
                "PRIMARY KEY(id), " +
                "UNIQUE KEY path(path(500)))");
        connection.setAutoCommit(false);
    }

    public static void insertPage(String table, String path, int code, String content) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `" + table
                + "` (path, code, content) VALUES ( ?, ?, ?)");
        preparedStatement.setString(1, path);
        preparedStatement.setInt(2, code);
        preparedStatement.setString(3, content);
        preparedStatement.execute();
        connection.commit();
    }
}
