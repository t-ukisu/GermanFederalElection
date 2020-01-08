package controller;

import bean.firststage.FirstStageResultDto;
import bean.firststage.FirstStageUnderDistributionDto;
import bean.firststage.FirstStageUpperDistributionDto;
import bean.secondstage.SecondStageResultDto;
import bean.secondstage.SecondStageUnderDistributionDto;
import bean.secondstage.SecondStageUpperDistributionDto;
import bean.simulator.PartyNameDto;
import bean.simulator.PartySimulatorDto;
import bean.simulator.StateSimulatorDto;
import constant.Constants;
import dao.PartyInfoDao;
import dao.YearMasterDao;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.el.ImportHandler;
import javax.faces.context.FacesContext;
import logic.ElectionMethodLogic;
import logic.FirstStageDistributionLogic;
import logic.SecondStageDistributionLogic;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

/**
 * ドイツ連邦議会議員選挙再現Webアプリのバッキングビーン
 *
 * @author T.Ukisu
 */
@Named(value = "federalElectionController")
@SessionScoped
public class FederalElectionController implements Serializable {

    /**
     * 表示モード:選挙結果
     */
    private boolean displayResult = false;
    /**
     * 表示モード:シミュレーションデータ入力
     */
    private boolean displaySimulator = false;
    /**
     * メニュー定義
     */
    private MenuModel menuModel;

    /**
     * 第一段階上位配分の計算結果DTO
     */
    private FirstStageUpperDistributionDto statePopulationInfo;
    /**
     * 第一段階下位配分の計算結果DTOのList
     */
    private List<FirstStageUnderDistributionDto> partySecondsVotesListByState;
    /**
     * 第一段階計算結果DTOのList
     */
    private List<FirstStageResultDto> firstStageResult;
    /**
     * 第二段階上位配分の計算結果DTO
     */
    private SecondStageUpperDistributionDto partySeatsInfo;
    /**
     * 第二段階下位配分の計算結果DTOのList
     */
    private List<SecondStageUnderDistributionDto> partyDistributionList;
    /**
     * 第二段階計算結果DTOのList
     */
    private List<SecondStageResultDto> secondStageResult;
    /**
     * 議席配分シミュレーターDTOのList
     */
    private List<StateSimulatorDto> stateList;

    /**
     * 
     */
    private StateSimulatorDto targetStateInfo;
    /**
     *
     */
    private List<PartyNameDto> partyList;

    /**
     * 議席配分シミュレーター画面上で選択した政党DTO
     */
    private PartySimulatorDto selectedPartyInfo;
    private PartySimulatorDto additionalPartyInfo;
    private int maxSecondVotes;
    private int maxConstituencySeats;

    @PostConstruct
    public void init() {
        List<String> years;
        try {
            years = YearMasterDao.getYears();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        List<MenuElement> menuItemList = years.stream().map(year -> {
            DefaultMenuItem item = new DefaultMenuItem(year);
            item.setCommand("#{federalElectionController.executeDistribution(" + year + ")}");
            item.setUpdate(":seatsDistribution");
//            item.setOncomplete("changeSelectedMenu($(this));");
            return item;
        }).collect(Collectors.toList());

        DefaultMenuItem item = new DefaultMenuItem("シミュレーター");
        item.setCommand("#{federalElectionController.displayStateName}");
        item.setUpdate(":seatsDistribution");
        menuItemList.add(item);

        DefaultSubMenu subMenu = new DefaultSubMenu("メニュー");
        subMenu.setElements(menuItemList);

        menuModel = new DefaultMenuModel();
        menuModel.addElement(subMenu);

        FacesContext.getCurrentInstance().getApplication().addELContextListener(ece -> {
            ImportHandler importHandler = ece.getELContext().getImportHandler();
            importHandler.importPackage("java.util");
            importHandler.importClass("constant.Constants");
        });
    }

    /**
     *
     * @param year
     * @throws SQLException
     */
    public void executeDistribution(int year) throws SQLException {
        setDisplayResult(true);
        setDisplaySimulator(false);

        Map<String, Map<String, Integer>> stateIndependentConstituencySeats = PartyInfoDao.getStateIndependentConstituencySeats(year);
        setStatePopulationInfo(FirstStageDistributionLogic.executeUpperDistribution(year, stateIndependentConstituencySeats));
        setPartySecondsVotesListByState(FirstStageDistributionLogic.executeUnderDistribution(year, getStatePopulationInfo().getStatePopulationInfoList()));
        setFirstStageResult(FirstStageDistributionLogic.getFirstLevelResultList(year, getPartySecondsVotesListByState(), stateIndependentConstituencySeats));
        setPartySeatsInfo(SecondStageDistributionLogic.executeUpperDistribution(year, getFirstStageResult(), stateIndependentConstituencySeats));
        setPartyDistributionList(SecondStageDistributionLogic.executeUnderDistribution(year, getPartySeatsInfo().getPartySeatsInfoList()));
        setSecondStageResult(SecondStageDistributionLogic.tallyElectionResult(getFirstStageResult(), getPartyDistributionList(), stateIndependentConstituencySeats));
    }

    /**
     *
     * @throws SQLException
     */
    public void displayStateName() throws SQLException {
        setDisplaySimulator(true);
        setDisplayResult(false);
        setStateList(new ArrayList<>(Arrays.asList(new StateSimulatorDto())));
        setPartyList(new ArrayList<>(Arrays.asList(new PartyNameDto())));
    }

    /**
     *
     * @throws SQLException
     */
    public void executeSimulation() throws SQLException {
        setDisplayResult(true);
        setStatePopulationInfo(FirstStageDistributionLogic.executeUpperDistribution(getStateList()));
        setPartySecondsVotesListByState(FirstStageDistributionLogic.executeUnderDistribution(getStateList(), getStatePopulationInfo().getStatePopulationInfoList()));
        setFirstStageResult(FirstStageDistributionLogic.getFirstLevelResultList(getStateList(), getPartySecondsVotesListByState()));
        setPartySeatsInfo(SecondStageDistributionLogic.executeUpperDistribution(getStateList(), getFirstStageResult()));
        setPartyDistributionList(SecondStageDistributionLogic.executeUnderDistribution(getStateList(), getPartySeatsInfo().getPartySeatsInfoList()));
        setSecondStageResult(SecondStageDistributionLogic.tallyElectionResult(getFirstStageResult(), getPartyDistributionList(), new HashMap<>()));
    }

    /**
     *
     */
    public void onCellEdit() {
        setDisplayResult(false);
        Map<String, Integer> statePopulationMap = getStateList().stream()
                .collect(Collectors.toMap(StateSimulatorDto::getState, StateSimulatorDto::getPopulation));
        BigDecimal divisor = ElectionMethodLogic.calculateSelectedDivisor(statePopulationMap, Constants.NUMBER_OF_ALL_CONSTITUENCY_SEATS);
        getStateList().forEach(stateInfo
                -> stateInfo.setConstituencySeats(new BigDecimal(stateInfo.getPopulation()).divide(divisor, 3, RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP).intValue())
        );
    }

    /**
     *
     * @param stateInfo
     */
    public void openPartyInfoDialog(StateSimulatorDto stateInfo) {
        setAdditionalPartyInfo(new PartySimulatorDto());
        setTargetStateInfo(stateInfo);
        List<PartySimulatorDto> partyInfoList = stateInfo.getPartyInfoList();
        setMaxSecondVotes(stateInfo.getPopulation()
                - partyInfoList.stream()
                        .mapToInt(PartySimulatorDto::getSecondVotes)
                        .sum());
        setMaxConstituencySeats(stateInfo.getConstituencySeats()
                - partyInfoList.stream()
                        .mapToInt(PartySimulatorDto::getConstituencySeats)
                        .sum());

        Map<String, Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("resizable", true);
        options.put("contentWidth", 830);
        options.put("contentHeight", 180);

        PrimeFaces.current().dialog().openDynamic("partyInfoInput", options, null);
    }

    /**
     *
     * @param event
     */
    public void onRowSelect(SelectEvent event) {
        PartySimulatorDto partyInfo = (PartySimulatorDto) event.getObject();
//        setAdditionalPartyInfo(new PartySimulatorDto());
//        List<PartySimulatorDto> partyInfoList = stateInfo.getPartyInfoList();
//        setMaxSecondVotes(stateInfo.getPopulation()
//                - partyInfoList.stream()
//                        .mapToInt(PartySimulatorDto::getSecondVotes)
//                        .sum());
//        setMaxConstituencySeats(stateInfo.getConstituencySeats()
//                - partyInfoList.stream()
//                        .mapToInt(PartySimulatorDto::getConstituencySeats)
//                        .sum());
//
//        Map<String, Object> options = new HashMap<>();
//        options.put("modal", true);
//        options.put("resizable", false);
//        options.put("contentWidth", 830);
//        options.put("contentHeight", 180);
//
//        PrimeFaces.current().dialog().openDynamic("partyInfoInput", options, null);
    }

    /**
     *
     */
    public void closePartyInfoDialog() {
        PrimeFaces.current().dialog().closeDynamic(getAdditionalPartyInfo());
    }

    /**
     *
     * @param event
     */
    public void onDialogReturn(SelectEvent event) {
        StateSimulatorDto stateInfo = (StateSimulatorDto) event.getComponent().getAttributes().get("stateInfo");
        stateInfo.getPartyInfoList().add((PartySimulatorDto) event.getObject());
    }

    /**
     *
     * @param event
     */
    public void onDisplayContextMenu(SelectEvent event) {
        setSelectedPartyInfo((PartySimulatorDto) event.getObject());
    }

    /**
     *
     * @param stateInfo
     */
    public void deletePartyInfo(StateSimulatorDto stateInfo) {
        stateInfo.getPartyInfoList().remove(getSelectedPartyInfo());
        setSelectedPartyInfo(null);
    }

    public void addStateList() {
        stateList.add(new StateSimulatorDto());
    }

    public void addPartyList() {
        partyList.add(new PartyNameDto());
    }

    /**
     * @return the partySecondsVotesListByState
     */
    public List<FirstStageUnderDistributionDto> getPartySecondsVotesListByState() {
        return partySecondsVotesListByState;
    }

    /**
     * @param partySecondsVotesListByState the partySecondsVotesListByState to
     * set
     */
    public void setPartySecondsVotesListByState(List<FirstStageUnderDistributionDto> partySecondsVotesListByState) {
        this.partySecondsVotesListByState = partySecondsVotesListByState;
    }

    /**
     * @return the statePopulationInfo
     */
    public FirstStageUpperDistributionDto getStatePopulationInfo() {
        return statePopulationInfo;
    }

    /**
     * @param statePopulationInfo the statePopulationInfo to set
     */
    public void setStatePopulationInfo(FirstStageUpperDistributionDto statePopulationInfo) {
        this.statePopulationInfo = statePopulationInfo;
    }

    public List<FirstStageResultDto> getFirstStageResult() {
        return firstStageResult;
    }

    public void setFirstStageResult(List<FirstStageResultDto> firstStageResult) {
        this.firstStageResult = firstStageResult;
    }

    public SecondStageUpperDistributionDto getPartySeatsInfo() {
        return partySeatsInfo;
    }

    public void setPartySeatsInfo(SecondStageUpperDistributionDto partySeatsInfo) {
        this.partySeatsInfo = partySeatsInfo;
    }

    /**
     * @return the partyDistributionList
     */
    public List<SecondStageUnderDistributionDto> getPartyDistributionList() {
        return partyDistributionList;
    }

    /**
     * @param partyDistributionList the partyDistributionList to set
     */
    public void setPartyDistributionList(List<SecondStageUnderDistributionDto> partyDistributionList) {
        this.partyDistributionList = partyDistributionList;
    }

    /**
     * @return the secondStageResult
     */
    public List<SecondStageResultDto> getSecondStageResult() {
        return secondStageResult;
    }

    /**
     * @param secondStageResult the secondStageResult to set
     */
    public void setSecondStageResult(List<SecondStageResultDto> secondStageResult) {
        this.secondStageResult = secondStageResult;
    }

    /**
     * @return the stateList
     */
    public List<StateSimulatorDto> getStateList() {
        return stateList;
    }

    /**
     * @param stateList the stateList to set
     */
    public void setStateList(List<StateSimulatorDto> stateList) {
        this.stateList = stateList;
    }

    /**
     *
     * @return
     */
    public PartySimulatorDto getSelectedPartyInfo() {
        return selectedPartyInfo;
    }

    /**
     *
     * @param selectedPartyInfo
     */
    public void setSelectedPartyInfo(PartySimulatorDto selectedPartyInfo) {
        if (selectedPartyInfo == null) {
            return;
        }
        this.selectedPartyInfo = selectedPartyInfo;
    }

    /**
     * @return the additionalPartyInfo
     */
    public PartySimulatorDto getAdditionalPartyInfo() {
        return additionalPartyInfo;
    }

    /**
     * @param additionalPartyInfo the additionalPartyInfo to set
     */
    public void setAdditionalPartyInfo(PartySimulatorDto additionalPartyInfo) {
        this.additionalPartyInfo = additionalPartyInfo;
    }

    /**
     * @return the maxSecondVotes
     */
    public int getMaxSecondVotes() {
        return maxSecondVotes;
    }

    /**
     * @param maxSecondVotes the maxSecondVotes to set
     */
    public void setMaxSecondVotes(int maxSecondVotes) {
        this.maxSecondVotes = maxSecondVotes;
    }

    /**
     * @return the maxConstituencySeats
     */
    public int getMaxConstituencySeats() {
        return maxConstituencySeats;
    }

    /**
     * @param maxConstituencySeats the maxConstituencySeats to set
     */
    public void setMaxConstituencySeats(int maxConstituencySeats) {
        this.maxConstituencySeats = maxConstituencySeats;
    }

    /**
     * @return the displayResult
     */
    public boolean isDisplayResult() {
        return displayResult;
    }

    /**
     * @param displayResult the displayResult to set
     */
    public void setDisplayResult(boolean displayResult) {
        this.displayResult = displayResult;
    }

    /**
     * @return the displaySimulator
     */
    public boolean isDisplaySimulator() {
        return displaySimulator;
    }

    /**
     * @param displaySimulator the displaySimulator to set
     */
    public void setDisplaySimulator(boolean displaySimulator) {
        this.displaySimulator = displaySimulator;
    }

    /**
     * @return the menuModel
     */
    public MenuModel getMenuModel() {
        return menuModel;
    }

    public List<PartyNameDto> getPartyList() {
        return partyList;
    }

    public void setPartyList(List<PartyNameDto> partyList) {
        this.partyList = partyList;
    }

    public StateSimulatorDto getTargetStateInfo() {
        return targetStateInfo;
    }

    public void setTargetStateInfo(StateSimulatorDto targetStateInfo) {
        this.targetStateInfo = targetStateInfo;
    }
}
