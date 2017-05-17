package org.jbehave.core.parsers;

import org.hamcrest.Matchers;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.model.Template;
import org.jbehave.core.model.Templates;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RegexTemplatesParserTest {

    private static final String NL = "\n";
    private RegexStoryParser parser = new RegexStoryParser(new LocalizedKeywords(), new TableTransformers());

    @Test
    public void shouldParseOneTemplateFromStory() throws Exception {
        String wholeStory = "Template: Some title" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +
                "Scenario: Test" + NL +
                "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";
        Templates templates = new RegexTemplatesParser(parser).parseTemplatesFrom(wholeStory);

        assertThat(templates.getTemplates(), hasSize(1));
        assertThat(templates.getTemplates().get(0).getTitle(), equalTo("Some title"));
        assertThat(templates.getTemplates().get(0).getSteps(), hasSize(2));
    }

    @Test
    public void shouldParseMultipleTemplatesFromStory() throws Exception {
        String wholeStory = "Template: FancyTitle" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +

                "Template: Title1" + NL +
                "Given some hunk" + NL +

                "Template: EmptyTemplate" + NL +

                "Template: Title2" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +

                "Scenario: Test" + NL +
                "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";

        Templates templates = new RegexTemplatesParser(parser).parseTemplatesFrom(wholeStory);

        assertThat(templates.getTemplates(), hasSize(4));

        Template template0 = templates.getTemplates().get(0);
        assertThat(template0.getTitle(), equalTo("FancyTitle"));
        assertThat(template0.getSteps(), hasSize(4));

        Template template1 = templates.getTemplates().get(1);
        assertThat(template1.getTitle(), equalTo("Title1"));
        assertThat(template1.getSteps(), hasSize(1));

        Template template2 = templates.getTemplates().get(2);
        assertThat(template2.getTitle(), equalTo("EmptyTemplate"));
        assertThat(template2.getSteps(), is(Matchers.<String>empty()));

        Template template3 = templates.getTemplates().get(3);
        assertThat(template3.getTitle(), equalTo("Title2"));
        assertThat(template3.getSteps(), hasSize(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyTemplateTitle() throws Exception {
        String wholeStory = "Template:" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +
                "Scenario: Test" + NL +
                "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";

        new RegexTemplatesParser(parser).parseTemplatesFrom(wholeStory);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnDuplicateTitle() throws Exception {
        String wholeStory = "Template: Duplicate" + NL +
                "Given some hunk" + NL +
                "Template: Duplicate" + NL +
                "Given some hunk" + NL +
                "Scenario: Test" + NL +
                "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";

        new RegexTemplatesParser(parser).parseTemplatesFrom(wholeStory);
    }

    @Test
    public void shouldNormallyProcessWithGivenTemplate() throws Exception {
        String wholeStory = "Template: SomeTemplate" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +
                "Scenario: Test" + NL +
                "GivenTemplate: SomeTemplate" + NL +
                "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";
        Templates templates = new RegexTemplatesParser(parser).parseTemplatesFrom(wholeStory);

        assertThat(templates.getTemplates(), hasSize(1));
        assertThat(templates.getTemplates().get(0).getTitle(), equalTo("SomeTemplate"));
        assertThat(templates.getTemplates().get(0).getSteps(), hasSize(2));
    }

    @Test
    public void shouldReplaceTemplateCorrectly() throws Exception {
        String wholeStory = "Template: SomeTemplate" + NL +
                "Given some hunk" + NL +
                "When you crash that" + NL +

                "Scenario: Test" + NL +
                "GivenTemplate: SomeTemplate" + NL +
                "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";

        RegexTemplatesParser templatesParser = new RegexTemplatesParser(parser);
        Templates templates = templatesParser.parseTemplatesFrom(wholeStory);

        List<String> steps = Arrays.asList("GivenTemplate: SomeTemplate" + NL, "Given a scenario");
        List<String> newSteps = templatesParser.includeTemplateInSteps(steps, templates);

        assertThat(newSteps, hasSize(3));
    }

}
