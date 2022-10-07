package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.logic.TeamView;
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

    private TeamView teamView;

    public TeamViewController(TeamView teamView) {
        this.teamView = teamView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        teamName.setText(teamView.getTeamName());
        teamLeader.setText(teamView.getTeamLeader());
        teamLeadEmail.setText(teamView.getTeamLeaderEmail());
        teamMembers.setText(teamView.getTeamMembers());
    }

}
