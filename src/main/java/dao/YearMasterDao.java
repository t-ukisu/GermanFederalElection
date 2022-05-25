package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dev6905768cd
 */
public class YearMasterDao {

    private static final String SELECT_ELECTION_YEAR_QUERY = "SELECT YEAR_ID, ELECTION_YEAR FROM YEAR_MASTER ORDER BY ELECTION_YEAR";

    public static Map<Integer, String> getYears() throws SQLException {
        try (ResultSet resultSet = DBConnectionManager.STATEMENT.executeQuery(SELECT_ELECTION_YEAR_QUERY)) {
            return DaoUtil.createResultSetStream(resultSet)
                    .collect(Collectors.toMap(
                            r -> DaoUtil.getInt(r, 1),
                            r -> DaoUtil.getString(r, 2),
                            (v1, v2) -> v1,
                            LinkedHashMap::new));
        }
    }
}
