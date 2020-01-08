package bean.firststage;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author dev6905768cd
 */
public class FirstStageUpperDistributionDto {

    private final List<StatePopulationDto> statePopulationInfoList;
    private final BigDecimal divisor;

    public FirstStageUpperDistributionDto(List<StatePopulationDto> statePopulationInfoList, BigDecimal divisor) {
        this.statePopulationInfoList = statePopulationInfoList;
        this.divisor = divisor;
    }

    /**
     * @return the statePopulationInfoList
     */
    public List<StatePopulationDto> getStatePopulationInfoList() {
        return statePopulationInfoList;
    }

    /**
     * @return the divisor
     */
    public BigDecimal getDivisor() {
        return divisor;
    }
}
