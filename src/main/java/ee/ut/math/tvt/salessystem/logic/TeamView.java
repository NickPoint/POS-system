package ee.ut.math.tvt.salessystem.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.Stream;

public class TeamView {
    private String teamName;
    private String teamLeader;
    private String teamLeaderEmail;
    private String teamMembers;

    public TeamView() throws IOException {
        try (InputStream fis = new FileInputStream("./src/main/resources/application.properties")) {
            Properties prop = new Properties();
            prop.load(fis);
            this.teamName = prop.getProperty("team.name");
            this.teamLeader = prop.getProperty("team.leader");
            this.teamLeaderEmail = prop.getProperty("team.leader.email");
            this.teamMembers = prop.getProperty("team.members");
        } catch (IOException e) {
            System.out.println("File properties not found!");
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

    public String getTeamMembers() {
        return teamMembers;
    }
}
