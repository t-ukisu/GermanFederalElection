package bean.secondstage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author dev6905768cd
 */
public class StateDistributionInfoDto {

    private final String state;
    private final int secondVotes;
    private final int constituencySeats;
    private BigDecimal unrounded;
    private int largerSeats;

    public StateDistributionInfoDto(String state, int secondVotes, int constituencySeats) {
        this.state = state;
        this.secondVotes = secondVotes;
        this.constituencySeats = constituencySeats;
    }

    /**
     * @param divisor
     * @return
     */
    public StateDistributionInfoDto fillOtherInfo(BigDecimal divisor) {
        this.unrounded = new BigDecimal(this.secondVotes).divide(divisor, 3, RoundingMode.DOWN);
        int rounded = this.unrounded.setScale(0, RoundingMode.HALF_UP).intValue();
        this.largerSeats = Integer.max(rounded, this.constituencySeats);
        return this;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the secondVotes
     */
    public int getSecondVotes() {
        return secondVotes;
    }

    /**
     * @return the constituencySeats
     */
    public Integer getConstituencySeats() {
        return constituencySeats;
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
    public Integer getRounded() {
        return this.unrounded.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * @return the largerSeats
     */
    public Integer getLargerSeats() {
        return largerSeats;
    }

    /**
     * @param largerSeats the largerSeats to set
     */
    public void setLargerSeats(Integer largerSeats) {
        this.largerSeats = largerSeats;
    }
}
