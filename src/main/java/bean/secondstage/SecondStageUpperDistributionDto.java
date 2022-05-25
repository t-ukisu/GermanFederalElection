package bean.secondstage;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dev6905768cd
 */
public class SecondStageUpperDistributionDto {

    private final List<PartySeatsDto> partySeatsInfoList;
    private final BigDecimal divisor;

    public SecondStageUpperDistributionDto(List<PartySeatsDto> partySeatsInfoList, BigDecimal divisor) {
        this.partySeatsInfoList = partySeatsInfoList;
        this.divisor = divisor;
    }

    /**
     * @return the partySeatsInfoList
     */
    public List<PartySeatsDto> getPartySeatsInfoList() {
        return partySeatsInfoList;
    }

    /**
     * @return the divisor
     */
    public BigDecimal getDivisor() {
        return divisor;
    }
}
