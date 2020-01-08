package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DB接続管理クラス
 *
 * @author dev6905768cd
 */
public class DBConnectionManager {

    /**
     * DBとの接続を扱うオブジェクト
     */
    public static final Connection CONNECTION;

    /**
     * SQLを実行し結果を返すオブジェクト
     */
    public static final Statement STATEMENT;

    /**
     * 接続するDBのURL
     */
    private static final String URL = "jdbc:derby://localhost:1527/GermanFederalElectionWeb";

    /**
     * 接続するDBのユーザー名
     */
    private static final String USER = "App";

    /**
     * 接続するDBのパスワード
     */
    private static final String PASSWORD = "App";

    /**
     * DBと接続する
     */
    static {
        try {
            CONNECTION = DriverManager.getConnection(URL, USER, PASSWORD);
            STATEMENT = CONNECTION.createStatement();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * DBとの接続を切断する
     *
     * @throws SQLException
     */
    public static void close() throws SQLException {
        STATEMENT.close();
        CONNECTION.close();
    }
}
