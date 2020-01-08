package bean.simulator;

/**
 *
 * @author dev6905768cd
 */
public class PartyNameDto {

    private String name;
    
    public PartyNameDto() {
        this.name = "政党名を入力してください。";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
