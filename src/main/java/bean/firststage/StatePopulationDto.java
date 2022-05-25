package bean.firststage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author dev6905768cd
 */
public class StatePopulationDto {

    /**
     * 州名
     */
    private final String state;

    /**
     * 人口
     */
    private final int population;

    /**
     * 丸められていない割合
     */
    private final BigDecimal unrounded;

    /**
     * @param state
     * @param population
     * @param unrounded
     */
    public StatePopulationDto(String state, int population, BigDecimal unrounded) {
        this.state = state;
        this.population = population;
        this.unrounded = unrounded;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the population
     */
    public int getPopulation() {
        return population;
    }

    /**
     * @return the unrounded
     */
    public BigDecimal getUnrounded() {
        return unrounded;
    }

    /**
     * @return the rounded
     */
    public int getRounded() {
        return this.unrounded.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
