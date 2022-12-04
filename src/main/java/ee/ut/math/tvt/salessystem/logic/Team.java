package ee.ut.math.tvt.salessystem.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class Team {
    private String teamName;
    private String teamLeader;
    private String teamLeaderEmail;
    private String[] teamMembers;
    private final Logger log = LogManager.getLogger(Team.class);


    public Team() {
        log.info("Loading team information from properties file");
        String path = "application.properties";
        log.debug("File path: " + path);
        try (InputStream fis = this.getClass().getResourceAsStream(path)) {
            Properties prop = new Properties();
            prop.load(fis);
            log.info("Team information is loaded");
            this.teamName = prop.getProperty("team.name");
            this.teamLeader = prop.getProperty("team.leader");
            this.teamLeaderEmail = prop.getProperty("team.leader.email");
            this.teamMembers = prop.getProperty("team.members").split("\\|");
            log.debug("Loaded info: "+this);
        } catch (IOException e) {
            log.error("Exception was thrown while reading the file", e);
        }
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamLeader() {
        return teamLeader;
    }

    public String getTeamLeaderEmail() {
        return teamLeaderEmail;
    }

    public String[] getTeamMembers() {
        return teamMembers;
    }

    @Override
    public String toString() {
        return String.format("teamName: %s, teamLeader: %s, teamLeaderEmail: %s, teamMembers: %s",
                teamName, teamLeader, teamLeaderEmail, String.join(" ",teamMembers)
        );
    }
}
