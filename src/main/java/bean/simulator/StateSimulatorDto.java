package bean.simulator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dev6905768cd
 */
public class StateSimulatorDto {
    
    private String state;
    private int population;
    private int constituencySeats;
    private final List<PartySimulatorDto> partyInfoList = new ArrayList<>();
    
    public StateSimulatorDto() {
        this("地域名を入力してください。");
    }

    public StateSimulatorDto(String state) {
        this.state = state;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the population
     */
    public int getPopulation() {
        return population;
    }

    /**
     * @param population the population to set
     */
    public void setPopulation(int population) {
        this.population = population;
    }

    /**
     * @return the constituencySeats
     */
    public int getConstituencySeats() {
        return constituencySeats;
    }

    /**
     * @param constituencySeats the constituencySeats to set
     */
    public void setConstituencySeats(int constituencySeats) {
        this.constituencySeats = constituencySeats;
    }

    /**
     * @return the partyInfoList
     */
    public List<PartySimulatorDto> getPartyInfoList() {
        return partyInfoList;
    }
}
