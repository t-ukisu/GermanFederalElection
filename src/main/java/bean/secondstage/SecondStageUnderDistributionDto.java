package bean.secondstage;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author dev6905768cd
 */
public class SecondStageUnderDistributionDto {

    private final String party;
    private final List<StateDistributionInfoDto> stateInfoList;
    private final BigDecimal divisor;

    public SecondStageUnderDistributionDto(String party, List<StateDistributionInfoDto> stateInfoList, BigDecimal divisor) {
        this.party = party;
        this.stateInfoList = stateInfoList;
        this.divisor = divisor;
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
    public List<StateDistributionInfoDto> getStateInfoList() {
        return stateInfoList;
    }

    /**
     * @return the secondVotesSum
     */
    public int getSecondVotesSum() {
        return stateInfoList.stream()
                .mapToInt(StateDistributionInfoDto::getSecondVotes)
                .sum();
    }

    /**
     * @return the divisor
     */
    public BigDecimal getDivisor() {
        return divisor;
    }
}
