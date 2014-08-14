package hudson.plugins.cigame;

import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import hudson.model.Hudson;
import hudson.model.User;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class LeaderBoardActionIntegrationTest extends HudsonTestCase {

    @LocalData
    public void testThatUsernameWithDifferentCasingIsDisplayedAsOne() throws Exception {
        Collection<User> users;
        Map<String, String> scores;


        Date testDate = new SimpleDateFormat("yyyy-MM-dd").parse("2014-08-01");
        Date testDate2 = new SimpleDateFormat("yyyy-MM-dd").parse("2009-09-02");

        int daysSinceTheLastBuild =(int) ((System.currentTimeMillis() - testDate.getTime()) / (24 * 60 * 60 * 1000));
        int daysSinceTheFirstBuild =(int) ((System.currentTimeMillis() - testDate2.getTime()) / (24 * 60 * 60 * 1000));

        //
        //first run some checks using the action directly
        //
        LeaderBoardAction leaderBoardAction = new LeaderBoardAction();

        // just before the last build, one user is active
        users = leaderBoardAction.getUserActiveInTheLastDays(daysSinceTheLastBuild+1);
        assertThat(users.size(), is(1));
        User user = users.iterator().next();
        assertThat(user.getId(), is("activeuser"));

        // just before the previous build, 2 users are active
        users = leaderBoardAction.getUserActiveInTheLastDays(daysSinceTheFirstBuild+1);
        assertThat(users.size(), is(2));

        // after the last build, nobody found!
        users = leaderBoardAction.getUserActiveInTheLastDays(daysSinceTheLastBuild-1);
        assertThat(users.size(), is(0));

        //
        // then check in the GUI with variousDaysWithoutBuildFilter

        // just before the last build, one user is active
//        hudson.getDescriptorByType(GameDescriptor.class).setDaysWithoutBuildFilter(daysSinceTheLastBuild+1);
//        scores = rebuildScores();
//        assertThat(scores.size(), is(1));
//        assertThat(scores.get("activeuser"), is("1.0"));
//
//        // just before the previous build, 2 users are active
//        hudson.getDescriptorByType(GameDescriptor.class).setDaysWithoutBuildFilter(daysSinceTheFirstBuild+1);
//        scores = rebuildScores();
//        assertThat(scores.size(), is(2));
//        assertThat(scores.get("activeuser"), is("1.0"));
//        assertThat(scores.get("mindless"), is("1.0"));
//
//        // after the last build, nobody found!
//        hudson.getDescriptorByType(GameDescriptor.class).setDaysWithoutBuildFilter(daysSinceTheLastBuild-1);
//        scores = rebuildScores();
//        assertThat(scores.size(), is(0));

        // finalkly check GroupUserScoresByFullName
        //no filter, all 3 users are returned
        Hudson.getInstance().getDescriptorByType(GameDescriptor.class).setDaysWithoutBuildFilter(0);
        hudson.getDescriptorByType(GameDescriptor.class).setGroupUserScoresByFullName(false);
        scores = rebuildScores();
        assertThat(scores.size(), is(3));
        assertThat(scores.get("activeuser"), is("1.0"));
        assertThat(scores.get("activeuser2"), is("1.0"));
        assertThat(scores.get("mindless"), is("1.0"));

        // no activity filter, but this time group the scores with the user that share its full name
        hudson.getDescriptorByType(GameDescriptor.class).setDaysWithoutBuildFilter(0);
        hudson.getDescriptorByType(GameDescriptor.class).setGroupUserScoresByFullName(true);
        scores = rebuildScores();
        assertThat(scores.size(), is(2));
        assertThat(scores.get("activeuser"), is("2.0"));
        assertThat(scores.get("mindless"), is("1.0"));

    }

    private Map<String, String> rebuildScores() throws IOException, SAXException {
        HtmlTable table = (HtmlTable) new WebClient().goTo("cigame/").getHtmlElementById("game.leaderboard");
        Map<String,String> scores= new HashMap<String,String>();
        for (HtmlTableRow htmlTableRow : table.getRows()) {

            String username = getUsername(htmlTableRow);
            if (!username.equals("Participant")) {
                scores.put(username, getUserScore(htmlTableRow));
            }
        }
        return scores;
    }

    private String getUserScore(HtmlTableRow row) {
        return row.getCell(4).getFirstChild().getNodeValue();
    }

    private String getUsername(HtmlTableRow row) {
        return row.getCell(2).getFirstChild().getFirstChild().getNodeValue();
    }


}
