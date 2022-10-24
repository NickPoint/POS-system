package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.logic.Team;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class TeamController implements Initializable {

    @FXML
    private Text teamName;
    @FXML
    private Text teamLeader;
    @FXML
    private Text teamLeadEmail;
    @FXML
    private Text teamMembers;

    private Team team;

    private final Logger log = LogManager.getLogger(TeamController.class);

    public TeamController(Team team) {
        this.team = team;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Setting team view fields:");
        teamName.setText(team.getTeamName());
        teamLeader.setText(team.getTeamLeader());
        teamLeadEmail.setText(team.getTeamLeaderEmail());
        teamMembers.setText(team.getTeamMembers());
    }

}
