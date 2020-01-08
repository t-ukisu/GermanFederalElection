package bean.simulator;

import java.io.Serializable;

/**
 *
 * @author dev6905768cd
 */
public class PartySimulatorDto implements Serializable {

    private String party;
    private int secondVotes;
    private int constituencySeats;

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
     * @return the secondVotes
     */
    public int getSecondVotes() {
        return secondVotes;
    }

    /**
     * @param secondVotes the secondVotes to set
     */
    public void setSecondVotes(int secondVotes) {
        this.secondVotes = secondVotes;
    }

    /**
     * @return the constituencySeats
     */
    public int getConstituencySeats() {
        return constituencySeats;
    }

    /**
     * @param constituencySeats the constituencySeats to set
     */
    public void setConstituencySeats(int constituencySeats) {
        this.constituencySeats = constituencySeats;
    }
}
