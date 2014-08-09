package hudson.plugins.cigame;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GameDescriptorIntegrationTest extends HudsonTestCase {

    public void testThatSettingCaseInsensitiveFlagWorks() throws Exception {
        GameDescriptor descriptor = hudson.getDescriptorByType(GameDescriptor.class);
        WebClient webClient = new WebClient();
        webClient.setThrowExceptionOnScriptError(false);
        
        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.namesAreCaseSensitive").isChecked(), is(true));
        form.getInputByName("_.namesAreCaseSensitive").setChecked(false);
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(descriptor.getNamesAreCaseSensitive(), is(false));

        form = webClient.goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.namesAreCaseSensitive").isChecked(), is(false));
        form.getInputByName("_.namesAreCaseSensitive").setChecked(true);
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(descriptor.getNamesAreCaseSensitive(), is(true));
    }

    @LocalData
    public void testLoadingCaseInsensitiveFlagWorks() throws Exception {
        GameDescriptor descriptor = hudson.getDescriptorByType(GameDescriptor.class);
        assertThat(descriptor.getNamesAreCaseSensitive(), is(false));
        HtmlForm form = new WebClient().goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.namesAreCaseSensitive").isChecked(), is(false));
    }

    public void testThatSettingGroupUserScoresByFullNameWorks() throws Exception {
        GameDescriptor descriptor = hudson.getDescriptorByType(GameDescriptor.class);
        WebClient webClient = new WebClient();
        webClient.setThrowExceptionOnScriptError(false);

        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.groupUserScoresByFullName").isChecked(), is(false));
        form.getInputByName("_.groupUserScoresByFullName").setChecked(true);
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(descriptor.getGroupUserScoresByFullName(), is(true));

        form = webClient.goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.groupUserScoresByFullName").isChecked(), is(true));
        form.getInputByName("_.groupUserScoresByFullName").setChecked(false);
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(descriptor.getGroupUserScoresByFullName(), is(false));
    }

    @LocalData
    public void testLoadingDaysWithoutBuildFilterWorks() throws Exception {
        GameDescriptor descriptor = hudson.getDescriptorByType(GameDescriptor.class);
        assertThat(descriptor.getDaysWithoutBuildFilter(), is(100));
        HtmlForm form = new WebClient().goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.daysWithoutBuildFilter").getValueAttribute(), is("100"));
    }

    public void testThatSettingDaysWithoutBuildFilterWorks() throws Exception {
        GameDescriptor descriptor = hudson.getDescriptorByType(GameDescriptor.class);
        WebClient webClient = new WebClient();
        webClient.setThrowExceptionOnScriptError(false);

        HtmlForm form = webClient.goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.daysWithoutBuildFilter").getValueAttribute(), is("0"));
        form.getInputByName("_.daysWithoutBuildFilter").setValueAttribute("100");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(descriptor.getDaysWithoutBuildFilter(), is(100));

        form = webClient.goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.daysWithoutBuildFilter").getValueAttribute(), is("100"));
        form.getInputByName("_.daysWithoutBuildFilter").setValueAttribute("0");
        form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
        assertThat(descriptor.getDaysWithoutBuildFilter(), is(0));
    }

    @LocalData
    public void testLoadingGroupUserScoresByFullNameWorks() throws Exception {
        GameDescriptor descriptor = hudson.getDescriptorByType(GameDescriptor.class);
        assertThat(descriptor.getGroupUserScoresByFullName(), is(true));
        HtmlForm form = new WebClient().goTo("configure").getFormByName("config");
        assertThat(form.getInputByName("_.groupUserScoresByFullName").isChecked(), is(true));
    }
}
