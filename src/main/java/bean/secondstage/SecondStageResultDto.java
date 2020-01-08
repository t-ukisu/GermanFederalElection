package bean.secondstage;

import java.util.List;

/**
 *
 * @author dev6905768cd
 */
public class SecondStageResultDto {

    private final String party;
    private final List<StateCompleteDistributionDto> stateInfoList;

    public SecondStageResultDto(String party, List<StateCompleteDistributionDto> stateInfoList) {
        this.party = party;
        this.stateInfoList = stateInfoList;
    }

    /**
     * @return the party
     */
    public String getParty() {
        return party;
    }

    /**
     * @return the stateInfoList
     */
    public List<StateCompleteDistributionDto> getStateInfoList() {
        return stateInfoList;
    }
}
