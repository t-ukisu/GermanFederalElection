package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dev6905768cd
 */
public class YearMasterDao {

    private static final String SELECT_ELECTION_YEAR_QUERY = "SELECT ELECTION_YEAR FROM YEAR_MASTER ORDER BY ELECTION_YEAR";

    public static List<String> getYears() throws SQLException {
        try (ResultSet resultSet = DBConnectionManager.STATEMENT.executeQuery(SELECT_ELECTION_YEAR_QUERY)) {
            return DaoUtil.createResultSetStream(resultSet)
                    .map(rs -> DaoUtil.getString(rs, 1))
                    .collect(Collectors.toList());
        }
    }
}
