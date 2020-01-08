package bean.secondstage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author dev6905768cd
 */
public class PartySeatsDto {
    
    private final String party;
    private final int guaranteedMinimumSeats;
    private final int secondVotes;
    private final BigDecimal unrounded;

    /**
     * 
     * @param party
     * @param guaranteedMinimumSeats
     * @param secondVotes
     * @param unrounded 
     */
    public PartySeatsDto(String party, Integer guaranteedMinimumSeats, Integer secondVotes, BigDecimal unrounded) {
        this.party = party;
        this.guaranteedMinimumSeats = guaranteedMinimumSeats;
        this.secondVotes = secondVotes;
        this.unrounded = unrounded;
    }

    /**
     * @return the party
     */
    public String getParty() {
        return party;
    }

    /**
     * @return the guaranteedMinimumSeats
     */
    public Integer getGuaranteedMinimumSeats() {
        return guaranteedMinimumSeats;
    }

    /**
     * @return the secondVotes
     */
    public Integer getSecondVotes() {
        return secondVotes;
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
     * @return the increasedSeats
     */
    public Integer getIncreasedSeats() {
        return getRounded() - getGuaranteedMinimumSeats();
    }
}
