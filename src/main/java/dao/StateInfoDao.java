package dao;

import bean.simulator.StateSimulatorDto;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 州情報テーブルのDAOクラス
 *
 * @author dev6905768cd
 */
public class StateInfoDao {

    private static final String SELECT_STATE_POPULATION = "SELECT \"STATE\", POPULATION FROM STATE_INFO S INNER JOIN YEAR_MASTER Y ON S.YEAR_ID = Y.YEAR_ID WHERE ELECTION_YEAR = ?";

    private static final String SELECT_DISTINCT_STATE = "SELECT DISTINCT \"STATE\" FROM STATE_INFO";

    /**
     * 各州の人口を取得する
     *
     * @param year
     * @return 各州の人口を保持するMap
     * @throws SQLException
     */
    public static Map<String, Integer> getStatePopulationMap(String year) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_POPULATION)) {
            preparedStatement.setString(1, year);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .collect(Collectors.toMap(
                                r -> DaoUtil.getString(r, 1),
                                r -> DaoUtil.getInt(r, 2)));
            }
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public static List<StateSimulatorDto> getStates() throws SQLException {
        try (ResultSet resultSet = DBConnectionManager.STATEMENT.executeQuery(SELECT_DISTINCT_STATE)) {
            return DaoUtil.createResultSetStream(resultSet)
                    .map(rs -> new StateSimulatorDto(DaoUtil.getString(rs, 1)))
                    .collect(Collectors.toList());
        }
    }
}
