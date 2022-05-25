package dao;

import bean.secondstage.StateDistributionInfoDto;
import util.Util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 政党情報テーブルのDAOクラス
 *
 * @author dev6905768cd
 */
public class PartyInfoDao {

    private static final String SELECT_PARTY_WITHOUT_DISTRIBUTION = "SELECT PARTY FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ? GROUP BY PARTY HAVING PARTY = ''"/* + " OR (SUM(CONSTITUENCY_SEAT) < 3  AND SUM(SECOND_VOTE) < (SELECT SUM(SECOND_VOTE) * 5 / 100 FROM PARTY_INFO))"/**/;

    private static final String SELECT_STATE_INDEPENDENT_CONSTITUENCY_SEATS = "SELECT PARTY, \"STATE\", CONSTITUENCY_SEAT FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ? AND PARTY IN (" + SELECT_PARTY_WITHOUT_DISTRIBUTION + ") AND CONSTITUENCY_SEAT > 0";

    private static final String SELECT_PARTY_SECOND_VOTES_BY_STATE = "SELECT PARTY, SECOND_VOTE FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ? AND \"STATE\" = ? AND PARTY NOT IN (" + SELECT_PARTY_WITHOUT_DISTRIBUTION + ")";

    private static final String SELECT_STATE_CONSTITUENCY_SEATS_BY_PARTY = "SELECT \"STATE\", CONSTITUENCY_SEAT FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ? AND PARTY = ?";

    private static final String SELECT_SECOND_VOTE_SUM_BY_PARTY = "SELECT PARTY, SUM(SECOND_VOTE) FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ? AND PARTY NOT IN (" + SELECT_PARTY_WITHOUT_DISTRIBUTION + ") GROUP BY PARTY";

    private static final String SELECT_STATE_SECOND_VOTES_BY_PARTY = "SELECT \"STATE\", SECOND_VOTE, CONSTITUENCY_SEAT FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ? AND PARTY = ?";

    private static final String SELECT_CONSTITUENCY_SEAT_SUM_BY_YEAR = "SELECT SUM(CONSTITUENCY_SEAT) FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YEAR_ID WHERE Y.YEAR_ID = ?";

    /**
     * @param yearId
     * @return
     * @throws SQLException
     */
    public static Map<String, Map<String, Integer>> getStateIndependentConstituencySeats(int yearId) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_INDEPENDENT_CONSTITUENCY_SEATS)) {
            preparedStatement.setInt(1, yearId);
            preparedStatement.setInt(2, yearId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .collect(Collectors.toMap(
                                r -> DaoUtil.getString(r, 1),
                                r -> Util.createMap(DaoUtil.getString(r, 2), DaoUtil.getInt(r, 3)),
                                Util::copyMap));
            }
        }
    }

    /**
     * @param yearId
     * @param state
     * @return
     * @throws java.sql.SQLException
     */
    public static Map<String, Integer> getPartySecondVotesMapByState(int yearId, String state) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_PARTY_SECOND_VOTES_BY_STATE)) {
            preparedStatement.setInt(1, yearId);
            preparedStatement.setString(2, state);
            preparedStatement.setInt(3, yearId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .collect(Collectors.toMap(
                                r -> DaoUtil.getString(r, 1),
                                r -> DaoUtil.getInt(r, 2)));
            }
        }
    }

    /**
     * 引数に渡された政党の各州の選挙区獲得議席数を取得する
     *
     * @param yearId
     * @param party  政党
     * @return 各州の選挙区獲得議席数を保持するMap
     * @throws SQLException
     */
    public static Map<String, Integer> getStateConstituencySeatsByParty(int yearId, String party) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_CONSTITUENCY_SEATS_BY_PARTY)) {
            preparedStatement.setInt(1, yearId);
            preparedStatement.setString(2, party);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .collect(Collectors.toMap(
                                rs -> DaoUtil.getString(rs, 1),
                                rs -> DaoUtil.getInt(rs, 2)));
            }
        }
    }

    /**
     * @param yearId
     * @return @throws SQLException
     */
    public static Map<String, Integer> getPartySecondVotesMap(int yearId) throws SQLException {
        try (PreparedStatement preparedStatemtent = DBConnectionManager.CONNECTION.prepareStatement(SELECT_SECOND_VOTE_SUM_BY_PARTY)) {
            preparedStatemtent.setInt(1, yearId);
            preparedStatemtent.setInt(2, yearId);
            try (ResultSet resultSet = preparedStatemtent.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .collect(Collectors.toMap(
                                rs -> DaoUtil.getString(rs, 1),
                                rs -> DaoUtil.getInt(rs, 2)));
            }
        }
    }

    /**
     * @param yearId
     * @param party
     * @return
     * @throws SQLException
     */
    public static List<StateDistributionInfoDto> getStateInfoListByParty(int yearId, String party) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_SECOND_VOTES_BY_PARTY)) {
            preparedStatement.setInt(1, yearId);
            preparedStatement.setString(2, party);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .map(rs -> new StateDistributionInfoDto(DaoUtil.getString(rs, 1), DaoUtil.getInt(rs, 2), DaoUtil.getInt(rs, 3)))
                        .collect(Collectors.toList());
            }
        }
    }

    public static int getConstituencySeatSumByYear(int yearId) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_CONSTITUENCY_SEAT_SUM_BY_YEAR)) {
            preparedStatement.setInt(1, yearId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return DaoUtil.getInt(resultSet, 1);
            }
        }
    }
}
