package hudson.plugins.cigame;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Leader board for users participating in the game.
 * 
 * @author Erik Ramfelt
 */
@ExportedBean(defaultVisibility = 999)
@Extension
public class LeaderBoardAction implements RootAction, AccessControlled {

    private static final long serialVersionUID = 1L;

    public LeaderBoardAction() {
        super();
    }

    public String getDisplayName() {
        return Messages.Leaderboard_Title();
    }

    public String getIconFileName() {
        return GameDescriptor.ACTION_LOGO_MEDIUM;
    }

    public String getUrlName() {
        return "/cigame"; //$NON-NLS-1$
    }

    /**
     * Returns the user that are participants in the ci game
     * 
     * @return list containing users.
     */
    @Exported
    public List<UserScore> getUserScores() {
        GameDescriptor gameDescriptor = Hudson.getInstance().getDescriptorByType(GameDescriptor.class);

        Collection<User> users;

        int daysWithoutBuildFilter = gameDescriptor.getDaysWithoutBuildFilter();

        if (daysWithoutBuildFilter >0) {
            users = getUserActiveInTheLastDays(daysWithoutBuildFilter);
        } else {
            users = User.getAll();
        }

        return getUserScores(users, gameDescriptor.getNamesAreCaseSensitive(), gameDescriptor.getGroupUserScoresByFullName());
    }

     Collection<User> getUserActiveInTheLastDays(int days) {
        Set<User> recentUsers = new HashSet<User>();
        long now = System.currentTimeMillis();
        long last = now - (24 * 60 * 60 * 1000) * (long)days;
        List<AbstractProject> projects = getAllProjects();
        for (AbstractProject<?,?> p : projects) {
            for (AbstractBuild<?, ?> b : p.getBuilds().byTimestamp(last, now)) {
                for (ChangeLogSet.Entry e : b.getChangeSet()) {
                    recentUsers.add(e.getAuthor());
                }
            }
        }
        return recentUsers;
    }

    protected List<AbstractProject> getAllProjects() {
        return Jenkins.getInstance().getAllItems(AbstractProject.class);
    }

    @Exported
    public boolean isUserAvatarSupported() {
        return new VersionNumber(Hudson.VERSION).isNewerThan(new VersionNumber("1.433"));
    }

    List<UserScore> getUserScores(Collection<User> users, boolean usernameIsCasesensitive, boolean groupUserScoresByFullName) {
        ArrayList<UserScore> list = new ArrayList<UserScore>();

        Collection<User> players;
        if (usernameIsCasesensitive) {
            players = users;
        } else {
            List<User> playerList = new ArrayList<User>();
            CaseInsensitiveUserIdComparator caseInsensitiveUserIdComparator = new CaseInsensitiveUserIdComparator();
            for (User user : users) {

                if (Collections.binarySearch(playerList, user, caseInsensitiveUserIdComparator) < 0) {
                    playerList.add(user);
                }
            }
            players = playerList;
        }

        if (!groupUserScoresByFullName) {
            for (User user : players) {
                UserScoreProperty property = user.getProperty(UserScoreProperty.class);
                if ((property != null) && property.isParticipatingInGame()) {
                    list.add(new UserScore(user, property.getScore(), user.getDescription()));
                }
            }
        } else {
            Map<String, User> usersMap = new HashMap<String, User>();
            Map<String, Double> scoresMap = new HashMap<String, Double>();

            for (User user : players) {
                UserScoreProperty property = user.getProperty(UserScoreProperty.class);
                if ((property != null) && property.isParticipatingInGame()) {
                    String key;
                    if (user.getFullName() == null) {
                        key = user.getId();
                    } else {
                        key = user.getFullName();
                    }
                    usersMap.put(key, user);
                    Double oldScore = scoresMap.get(key);
                    if (oldScore == null) {
                        scoresMap.put(key, property.getScore());
                    } else {
                        scoresMap.put(key, oldScore.doubleValue() + property.getScore());
                    }
                }
            }

            for (Map.Entry<String, Double> score : scoresMap.entrySet()) {
                User user = usersMap.get(score.getKey());
                list.add(new UserScore(user, score.getValue(), user.getDescription()));
            }

        }

        Collections.sort(list, new Comparator<UserScore>() {
            public int compare(UserScore o1, UserScore o2) {
                if (o1.score < o2.score)
                    return 1;
                if (o1.score > o2.score)
                    return -1;
                return 0;
            }
        });

        return list;
    }

    public void doResetScores( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if (Hudson.getInstance().getACL().hasPermission(Hudson.ADMINISTER)) {
            doResetScores(User.getAll());
        }
        rsp.sendRedirect2(req.getContextPath());
    }

    void doResetScores(Collection<User> users) throws IOException {
        for (User user : users) {
            UserScoreProperty property = user.getProperty(UserScoreProperty.class);
            if (property != null) {
                property.setScore(0);
                user.save();
            }
        }
    }

    
    @ExportedBean(defaultVisibility = 999)
    public static class UserScore {
        private User user;
        private double score;
        private String description;

        public UserScore(User user, double score, String description) {
            super();
            this.user = user;
            this.score = score;
            this.description = description;
        }

        @Exported
        public User getUser() {
            return user;
        }

        @Exported
        public double getScore() {
            return score;
        }

        @Exported
        public String getDescription() {
            return description;
        }
    }

    public ACL getACL() {
        return Hudson.getInstance().getACL();
    }

    public void checkPermission(Permission p) {
        getACL().checkPermission(p);
    }

    public boolean hasPermission(Permission p) {
        return getACL().hasPermission(p);
    }
}
