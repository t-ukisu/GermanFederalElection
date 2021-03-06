package dao;

import bean.secondstage.StateDistributionInfoDto;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import util.Util;

/**
 * 政党情報テーブルのDAOクラス
 *
 * @author dev6905768cd
 */
public class PartyInfoDao {

    private static final String SELECT_PARTY_WITHOUT_DISTRIBUTION = "SELECT PARTY FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YAER_ID WHERE ELECTION_YEAR = ? GROUP BY PARTY HAVING PARTY = ''"/* + " OR (SUM(CONSTITUENCY_SEAT) < 3  AND SUM(SECOND_VOTE) < (SELECT SUM(SECOND_VOTE) * 5 / 100 FROM PARTY_INFO))"/**/;

    private static final String SELECT_STATE_INDEPENDENT_CONSTITUENCY_SEATS = "SELECT PARTY, \"STATE\", CONSTITUENCY_SEAT FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YAER_ID WHERE ELECTION_YEAR = ? AND PARTY IN (" + SELECT_PARTY_WITHOUT_DISTRIBUTION + ") AND CONSTITUENCY_SEAT > 0";

    private static final String SELECT_PARTY_SECOND_VOTES_BY_STATE = "SELECT PARTY, SECOND_VOTE FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YAER_ID WHERE ELECTION_YEAR = ? AND \"STATE\" = ? AND PARTY NOT IN (" + SELECT_PARTY_WITHOUT_DISTRIBUTION + ")";

    private static final String SELECT_STATE_CONSTITUENCY_SEATS_BY_PARTY = "SELECT \"STATE\", CONSTITUENCY_SEAT FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YAER_ID WHERE ELECTION_YEAR = ? AND PARTY = ?";

    private static final String SELECT_SECOND_VOTE_SUM_BY_PARTY = "SELECT PARTY, SUM(SECOND_VOTE) FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YAER_ID WHERE ELECTION_YEAR = ? AND PARTY NOT IN (" + SELECT_PARTY_WITHOUT_DISTRIBUTION + ") GROUP BY PARTY";

    private static final String SELECT_STATE_SECOND_VOTES_BY_PARTY = "SELECT \"STATE\", SECOND_VOTE, CONSTITUENCY_SEAT FROM PARTY_INFO P INNER JOIN YEAR_MASTER Y ON P.YEAR_ID = Y.YAER_ID WHERE ELECTION_YEAR = ? AND PARTY = ?";

    /**
     *
     * @param year
     * @return
     * @throws SQLException
     */
    public static Map<String, Map<String, Integer>> getStateIndependentConstituencySeats(int year) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_INDEPENDENT_CONSTITUENCY_SEATS)) {
            preparedStatement.setInt(1, year);
            preparedStatement.setInt(2, year);
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
     *
     * @param year
     * @param state
     * @return
     * @throws java.sql.SQLException
     */
    public static Map<String, Integer> getPartySecondVotesMapByState(int year, String state) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_PARTY_SECOND_VOTES_BY_STATE)) {
            preparedStatement.setInt(1, year);
            preparedStatement.setString(2, state);
            preparedStatement.setInt(3, year);
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
     * @param year
     * @param party 政党
     * @return 各州の選挙区獲得議席数を保持するMap
     * @throws SQLException
     */
    public static Map<String, Integer> getStateConstituencySeatsByParty(int year, String party) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_CONSTITUENCY_SEATS_BY_PARTY)) {
            preparedStatement.setInt(1, year);
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
     *
     * @param year
     * @return @throws SQLException
     */
    public static Map<String, Integer> getPartySecondVotesMap(int year) throws SQLException {
        try (PreparedStatement preparedStatemtent = DBConnectionManager.CONNECTION.prepareStatement(SELECT_SECOND_VOTE_SUM_BY_PARTY)) {
            preparedStatemtent.setInt(1, year);
            preparedStatemtent.setInt(2, year);
            try (ResultSet resultSet = preparedStatemtent.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .collect(Collectors.toMap(
                                rs -> DaoUtil.getString(rs, 1),
                                rs -> DaoUtil.getInt(rs, 2)));
            }
        }
    }

    /**
     *
     * @param year
     * @param party
     * @return
     * @throws SQLException
     */
    public static List<StateDistributionInfoDto> getStateInfoListByParty(int year, String party) throws SQLException {
        try (PreparedStatement preparedStatement = DBConnectionManager.CONNECTION.prepareStatement(SELECT_STATE_SECOND_VOTES_BY_PARTY)) {
            preparedStatement.setInt(1, year);
            preparedStatement.setString(2, party);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return DaoUtil.createResultSetStream(resultSet)
                        .map(rs -> new StateDistributionInfoDto(DaoUtil.getString(rs, 1), DaoUtil.getInt(rs, 2), DaoUtil.getInt(rs, 3)))
                        .collect(Collectors.toList());
            }
        }
    }
}
