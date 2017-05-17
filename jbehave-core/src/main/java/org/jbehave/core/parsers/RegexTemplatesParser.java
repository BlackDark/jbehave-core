package org.jbehave.core.parsers;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.jbehave.core.parsers.RegexStoryParser.NONE;
import static org.jbehave.core.parsers.RegexStoryParser.concatenateWithOr;
import static org.jbehave.core.parsers.RegexStoryParser.startingWithNL;

/**
 * Pattern-based parser for matching template related keywords.
 */
public class RegexTemplatesParser {

    private RegexStoryParser storyParser;
    private final Keywords keywords;
    private static boolean isInitialized = false;
    private static Pattern givenTemplatePattern;
    private static Pattern templatePattern;
    private static Pattern templateTitlePattern;

    public RegexTemplatesParser(RegexStoryParser storyParser) {
        this.storyParser = storyParser;
        this.keywords = storyParser.getKeywords();
        initializePatterns();
    }

    public Templates parseTemplatesFrom(String storyAsText) {
        List<Template> parsed = new ArrayList<Template>();
        for (String split : splitTemplates(storyAsText)) {
            parsed.add(parseTemplate(split));
        }
        assertNoDuplicateTitles(parsed);
        return new Templates(parsed);
    }

    /**
     * Replaces GivenTemplate: steps with the according template steps.
     * @param steps The original step list. Will NOT be modified.
     * @param templates The collected templates from the story. See {@link #parseTemplatesFrom(String)}.
     * @return New step list with all included steps.
     */
    public List<String> includeTemplateInSteps(List<String> steps, Templates templates) {
        List<String> extendedSteps = new ArrayList<String>();

        for (String step : steps) {
            Matcher matcher = givenTemplatePattern.matcher(step);
            if (matcher.find()) {
                String templateTitle = matcher.group(1).trim();
                Template template = templates.getTemplateWithTitle(templateTitle);

                if (template == null) {
                    throw new IllegalStateException("No template found for template name '" + templateTitle + "'");
                }

                extendedSteps.addAll(template.getSteps());
            } else {
                extendedSteps.add(step);
            }
        }

        return extendedSteps;
    }

    private void initializePatterns() {
        if (isInitialized) {
            return;
        }

        if (keywords == null) {
            throw new IllegalStateException("Cannot initialize template parser if keyword parameter is null.");
        }

        givenTemplatePattern = compile(keywords.givenTemplate() + "(.*)", DOTALL);

        String stopWords = concatenateWithOr(keywords.givenStories(), keywords.lifecycle(), keywords.scenario(), keywords.narrative());
        templatePattern = compile("(" + keywords.template() + ".*?)\\s*(" + stopWords + ").*", DOTALL);

        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        templateTitlePattern = compile(keywords.template() + "(.*?)\\s*(" + startingWords + "|$).*", DOTALL);

        isInitialized = true;
    }

    private void assertNoDuplicateTitles(List<Template> parsed) {
        Set<String> checkDuplicate = new HashSet<String>();
        for (Template template : parsed) {
            if (!checkDuplicate.add(template.getTitle())) {
                throw new IllegalStateException("Duplicate title in templates found for '" + template.getTitle() + "'");
            }
        }
    }

    private List<String> splitTemplates(String storyAsText) {
        List<String> templates = new ArrayList<String>();
        String templateKeyword = keywords.template();

        String templateText = findTemplateText(storyAsText);

        for (String templateAsText : templateText.split(templateKeyword)) {
            if (templateAsText.trim().length() > 0) {
                templates.add(templateKeyword + "\n" + templateAsText);
            }
        }

        return templates;
    }

    private Template parseTemplate(String templateAsText) {
        String title = findTemplateTitle(templateAsText);
        if (StringUtils.isEmpty(title)) {
            throw new IllegalArgumentException("The title of a template is not filled. Please fill the title.");
        }
        String templateWithoutKeyword = removeStart(templateAsText, keywords.template()).trim();
        String templateWithoutTitle = removeStart(templateWithoutKeyword, title);
        templateWithoutTitle = startingWithNL(templateWithoutTitle);

        List<String> steps = storyParser.findSteps(templateWithoutTitle);
        return new Template(title, steps);
    }

    private String findTemplateText(String storyAsText) {
        Matcher matcher = templatePattern.matcher(storyAsText);
        return matcher.find() ? matcher.group(1).trim() : RegexStoryParser.NONE;
    }

    private String findTemplateTitle(String templateAsText) {
        Matcher findingTitle = templateTitlePattern.matcher(templateAsText);
        return findingTitle.find() ? findingTitle.group(1).trim() : NONE;
    }
}
