package logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author dev6905768cd
 */
public class ElectionMethodLogic {

    /**
     * 数字の0.5を扱うためのBigDecimal型定数
     */
    private static final BigDecimal ZERO_POINT_FIVE = new BigDecimal("0.5");

    /**
     * 数字の1.5を扱うためのBigDecimal型定数
     */
    private static final BigDecimal ONE_POINT_FIVE = new BigDecimal("1.5");

    /**
     * 配分対象の値の割合に応じて、議席配分を行う
     *
     * @param targetMap 配分対象の値をもつMap
     * @param seatsNumber 配分する議席数
     * @return 配分対象ごとの議席数をもつMap
     */
    public static BigDecimal calculateSelectedDivisor(Map<String, Integer> targetMap, int seatsNumber) {

        // 配分対象の値の合計値を求める
        int sum = sumValues(targetMap);

        // 配分対象の値の合計値を議席数で割り、商を小数点第4位で四捨五入して開始除数を求める
        BigDecimal initialDivisor = divide(sum, new BigDecimal(seatsNumber), 3, RoundingMode.HALF_UP, false);

        // 配分対象の値をそれぞれ開始除数で割り、商を小数点以下で四捨五入する
        Map<String, Integer> seatsNumberMap = calculateSeatsByTarget(targetMap, initialDivisor, 3);

        // 四捨五入した商を合計する
        int quotSum = sumValues(seatsNumberMap);

        // (商の集計結果と配分議席数が等しい かつ 初回計算) または 商の集計結果と配分議席数が等しくない 場合ループする
        BigDecimal selectedDivisor = null;
        while (quotSum != seatsNumber || Objects.isNull(selectedDivisor)) {

            // 選択除数を決定する
            selectedDivisor = decideSelectedDivisor(targetMap, seatsNumberMap, Integer.compare(quotSum, seatsNumber));

            // 配分対象の値をそれぞれ選択した除数で割り、商を小数点以下で四捨五入する
            seatsNumberMap = calculateSeatsByTarget(targetMap, selectedDivisor, 3);

            // 四捨五入した商を合計する
            quotSum = sumValues(seatsNumberMap);
        }

        return selectedDivisor;
    }

    /**
     * 配分対象の配分議席数が最低保障議席数を下回らないよう調整議席を配分する
     *
     * @param targetMap 配分対象の値をもつMap
     * @param minimumSeatsMap 配分対象の最低保障議席数をもつMap
     * @return 配分対象が最低保障議席数を下回らないよう比例配分された議席数をもつMap
     */
    public static BigDecimal distributeGuaranteedMinimumSeats(Map<String, Integer> targetMap, Map<String, Integer> minimumSeatsMap) {

        // 配分対象の比例変数を、最低保障議席から0.5を減じた数で割り、最小の( <=> 1議席あたりの比例変数が最も少ない)商を求める
        BigDecimal upperLimit = targetMap.entrySet().stream()
                .map(entry -> divideProportionalValueMinus(minimumSeatsMap, entry))
                .min(Comparator.nullsLast(Comparator.naturalOrder()))
                .orElse(new BigDecimal(Integer.MAX_VALUE));

        // 配分対象の比例変数を、上で求めた最小の商で割り、各配分対象の1議席あたりの比例変数を均等になるよう議席配分を行う
        Map<String, Integer> seatsNumberMap = targetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> divide(targetMap.get(entry.getKey()), upperLimit, 3, RoundingMode.HALF_UP, true).intValue()));

        // 配分対象の比例変数を、上で配分された議席数に0.5を加えた数で割り、最大の( <=> 1議席あたりの比例変数が最も多い)商を求める
        BigDecimal lowerLimit = targetMap.entrySet().stream()
                .map(entry -> divideProportionalValuePlus(seatsNumberMap, entry))
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        // 選択除数を決定する
        BigDecimal selectedDivisor = selectSmoothestDivisor(lowerLimit, upperLimit);

        // 配分対象の値をそれぞれ選択した除数で割り、商を小数点以下で四捨五入する
        Map<String, Integer> verifyingMap = calculateSeatsByTarget(targetMap, selectedDivisor, 3);

        // 選択除数で割った商が、配分議席数と等しいか検証する
        seatsNumberMap.forEach((key, value) -> verifySeatsNumber(verifyingMap, key, value));

        return selectedDivisor;
    }

    /**
     * 配分対象が最低議席数を下回らないように総議席を配分する
     *
     * @param targetMap 配分対象の値をもつMap
     * @param seatsNumber 配分する議席数
     * @param targetMinimumSeatsMap 配分対象の最低議席数をもつMap
     * @return 配分対象ごとの議席数をもつMap
     */
    public static BigDecimal distributeAllSeatsByTargetMinimumSeats(Map<String, Integer> targetMap, int seatsNumber, Map<String, Integer> targetMinimumSeatsMap) {

        // 配分対象の値の合計値を求める
        int sum = sumValues(targetMap);

        // 配分対象の値の合計値を議席数で割り、商を小数点第4位で四捨五入して開始除数を求める
        BigDecimal initialDivisor = divide(sum, new BigDecimal(seatsNumber), 3, RoundingMode.HALF_UP, false);

        // 配分対象の値をそれぞれ開始除数で割り、商を小数点以下で四捨五入する
        Map<String, Integer> seatsNumberMap = calculateSeatsByTarget(targetMap, initialDivisor, 3);

        // 比例配分の商と最低獲得議席のうち、大きいほうをその配分対象の議席とする
        Map<String, Integer> targetSeatsMap = getGreaterSeatsMap(seatsNumberMap, targetMinimumSeatsMap);

        // 四捨五入した商を合計する
        int quotSum = sumValues(targetSeatsMap);

        // 商の集計結果と配分議席数が等しくない場合ループする
        BigDecimal selectedDivisor = null;
        while (quotSum != seatsNumber || selectedDivisor == null) {

            // 最低獲得議席数より比例配分議席数が上回った組み合わせを抽出する
            Map<String, Integer> seatsOverMinimumMap = seatsNumberMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > targetMinimumSeatsMap.get(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 選択除数を決定する
            selectedDivisor = decideSelectedDivisor(targetMap, seatsOverMinimumMap, Integer.compare(quotSum, seatsNumber));

            // 配分対象の値をそれぞれ選択した除数で割り、商を小数点以下で四捨五入する
            seatsNumberMap = calculateSeatsByTarget(targetMap, selectedDivisor, 0);

            // 比例配分の商と最低獲得議席のうち、大きいほうをその配分対象の議席とする
            targetSeatsMap = getGreaterSeatsMap(seatsNumberMap, targetMinimumSeatsMap);

            // 四捨五入した商を合計する
            quotSum = sumValues(targetSeatsMap);
        }

        return selectedDivisor;
    }

    /**
     * Mapの値を合計する
     *
     * @param intValueMap 数値を値に持つMap
     * @return 合計値
     */
    private static int sumValues(Map<String, Integer> intValueMap) {
        return intValueMap.values().stream()
                .mapToInt(i -> i)
                .sum();
    }

    /**
     * 指定された条件で除算を行い、その後四捨五入した整数値を返す
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @param scale 商のスケール
     * @param roundingMode 丸めモード
     * @param isToRound 商を四捨五入するか
     * @return dividend / divisor
     */
    private static BigDecimal divide(int dividend, BigDecimal divisor, int scale, RoundingMode roundingMode, boolean isToRound) {
        BigDecimal quot = new BigDecimal(dividend).divide(divisor, scale, roundingMode);
        return isToRound ? quot.setScale(0, RoundingMode.HALF_UP) : quot;
    }

    /**
     * 議席配分対象の比例変数を除数で割り配分議席数を計算する
     *
     * @param targetMap 配分対象の値をもつMap
     * @param divisor 除数
     * @param scale 値を保持する小数点以下の位の数
     * @return 配分対象ごとの議席数をもつMap
     */
    private static Map<String, Integer> calculateSeatsByTarget(Map<String, Integer> targetMap, BigDecimal divisor, int scale) {
        return targetMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> divide(entry.getValue(), divisor, scale, RoundingMode.HALF_UP, true).intValue()));
    }

    /**
     * 選択除数を決定する
     *
     * @param targetMap 配分対象の値をもつMap
     * @param seatsNumberMap 配分対象ごとの議席数をもつMap
     * @param differenceFromSeats 商の集計結果と配分議席数の差
     * @return 選択除数
     */
    private static BigDecimal decideSelectedDivisor(Map<String, Integer> targetMap, Map<String, Integer> seatsNumberMap, int differenceFromSeats) {
        List<TargetValueSeatsDto> targetValueSeatsMap = getTargetValueSeatsMap(targetMap, seatsNumberMap);
        List<BigDecimal> divisorBorderList;
        if (differenceFromSeats == 0) {
            divisorBorderList = getDivisorBorderListEqualQuot(targetValueSeatsMap);
        } else if (differenceFromSeats > 0) {
            divisorBorderList = getDivisorBorderListGreaterQuot(targetValueSeatsMap);
        } else {
            divisorBorderList = getDivisorBorderListLessQuot(targetValueSeatsMap);
        }

        return selectSmoothestDivisor(divisorBorderList.get(0), divisorBorderList.get(1));
    }

    /**
     * 配分対象の値をキーに、配分対象ごとの議席数を値に持つMapを返す
     *
     * @param targetMap 配分対象の値をもつMap
     * @param seatsNumberMap 配分対象ごとの議席数をもつMap
     * @return 配分対象の値をキーに、配分対象ごとの議席数を値に持つMap
     */
    private static List<TargetValueSeatsDto> getTargetValueSeatsMap(Map<String, Integer> targetMap, Map<String, Integer> seatsNumberMap) {
        return targetMap.entrySet().stream()
                .filter(entry -> Objects.nonNull(seatsNumberMap.get(entry.getKey())))
                .map(entry -> new TargetValueSeatsDto(new BigDecimal(entry.getValue()), new BigDecimal(seatsNumberMap.get(entry.getKey()))))
                .collect(Collectors.toList());
    }

    /**
     * 商の集計結果と配分議席数が等しい場合の、選択除数の境界値のListを取得する
     *
     * @param targetValueSeatsMap 配分対象の値をキーに、配分対象ごとの議席数を値に持つMap
     * @return 選択除数の下限を1つ目、上限を2つ目の要素にもつList
     */
    private static List<BigDecimal> getDivisorBorderListEqualQuot(List<TargetValueSeatsDto> targetValueSeatsMap) {
        // 配分対象ごとに比例変数を、前回商から0.5を減じた数、、前回商に0.5を加えた数でそれぞれ除算
        List<BigDecimal> list1 = getDivisorCandidateListMinus(targetValueSeatsMap, ZERO_POINT_FIVE);
        List<BigDecimal> sortedList1 = list1.stream()
                .sorted(Comparator.naturalOrder())
                .distinct()
                .collect(Collectors.toList());

        List<BigDecimal> list2 = getDivisorCandidateListPlus(targetValueSeatsMap, ZERO_POINT_FIVE);
        List<BigDecimal> sortedList2 = list2.stream()
                .sorted(Comparator.reverseOrder())
                .distinct()
                .collect(Collectors.toList());

        List<BigDecimal> divisorBorderList = new ArrayList<>();
        divisorBorderList.add(0, sortedList2.get(0));
        divisorBorderList.add(1, sortedList1.get(0));
        return divisorBorderList;
    }

    /**
     * 商の集計結果が配分議席数より大きい場合の、選択除数の境界値のListを取得する
     *
     * @param targetValueSeatsMap 配分対象の値をキーに、配分対象ごとの議席数を値に持つMap
     * @return 選択除数の下限を1つ目、上限を2つ目の要素にもつList
     */
    private static List<BigDecimal> getDivisorBorderListGreaterQuot(List<TargetValueSeatsDto> targetValueSeatsMap) {
        // 配分対象ごとに比例変数を、前回商から0.5を減じた数、、前回商から1.5を減じた数でそれぞれ除算
        List<BigDecimal> list1 = getDivisorCandidateListMinus(targetValueSeatsMap, ZERO_POINT_FIVE);
        List<BigDecimal> list2 = getDivisorCandidateListMinus(targetValueSeatsMap, ONE_POINT_FIVE);
        list1.addAll(list2);
        List<BigDecimal> sortedList = list1.stream()
                .sorted(Comparator.naturalOrder())
                .distinct()
                .collect(Collectors.toList());

        List<BigDecimal> divisorBorderList = new ArrayList<>();
        divisorBorderList.add(0, sortedList.get(0));
        divisorBorderList.add(1, sortedList.get(1));
        return divisorBorderList;
    }

    /**
     * 商の集計結果が配分議席数より小さい場合の、選択除数の境界値のListを取得する
     *
     * @param targetValueSeatsMap 配分対象の値をキーに、配分対象ごとの議席数を値に持つMap
     * @return 選択除数の下限を1つ目、上限を2つ目の要素にもつList
     */
    private static List<BigDecimal> getDivisorBorderListLessQuot(List<TargetValueSeatsDto> targetValueSeatsMap) {
        // 配分対象ごとに比例変数を、前回商に0.5を加えた数、前回商に1.5を加えた数でそれぞれ除算
        List<BigDecimal> list1 = getDivisorCandidateListPlus(targetValueSeatsMap, ZERO_POINT_FIVE);
        List<BigDecimal> list2 = getDivisorCandidateListPlus(targetValueSeatsMap, ONE_POINT_FIVE);
        list1.addAll(list2);
        List<BigDecimal> sortedList = list1.stream()
                .sorted(Comparator.reverseOrder())
                .distinct()
                .collect(Collectors.toList());

        List<BigDecimal> divisorBorderList = new ArrayList<>();
        divisorBorderList.add(0, sortedList.get(1));
        divisorBorderList.add(1, sortedList.get(0));
        return divisorBorderList;
    }

    /**
     * 配分対象の値から減算した値を用いて除数候補のListを取得する
     *
     * @param targetValueSeatsMap 配分対象の値をキーに、配分対象ごとの議席数を値に持つMap
     * @param divisor 減数
     * @return 除数候補のList
     */
    private static List<BigDecimal> getDivisorCandidateListMinus(List<TargetValueSeatsDto> targetValueSeatsMap, BigDecimal divisor) {
        return targetValueSeatsMap.stream()
                .filter(entry -> entry.getSeats().subtract(divisor).compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> entry.getTargetValue().divide(entry.getSeats().subtract(divisor), 3, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
    }

    /**
     * 配分対象の値に加算した値を用いて除数候補のListを取得する
     *
     * @param targetValueSeatsMap 配分対象の値をキーに、配分対象ごとの議席数を値に持つMap
     * @param divisor 加数
     * @return 除数候補のList
     */
    private static List<BigDecimal> getDivisorCandidateListPlus(List<TargetValueSeatsDto> targetValueSeatsMap, BigDecimal divisor) {
        return targetValueSeatsMap.stream()
                .map(entry -> entry.getTargetValue().divide(entry.getSeats().add(divisor), 3, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
    }

    /**
     * 下限より大きく上限以下の範囲で、できるだけキリのいい数を求める
     *
     * @param lowerLimit 下限
     * @param upperLimit 上限
     * @return 選択除数
     */
    private static BigDecimal selectSmoothestDivisor(BigDecimal lowerLimit, BigDecimal upperLimit) {

        // 下限・上限の小数点以下を切り捨てる
        BigDecimal lowerLimitForCalc = lowerLimit.setScale(0, RoundingMode.FLOOR);
        BigDecimal upperLimitForCalc = upperLimit.setScale(0, RoundingMode.FLOOR);

        if (lowerLimitForCalc.compareTo(upperLimitForCalc) == 0) {
            return upperLimit;
        }

        BigDecimal divisorForCalc = BigDecimal.ONE;
        // 下限と上限が等しくなるまで、以下の処理を続ける
        while (upperLimitForCalc.compareTo(lowerLimitForCalc) > 0) {

            // 除数を10倍する
            divisorForCalc = divisorForCalc.multiply(BigDecimal.TEN);

            // 下限と上限をそれぞれ除数で割る
            lowerLimitForCalc = lowerLimit.divide(divisorForCalc, 0, RoundingMode.FLOOR);
            upperLimitForCalc = upperLimit.divide(divisorForCalc, 0, RoundingMode.FLOOR);
        }

        // 10の「ループした回数乗」以上の位で等しくなるため、その1つ下の位が0でない数を取る最小位となる
        divisorForCalc = divisorForCalc.divide(BigDecimal.TEN, 0, RoundingMode.FLOOR);

        // 0でない数を取る最小位の数を求めるため、下限・上限の0でない数を取る最小位の数をそれぞれ求める
        lowerLimitForCalc = lowerLimit.divide(divisorForCalc, 0, RoundingMode.FLOOR);
        int lowerLimitOneDigit = lowerLimitForCalc.remainder(BigDecimal.TEN).intValue();

        upperLimitForCalc = upperLimit.divide(divisorForCalc, 0, RoundingMode.FLOOR);
        int upperLimitOneDigit = upperLimitForCalc.remainder(BigDecimal.TEN).intValue();

        // 0でない数を取る最小位の数
        BigDecimal notZeroMinDigit;
        if (lowerLimitOneDigit < 5 && 5 <= upperLimitOneDigit) {

            // 範囲内に5が含まれる場合
            notZeroMinDigit = new BigDecimal(5);

        } else if (lowerLimitOneDigit % 2 != 0 || upperLimitOneDigit - lowerLimitOneDigit == 1) {

            // 範囲内に5が含まれない かつ (下限が奇数 または 下限と上限の差が1である) 場合
            notZeroMinDigit = new BigDecimal(lowerLimitOneDigit + 1);

        } else {

            // 範囲内に5が含まれない かつ (下限が偶数 かつ 下限と上限の差が1より大きい) 場合
            notZeroMinDigit = new BigDecimal(lowerLimitOneDigit + 2);

        }

        // 0でない数を取る最小位の数を上で求めた数に置き換え、その位を掛けて選択除数を求める
        return upperLimitForCalc.subtract(new BigDecimal(upperLimitOneDigit)).add(notZeroMinDigit).multiply(divisorForCalc);
    }

    /**
     * 配分対象の比例変数を、議席数から0.5を減じた数で割る
     *
     * @param seatsMap 議席数を値に持つMap
     * @param entry 配分対象のマッピング
     * @return 除算結果
     */
    private static BigDecimal divideProportionalValueMinus(Map<String, Integer> seatsMap, Map.Entry<String, Integer> entry) {
        if (seatsMap.get(entry.getKey()) <= 0) {
            return null;
        }
        BigDecimal seatsDivisor = new BigDecimal(seatsMap.get(entry.getKey())).subtract(ZERO_POINT_FIVE);
        return divide(entry.getValue(), seatsDivisor, 3, RoundingMode.HALF_UP, false);
    }

    /**
     * 配分対象の比例変数を、議席数に0.5を加えた数で割る
     *
     * @param seatsMap 議席数を値に持つMap
     * @param entry 配分対象のマッピング
     * @return 除算結果
     */
    private static BigDecimal divideProportionalValuePlus(Map<String, Integer> seatsMap, Map.Entry<String, Integer> entry) {
        BigDecimal seatsDivisor = new BigDecimal(seatsMap.get(entry.getKey())).add(ZERO_POINT_FIVE);
        return divide(entry.getValue(), seatsDivisor, 3, RoundingMode.HALF_UP, false);
    }

    /**
     *
     * @param verifyingMap
     * @param key
     * @param value
     */
    private static void verifySeatsNumber(Map<String, Integer> verifyingMap, String key, int value) {
        int verifyingSeats = verifyingMap.get(key);
        assert value == verifyingSeats;
    }

    /**
     * 比例配分の商と最低獲得議席のうち、大きいほうを値に持つMapを返す
     *
     * @param seatsNumberMap 比例配分の商を値に持つMap
     * @param targetMinimumSeatsMap 最低獲得議席を値に持つMap
     * @return より大きい方を値に持つMap
     */
    private static Map<String, Integer> getGreaterSeatsMap(Map<String, Integer> seatsNumberMap, Map<String, Integer> targetMinimumSeatsMap) {
        return seatsNumberMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getGreaterSeats(targetMinimumSeatsMap, entry)));
    }

    /**
     * 比例配分の商と最低獲得議席のうち、大きいほうを返す
     *
     * @param targetMinimumSeatsMap 最低獲得議席を値に持つMap
     * @param entry 比例配分の商のマッピング
     * @return より大きい値
     */
    private static int getGreaterSeats(Map<String, Integer> targetMinimumSeatsMap, Map.Entry<String, Integer> entry) {
        int minimumSeats = targetMinimumSeatsMap.get(entry.getKey());
        return Integer.max(entry.getValue(), minimumSeats);
    }

    /**
     *
     */
    private static class TargetValueSeatsDto {

        private BigDecimal targetValue;
        private BigDecimal seats;

        public TargetValueSeatsDto(BigDecimal targetValue, BigDecimal seats) {
            this.targetValue = targetValue;
            this.seats = seats;
        }

        public BigDecimal getTargetValue() {
            return targetValue;
        }

        public void setTargetValue(BigDecimal targetValue) {
            this.targetValue = targetValue;
        }

        public BigDecimal getSeats() {
            return seats;
        }

        public void setSeats(BigDecimal seats) {
            this.seats = seats;
        }
    }
}
