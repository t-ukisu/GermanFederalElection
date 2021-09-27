package logic;

import bean.firststage.FirstStageResultDto;
import bean.secondstage.PartySeatsDto;
import bean.secondstage.SecondStageResultDto;
import bean.secondstage.SecondStageUnderDistributionDto;
import bean.secondstage.SecondStageUpperDistributionDto;
import bean.secondstage.StateCompleteDistributionDto;
import bean.secondstage.StateDistributionInfoDto;
import bean.firststage.StateSeatsDto;
import bean.simulator.PartySimulatorDto;
import bean.simulator.StateSimulatorDto;
import constant.Constants;
import dao.PartyInfoDao;
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
public class SecondStageDistributionLogic {

    /**
     *
     * @param year
     * @param firstLevelResult
     * @param stateIndependentConstituencySeats
     * @return
     * @throws SQLException
     */
    public static SecondStageUpperDistributionDto executeUpperDistribution(int year, List<FirstStageResultDto> firstLevelResult, Map<String, Map<String, Integer>> stateIndependentConstituencySeats) throws SQLException {
        Map<String, Integer> partySeatsMap = hoge(firstLevelResult, stateIndependentConstituencySeats);
        Map<String, Integer> partySecondVotesMap = PartyInfoDao.getPartySecondVotesMap(year);
        return createUpperDistributionInfo(partySecondVotesMap, partySeatsMap);
    }

    /**
     *
     * @param stateList
     * @param firstLevelResult
     * @return
     */
    public static SecondStageUpperDistributionDto executeUpperDistribution(List<StateSimulatorDto> stateList, List<FirstStageResultDto> firstLevelResult) {
        Map<String, Integer> partySeatsMap = hoge(firstLevelResult, new HashMap<>());
        Map<String, Integer> partySecondVotesMap = stateList.stream()
                .map(dto -> Util.replaceMapKeys(dto, StateSimulatorDto::getPartyInfoList, PartySimulatorDto::getParty, (base, inner) -> inner.getSecondVotes()))
                .flatMap(e -> e.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
        return createUpperDistributionInfo(partySecondVotesMap, partySeatsMap);
    }

    /**
     *
     * @param year
     * @param partySeatsInfoList
     * @return
     */
    public static List<SecondStageUnderDistributionDto> executeUnderDistribution(int year, List<PartySeatsDto> partySeatsInfoList) {
        Map<String, Integer> partySeatsMap = partySeatsInfoList.stream()
                .collect(Collectors.toMap(
                        PartySeatsDto::getParty,
                        PartySeatsDto::getRounded));

        return partySeatsMap.entrySet().stream()
                .map(entry -> createDistributionDto(entry, stateInfoListCreator(year)))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param stateList
     * @param partySeatsInfoList
     * @return
     */
    public static List<SecondStageUnderDistributionDto> executeUnderDistribution(List<StateSimulatorDto> stateList, List<PartySeatsDto> partySeatsInfoList) {
        Map<String, Integer> partySeatsMap = partySeatsInfoList.stream()
                .collect(Collectors.toMap(
                        PartySeatsDto::getParty,
                        PartySeatsDto::getRounded));

        return partySeatsMap.entrySet().stream()
                .map(entry -> createDistributionDto(entry, stateInfoListCreator(stateList)))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param firstLevelResult
     * @param partyDistributionList
     * @param stateIndependentConstituencySeats
     * @return
     */
    public static List<SecondStageResultDto> tallyElectionResult(List<FirstStageResultDto> firstLevelResult, List<SecondStageUnderDistributionDto> partyDistributionList, Map<String, Map<String, Integer>> stateIndependentConstituencySeats) {
        List<SecondStageResultDto> secondStageResultList = firstLevelResult.stream()
                .filter(dto -> !dto.getParty().equals(Constants.SUMMARY_TITLE) && !stateIndependentConstituencySeats.containsKey(dto.getParty()))
                .map(result -> createSecondStageResultInfo(result, partyDistributionList))
                .collect(Collectors.toList());

        int independentConstituencySeatsSum = stateIndependentConstituencySeats.values().stream()
                .flatMap(e -> e.values().stream())
                .reduce(0, Integer::sum);

        if (independentConstituencySeatsSum > 0) {
            List<String> stateList = secondStageResultList.stream()
                    .flatMap(dto -> dto.getStateInfoList().stream())
                    .map(StateCompleteDistributionDto::getState)
                    .distinct()
                    .collect(Collectors.toList());

            List<SecondStageResultDto> partyWithoutDistributionList = stateIndependentConstituencySeats.entrySet().stream()
                    .map(entry -> new SecondStageResultDto(entry.getKey(), stateList.stream()
                    .map(state -> new StateCompleteDistributionDto(state, entry))
                    .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            secondStageResultList.addAll(partyWithoutDistributionList);
        }

        secondStageResultList.add(0, new SecondStageResultDto(Constants.SUMMARY_TITLE, sumSecondStageResult(secondStageResultList)));

        return secondStageResultList;
    }

    /**
     *
     * @param firstLevelResult
     * @param stateIndependentConstituencySeats
     * @return
     */
    private static Map<String, Integer> hoge(List<FirstStageResultDto> firstLevelResult, Map<String, Map<String, Integer>> stateIndependentConstituencySeats) {
        return firstLevelResult.stream()
                .filter(dto -> !dto.getParty().equals(Constants.SUMMARY_TITLE) && !stateIndependentConstituencySeats.containsKey(dto.getParty()))
                .collect(Collectors.toMap(FirstStageResultDto::getParty,
                        FirstStageResultDto::getLargerSeatsSum));
    }

    /**
     *
     * @param partySecondVotesMap
     * @param partySeatsMap
     * @return
     */
    private static SecondStageUpperDistributionDto createUpperDistributionInfo(Map<String, Integer> partySecondVotesMap, Map<String, Integer> partySeatsMap) {
        BigDecimal divisor = ElectionMethodLogic.distributeGuaranteedMinimumSeats(partySecondVotesMap, partySeatsMap);

        List<PartySeatsDto> partySeatsInfoList = partySeatsMap.keySet().stream()
                .map(party -> createPartySeatsDto(party, partySeatsMap.get(party), partySecondVotesMap.get(party), divisor))
                .collect(Collectors.toList());

        return new SecondStageUpperDistributionDto(partySeatsInfoList, divisor);
    }

    /**
     *
     * @param party
     * @param guaranteedMinimumSeats
     * @param secondVotes
     * @param divisor
     * @return
     */
    private static PartySeatsDto createPartySeatsDto(String party, int guaranteedMinimumSeats, int secondVotes, BigDecimal divisor) {
        BigDecimal unrounded = new BigDecimal(secondVotes).divide(divisor, 3, RoundingMode.DOWN);
        return new PartySeatsDto(party, guaranteedMinimumSeats, secondVotes, unrounded);
    }

    /**
     *
     * @param partySeatsEntry
     * @param listCreator
     * @return
     */
    private static SecondStageUnderDistributionDto createDistributionDto(Map.Entry<String, Integer> partySeatsEntry, Function<String, List<StateDistributionInfoDto>> listCreator) {
        String party = partySeatsEntry.getKey();
        List<StateDistributionInfoDto> stateInfoList = listCreator.apply(party);

        int seats = partySeatsEntry.getValue();
        if (stateInfoList.size() < 2) {
            stateInfoList.get(0).setLargerSeats(seats);
            return new SecondStageUnderDistributionDto(party, stateInfoList, BigDecimal.ZERO);
        }

        Map<String, Integer> stateSecondVotesMap = stateInfoList.stream()
                .collect(Collectors.toMap(StateDistributionInfoDto::getState,
                        StateDistributionInfoDto::getSecondVotes));

        Map<String, Integer> stateConstituencySeatsMap = stateInfoList.stream()
                .collect(Collectors.toMap(StateDistributionInfoDto::getState,
                        StateDistributionInfoDto::getConstituencySeats));

        final BigDecimal divisor = ElectionMethodLogic.distributeAllSeatsByTargetMinimumSeats(stateSecondVotesMap, seats, stateConstituencySeatsMap);

        stateInfoList = stateInfoList.stream()
                .map(info -> info.fillOtherInfo(divisor))
                .collect(Collectors.toList());

        return new SecondStageUnderDistributionDto(party, stateInfoList, divisor);
    }

    /**
     *
     * @param result
     * @param partyDistributionList
     * @return
     */
    private static SecondStageResultDto createSecondStageResultInfo(FirstStageResultDto result, List<SecondStageUnderDistributionDto> partyDistributionList) {
        String party = result.getParty();
        SecondStageUnderDistributionDto secondStageResultDto = partyDistributionList.stream()
                .filter(e -> e.getParty().equals(party))
                .findFirst()
                .get();

        List<StateSeatsDto> stateSeatsList = result.getStateSeatsList();
        List<StateDistributionInfoDto> stateInfoList = secondStageResultDto.getStateInfoList();
        List<StateCompleteDistributionDto> collect = stateSeatsList.stream()
                .map(dto -> createStateDistributionInfo(dto, stateInfoList))
                .collect(Collectors.toList());

        return new SecondStageResultDto(party, collect);
    }

    /**
     *
     * @param dto
     * @param stateInfoList
     * @return
     */
    private static StateCompleteDistributionDto createStateDistributionInfo(StateSeatsDto dto, List<StateDistributionInfoDto> stateInfoList) {
        String state = dto.getState();
        StateDistributionInfoDto stateResultDto = stateInfoList.stream()
                .filter(e -> e.getState().equals(state))
                .findFirst()
                .get();

        int constituencySeats = dto.getConstituencySeats();
        int firstStageResultSeats = dto.getLargerSeats();
        int overhang = dto.getOverhang();
        int secondStageResultSeats = stateResultDto.getLargerSeats();
        int landListSeats = secondStageResultSeats - constituencySeats;
        int increasedSeats = secondStageResultSeats - firstStageResultSeats;
        return new StateCompleteDistributionDto(state, constituencySeats, firstStageResultSeats, overhang, secondStageResultSeats, landListSeats, increasedSeats);
    }

    /**
     *
     * @param secondStageResultList
     * @return
     */
    private static List<StateCompleteDistributionDto> sumSecondStageResult(List<SecondStageResultDto> secondStageResultList) {
        return secondStageResultList.stream()
                .flatMap(dto -> dto.getStateInfoList().stream())
                .collect(Util.toGroupedEntry(StateCompleteDistributionDto::getState))
                .map(StateCompleteDistributionDto::new)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param year
     * @return
     */
    private static Function<String, List<StateDistributionInfoDto>> stateInfoListCreator(int year) {
        return party -> {
            try {
                return PartyInfoDao.getStateInfoListByParty(year, party);
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
    private static Function<String, List<StateDistributionInfoDto>> stateInfoListCreator(List<StateSimulatorDto> stateList) {
        return party -> stateList.stream()
                .map(dto -> Util.replaceMapKeys(dto, StateSimulatorDto::getPartyInfoList, PartySimulatorDto::getParty, (base, inner) -> new StateDistributionInfoDto(base.getState(), inner.getSecondVotes(), inner.getConstituencySeats())))
                .flatMap(e -> e.entrySet().stream())
                .filter(e -> e.getKey().equals(party))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
