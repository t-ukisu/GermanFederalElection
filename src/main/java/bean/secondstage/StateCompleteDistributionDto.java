package bean.secondstage;

import java.util.List;
import java.util.Map;

/**
 * @author dev6905768cd
 */
public class StateCompleteDistributionDto {

    private final String state;
    private final int constituencySeats;
    private final int firstStageResultSeats;
    private final int overhang;
    private final int secondStageResultSeats;
    private final int landListSeats;
    private final int increasedSeats;

    /**
     * @param state
     * @param constituencySeats
     * @param firstStageResultSeats
     * @param overhang
     * @param secondStageResultSeats
     * @param landListSeats
     * @param increasedSeats
     */
    public StateCompleteDistributionDto(String state, int constituencySeats, int firstStageResultSeats, int overhang, int secondStageResultSeats, int landListSeats, int increasedSeats) {
        this.state = state;
        this.constituencySeats = constituencySeats;
        this.firstStageResultSeats = firstStageResultSeats;
        this.overhang = overhang;
        this.secondStageResultSeats = secondStageResultSeats;
        this.landListSeats = landListSeats;
        this.increasedSeats = increasedSeats;
    }

    /**
     * @param state
     * @param entry
     */
    public StateCompleteDistributionDto(String state, Map.Entry<String, Map<String, Integer>> entry) {
        this.state = state;
        int seats = entry.getValue().getOrDefault(state, 0);
        this.constituencySeats = seats;
        this.firstStageResultSeats = seats;
        this.overhang = seats;
        this.secondStageResultSeats = seats;
        this.landListSeats = 0;
        this.increasedSeats = 0;
    }

    /**
     * @param entry
     */
    public StateCompleteDistributionDto(Map.Entry<String, List<StateCompleteDistributionDto>> entry) {
        this.state = entry.getKey();
        List<StateCompleteDistributionDto> list = entry.getValue();
        this.constituencySeats = list.stream()
                .mapToInt(StateCompleteDistributionDto::getConstituencySeats)
                .sum();
        this.firstStageResultSeats = list.stream()
                .mapToInt(StateCompleteDistributionDto::getFirstStageResultSeats)
                .sum();
        this.overhang = list.stream()
                .mapToInt(StateCompleteDistributionDto::getOverhang)
                .sum();
        this.secondStageResultSeats = list.stream()
                .mapToInt(StateCompleteDistributionDto::getSecondStageResultSeats)
                .sum();
        this.landListSeats = list.stream()
                .mapToInt(StateCompleteDistributionDto::getLandListSeats)
                .sum();
        this.increasedSeats = list.stream()
                .mapToInt(StateCompleteDistributionDto::getIncreasedSeats)
                .sum();
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the constituencySeats
     */
    public int getConstituencySeats() {
        return constituencySeats;
    }

    /**
     * @return the firstStageResultSeats
     */
    public int getFirstStageResultSeats() {
        return firstStageResultSeats;
    }

    /**
     * @return the overhang
     */
    public int getOverhang() {
        return overhang;
    }

    /**
     * @return the secondStageResultSeats
     */
    public int getSecondStageResultSeats() {
        return secondStageResultSeats;
    }

    /**
     * @return the landListSeats
     */
    public int getLandListSeats() {
        return landListSeats;
    }

    /**
     * @return the increasedSeats
     */
    public int getIncreasedSeats() {
        return increasedSeats;
    }
}
