package bean.firststage;

import java.util.List;
import java.util.Map;

/**
 * @author dev6905768cd
 */
public class StateSeatsDto {

    private final String state;
    private final int seatQuotas;
    private final int constituencySeats;
    private final int largerSeats;
    private final int overhang;

    /**
     * @param state
     * @param seatQuotas
     * @param constituencySeats
     * @param largerSeats
     * @param overhang
     */
    public StateSeatsDto(String state, int seatQuotas, int constituencySeats, int largerSeats, int overhang) {
        this.state = state;
        this.seatQuotas = seatQuotas;
        this.constituencySeats = constituencySeats;
        this.largerSeats = largerSeats;
        this.overhang = overhang;
    }

    /**
     * @param state
     * @param entry
     */
    public StateSeatsDto(String state, Map.Entry<String, Map<String, Integer>> entry) {
        this.state = state;
        this.seatQuotas = 0;
        int seats = entry.getValue().getOrDefault(state, 0);
        this.constituencySeats = seats;
        this.largerSeats = seats;
        this.overhang = seats;
    }

    /**
     * @param stateInfoEntry
     */
    public StateSeatsDto(Map.Entry<String, List<StateSeatsDto>> stateInfoEntry) {
        this.state = stateInfoEntry.getKey();

        List<StateSeatsDto> stateInfoList = stateInfoEntry.getValue();

        this.seatQuotas = stateInfoList.stream()
                .mapToInt(StateSeatsDto::getSeatQuotas)
                .sum();

        this.constituencySeats = stateInfoList.stream()
                .mapToInt(StateSeatsDto::getConstituencySeats)
                .sum();

        this.largerSeats = stateInfoList.stream()
                .mapToInt(StateSeatsDto::getLargerSeats)
                .sum();

        this.overhang = stateInfoList.stream()
                .mapToInt(StateSeatsDto::getOverhang)
                .sum();
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the seatQuotas
     */
    public int getSeatQuotas() {
        return seatQuotas;
    }

    /**
     * @return the constituencySeats
     */
    public int getConstituencySeats() {
        return constituencySeats;
    }

    /**
     * @return the largerSeats
     */
    public int getLargerSeats() {
        return largerSeats;
    }

    /**
     * @return the overhang
     */
    public int getOverhang() {
        return overhang;
    }
}
