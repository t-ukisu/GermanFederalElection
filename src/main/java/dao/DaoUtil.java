package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author dev6905768cd
 */
public class DaoUtil {

    /**
     * ResultSetのStreamを生成して返す
     *
     * @param rs ResultSet
     * @return ResultSetのStream
     */
    public static Stream<ResultSet> createResultSetStream(ResultSet rs) {

        Iterator<ResultSet> resultSetIterator = createResultSetIterator(rs);

        Spliterator<ResultSet> resultSetSpliterator = Spliterators.spliteratorUnknownSize(resultSetIterator, Spliterator.ORDERED | Spliterator.NONNULL);

        return StreamSupport.stream(resultSetSpliterator, false);
    }

    /**
     *
     * @param rs
     * @param columnIndex
     * @return
     */
    public static String getString(ResultSet rs, int columnIndex) {
        try {
            return rs.getString(columnIndex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     *
     * @param rs
     * @param columnIndex
     * @return
     */
    public static int getInt(ResultSet rs, int columnIndex) {
        try {
            return rs.getInt(columnIndex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * ResultSetのイテレータを生成して返す
     *
     * @param rs ResultSet
     * @return ResultSetのイテレータ
     */
    private static Iterator<ResultSet> createResultSetIterator(ResultSet rs) {
        return new Iterator<ResultSet>() {

            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public ResultSet next() {
                return rs;
            }

        };
    }
}
