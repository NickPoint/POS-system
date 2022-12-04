package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.logic.Team;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class TeamController implements Initializable {

    @FXML
    private Label teamName;
    @FXML
    private Label teamLeader;
    @FXML
    private Label teamLeadEmail;
    @FXML
    private Label teamMembers;

    private final Team team;

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
        teamMembers.setText(String.join("\n", team.getTeamMembers()));
    }

}
