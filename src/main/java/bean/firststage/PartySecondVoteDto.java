package bean.firststage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author dev6905768cd
 */
public class PartySecondVoteDto {

    /**
     * 政党名
     */
    private final String party;

    /**
     * 人口
     */
    private final int secondVotes;

    /**
     * 丸められていない割合
     */
    private final BigDecimal unrounded;

    /**
     *
     * @param party
     * @param secondVotes
     * @param unrounded
     */
    public PartySecondVoteDto(String party, int secondVotes, BigDecimal unrounded) {
        this.party = party;
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
     * @return the secondVotes
     */
    public int getSecondVotes() {
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
    public int getRounded() {
        return this.unrounded.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
