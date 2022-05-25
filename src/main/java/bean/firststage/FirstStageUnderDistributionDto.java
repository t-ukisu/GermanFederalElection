package bean.firststage;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dev6905768cd
 */
public class FirstStageUnderDistributionDto {

    private final String state;
    private final List<PartySecondVoteDto> partySecondVoteInfoList;
    private final BigDecimal divisor;

    public FirstStageUnderDistributionDto(String state, List<PartySecondVoteDto> partySecondVoteInfoList, BigDecimal divisor) {
        this.state = state;
        this.partySecondVoteInfoList = partySecondVoteInfoList;
        this.divisor = divisor;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the partySecondVoteInfoList
     */
    public List<PartySecondVoteDto> getPartySecondVoteInfoList() {
        return partySecondVoteInfoList;
    }

    /**
     * @return the divisor
     */
    public BigDecimal getDivisor() {
        return divisor;
    }
}
