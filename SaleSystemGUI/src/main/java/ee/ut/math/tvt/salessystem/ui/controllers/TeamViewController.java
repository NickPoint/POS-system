package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.logic.Team;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class TeamViewController implements Initializable {

    @FXML
    private Text teamName;
    @FXML
    private Text teamLeader;
    @FXML
    private Text teamLeadEmail;
    @FXML
    private Text teamMembers;

    private Team team;

    public TeamViewController(Team team) {
        this.team = team;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        teamName.setText(team.getTeamName());
        teamLeader.setText(team.getTeamLeader());
        teamLeadEmail.setText(team.getTeamLeaderEmail());
        teamMembers.setText(team.getTeamMembers());
    }

}
