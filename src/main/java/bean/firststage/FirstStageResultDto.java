package bean.firststage;

import java.util.List;

/**
 * @author dev6905768cd
 */
public class FirstStageResultDto {

    private String party;
    private List<StateSeatsDto> stateSeatsList;

    public FirstStageResultDto(String party, List<StateSeatsDto> stateSeatsList) {
        this.party = party;
        this.stateSeatsList = stateSeatsList;
    }

    /**
     * @return the party
     */
    public String getParty() {
        return party;
    }

    /**
     * @param party the party to set
     */
    public void setParty(String party) {
        this.party = party;
    }

    /**
     * @return the stateSeatsList
     */
    public List<StateSeatsDto> getStateSeatsList() {
        return stateSeatsList;
    }

    /**
     * @param stateSeatsList the stateSeatsList to set
     */
    public void setStateSeatsList(List<StateSeatsDto> stateSeatsList) {
        this.stateSeatsList = stateSeatsList;
    }

    /**
     * @return the largerSeatsSum
     */
    public Integer getLargerSeatsSum() {
        return stateSeatsList.stream()
                .mapToInt(StateSeatsDto::getLargerSeats)
                .sum();
    }
}
