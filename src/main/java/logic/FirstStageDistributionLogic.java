package logic;

import bean.firststage.FirstStageResultDto;
import bean.firststage.FirstStageUnderDistributionDto;
import bean.firststage.FirstStageUpperDistributionDto;
import bean.firststage.PartySecondVoteDto;
import bean.firststage.StatePopulationDto;
import bean.firststage.StateSeatsDto;
import bean.simulator.PartySimulatorDto;
import bean.simulator.StateSimulatorDto;
import constant.Constants;
import dao.PartyInfoDao;
import dao.StateInfoDao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import util.Util;

/**
 *
 * @author dev6905768cd
 */
public class FirstStageDistributionLogic {

    /**
     *
     * @param year
     * @param stateIndependentConstituencySeats
     * @return @throws SQLException
     */
    public static FirstStageUpperDistributionDto executeUpperDistribution(int year, Map<String, Map<String, Integer>> stateIndependentConstituencySeats) throws SQLException {
        // 各州の人口データを取得
        Map<String, Integer> statePopulationMap = StateInfoDao.getStatePopulationMap(year);
        return calculateStateSeats(statePopulationMap, stateIndependentConstituencySeats);
    }

    /**
     *
     * @param stateList
     * @return
     * @throws java.sql.SQLException
     */
    public static FirstStageUpperDistributionDto executeUpperDistribution(List<StateSimulatorDto> stateList) throws SQLException {
        // 各州の人口データを取得
        Map<String, Integer> statePopulationMap = stateList.stream()
                .collect(Collectors.toMap(StateSimulatorDto::getState, StateSimulatorDto::getPopulation));
        return calculateStateSeats(statePopulationMap, new HashMap<>());
    }

    /**
     *
     * @param year
     * @param statePopulationInfoList
     * @return
     */
    public static List<FirstStageUnderDistributionDto> executeUnderDistribution(int year, List<StatePopulationDto> statePopulationInfoList) {
        return statePopulationInfoList.stream()
                .map(dto -> getPartySecondVotesList(dto, partySecondVotesMapCreator(year)))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param stateList
     * @param statePopulationInfoList
     * @return
     */
    public static List<FirstStageUnderDistributionDto> executeUnderDistribution(List<StateSimulatorDto> stateList, List<StatePopulationDto> statePopulationInfoList) {
        return statePopulationInfoList.stream()
                .map(dto -> getPartySecondVotesList(dto, partySecondVotesMapCreator(stateList)))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param year
     * @param list
     * @param stateIndependentConstituencySeats
     * @return
     */
    public static List<FirstStageResultDto> getFirstLevelResultList(int year, List<FirstStageUnderDistributionDto> list, Map<String, Map<String, Integer>> stateIndependentConstituencySeats) {
        Map<String, Map<String, Integer>> seatsByPartyMapByStateMap = getSeatsByPartyMapByStateMap(list);

        List<FirstStageResultDto> firstStageResultList = seatsByPartyMapByStateMap.entrySet().stream()
                .map(entry -> getFirstLevelResult(entry, stateConstituencySeatsMapCreator(year)))
                .collect(Collectors.toList());
        return aggregateFirstLevelResult(stateIndependentConstituencySeats, firstStageResultList);
    }

    /**
     *
     * @param stateList
     * @param list
     * @return
     */
    public static List<FirstStageResultDto> getFirstLevelResultList(List<StateSimulatorDto> stateList, List<FirstStageUnderDistributionDto> list) {
        Map<String, Map<String, Integer>> seatsByPartyMapByStateMap = getSeatsByPartyMapByStateMap(list);

        List<FirstStageResultDto> firstStageResultList = seatsByPartyMapByStateMap.entrySet().stream()
                .map(entry -> getFirstLevelResult(entry, stateConstituencySeatsMapCreator(stateList)))
                .collect(Collectors.toList());
        return aggregateFirstLevelResult(new HashMap<>(), firstStageResultList);
    }

    /**
     *
     * @param statePopulationMap
     * @return
     */
    private static FirstStageUpperDistributionDto calculateStateSeats(Map<String, Integer> statePopulationMap, Map<String, Map<String, Integer>> stateIndependentConstituencySeats) {
        int independentConstituencySeatsSum = stateIndependentConstituencySeats.values().stream()
                .flatMap(e -> e.values().stream())
                .reduce(0, Integer::sum);
        final BigDecimal divisor = ElectionMethodLogic.calculateSelectedDivisor(statePopulationMap, Constants.NUMBER_OF_ALL_BUNDESTAG_SEATS - independentConstituencySeatsSum);

        List<StatePopulationDto> statePopulationList = statePopulationMap.entrySet().stream()
                .map(entry -> createStatePopulationInfo(entry, divisor))
                .collect(Collectors.toList());

        return new FirstStageUpperDistributionDto(statePopulationList, divisor);
    }

    /**
     *
     * @param entry
     * @param divisor
     * @return
     */
    private static StatePopulationDto createStatePopulationInfo(Map.Entry<String, Integer> entry, BigDecimal divisor) {
        String state = entry.getKey();
        int population = entry.getValue();
        BigDecimal unrounded = new BigDecimal(population).divide(divisor, 3, RoundingMode.DOWN);
        return new StatePopulationDto(state, population, unrounded);
    }

    /**
     *
     * @param stateInfo
     * @param mapCreator
     * @return
     */
    private static FirstStageUnderDistributionDto getPartySecondVotesList(StatePopulationDto stateInfo, Function<String, Map<String, Integer>> mapCreator) {
        String state = stateInfo.getState();
        Map<String, Integer> partySecondVotesMap = mapCreator.apply(state);
        return createPartySecondVotesList(stateInfo, state, partySecondVotesMap);
    }

    /**
     *
     * @param stateInfo
     * @param state
     * @param partySecondVotesMap
     * @return
     */
    private static FirstStageUnderDistributionDto createPartySecondVotesList(StatePopulationDto stateInfo, String state, Map<String, Integer> partySecondVotesMap) {
        final BigDecimal divisor = ElectionMethodLogic.calculateSelectedDivisor(partySecondVotesMap, stateInfo.getRounded());

        List<PartySecondVoteDto> partySecondVotesList = partySecondVotesMap.entrySet().stream()
                .map(entry -> createPartySecondVotesInfo(entry, divisor))
                .collect(Collectors.toList());

        return new FirstStageUnderDistributionDto(state, partySecondVotesList, divisor);
    }

    /**
     *
     * @param entry
     * @param divisor
     * @return
     */
    private static PartySecondVoteDto createPartySecondVotesInfo(Map.Entry<String, Integer> entry, BigDecimal divisor) {
        String party = entry.getKey();
        int secondVotes = entry.getValue();
        BigDecimal unrounded = new BigDecimal(secondVotes).divide(divisor, 3, RoundingMode.DOWN);
        return new PartySecondVoteDto(party, secondVotes, unrounded);
    }

    /**
     *
     * @param list
     * @return
     */
    private static Map<String, Map<String, Integer>> getSeatsByPartyMapByStateMap(List<FirstStageUnderDistributionDto> list) {
        return list.stream()
                .map(dto -> Util.replaceMapKeys(dto, FirstStageUnderDistributionDto::getPartySecondVoteInfoList, PartySecondVoteDto::getParty, Util.mapCreator(FirstStageUnderDistributionDto::getState, PartySecondVoteDto::getRounded)))
                .flatMap(e -> e.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        Util::copyMap));
    }

    /**
     *
     * @param entry
     * @param mapCreator
     * @return
     */
    private static FirstStageResultDto getFirstLevelResult(Map.Entry<String, Map<String, Integer>> entry, Function<String, Map<String, Integer>> mapCreator) {
        String party = entry.getKey();
        Map<String, Integer> stateConstituencySeatsMap = mapCreator.apply(party);
        return createFirstLevelResult(entry, party, stateConstituencySeatsMap);
    }

    /**
     *
     * @param entry
     * @param party
     * @param stateConstituencySeatsMap
     * @return
     */
    private static FirstStageResultDto createFirstLevelResult(Map.Entry<String, Map<String, Integer>> entry, String party, Map<String, Integer> stateConstituencySeatsMap) {
        Map<String, Integer> stateSeatQuotasMap = entry.getValue();
        List<StateSeatsDto> stateSeatsDtoList = stateSeatQuotasMap.keySet().stream()
                .map(state -> createStateSeatsDto(state, stateSeatQuotasMap.get(state), stateConstituencySeatsMap.get(state)))
                .collect(Collectors.toList());

        return new FirstStageResultDto(party, stateSeatsDtoList);
    }

    /**
     *
     * @param state
     * @param seatQuotas
     * @param constituencySeats
     * @return
     */
    private static StateSeatsDto createStateSeatsDto(String state, int seatQuotas, int constituencySeats) {
        boolean constituencySeatsIsLarger = constituencySeats > seatQuotas;
        int largerSeats = constituencySeatsIsLarger ? constituencySeats : seatQuotas;
        int overhang = constituencySeatsIsLarger ? constituencySeats - seatQuotas : 0;
        return new StateSeatsDto(state, seatQuotas, constituencySeats, largerSeats, overhang);
    }

    /**
     *
     * @param stateIndependentConstituencySeats
     * @param firstStageResultList
     * @return
     */
    private static List<FirstStageResultDto> aggregateFirstLevelResult(Map<String, Map<String, Integer>> stateIndependentConstituencySeats, List<FirstStageResultDto> firstStageResultList) {
        int independentConstituencySeatsSum = stateIndependentConstituencySeats.values().stream()
                .flatMap(e -> e.values().stream())
                .reduce(0, Integer::sum);
        if (independentConstituencySeatsSum > 0) {
            List<String> stateList1 = firstStageResultList.stream()
                    .flatMap(dto -> dto.getStateSeatsList().stream())
                    .map(StateSeatsDto::getState)
                    .distinct()
                    .collect(Collectors.toList());

            List<FirstStageResultDto> partyWithoutDistributionList = stateIndependentConstituencySeats.entrySet().stream()
                    .map(entry -> new FirstStageResultDto(entry.getKey(), stateList1.stream()
                            .map(state -> new StateSeatsDto(state, entry))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            firstStageResultList.addAll(partyWithoutDistributionList);
        }

        firstStageResultList.add(0, new FirstStageResultDto(Constants.SUMMARY_TITLE, sumFirstStageResult(firstStageResultList)));

        return firstStageResultList;
    }

    /**
     *
     * @param firstStageResultList
     * @return
     */
    private static List<StateSeatsDto> sumFirstStageResult(List<FirstStageResultDto> firstStageResultList) {
        return firstStageResultList.stream()
                .flatMap(dto -> dto.getStateSeatsList().stream())
                .collect(Util.toGroupedEntry(StateSeatsDto::getState))
                .map(StateSeatsDto::new)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param year
     * @return
     */
    private static Function<String, Map<String, Integer>> partySecondVotesMapCreator(int year) {
        return state -> {
            try {
                return PartyInfoDao.getPartySecondVotesMapByState(year, state);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    /**
     *
     * @param stateList
     * @return
     */
    private static Function<String, Map<String, Integer>> partySecondVotesMapCreator(List<StateSimulatorDto> stateList) {
        return state -> {
            StateSimulatorDto stateSimulatorInfo = stateList.stream()
                    .filter(dto -> dto.getState().equals(state))
                    .findFirst().get();
            return stateSimulatorInfo.getPartyInfoList().stream()
                    .collect(Collectors.toMap(PartySimulatorDto::getParty, PartySimulatorDto::getSecondVotes));
        };
    }

    /**
     * 最低保障議席を返す
     *
     * @param year
     * @return 最低保障議席
     */
    private static Function<String, Map<String, Integer>> stateConstituencySeatsMapCreator(Integer year) {
        return party -> {
            try {
                return PartyInfoDao.getStateConstituencySeatsByParty(year, party);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    /**
     * 最低保障議席を返す
     *
     * @param stateList
     * @return 最低保障議席
     */
    private static Function<String, Map<String, Integer>> stateConstituencySeatsMapCreator(List<StateSimulatorDto> stateList) {
        return party -> stateList.stream()
                .map(dto -> Util.replaceMapKeys(dto, StateSimulatorDto::getPartyInfoList, PartySimulatorDto::getParty, Util.mapCreator(StateSimulatorDto::getState, PartySimulatorDto::getConstituencySeats)))
                .flatMap(map -> map.entrySet().stream())
                .filter(e -> e.getKey().equals(party))
                .map(Map.Entry::getValue)
                .flatMap(e -> e.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
