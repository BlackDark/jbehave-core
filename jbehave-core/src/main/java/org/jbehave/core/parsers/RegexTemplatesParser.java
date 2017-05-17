package org.jbehave.core.parsers;

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

public class RegexTemplatesParser {

    private RegexStoryParser storyParser;
    private final Keywords keywords;

    public RegexTemplatesParser(RegexStoryParser storyParser) {
        this.storyParser = storyParser;
        this.keywords = storyParser.getKeywords();
    }

    protected Templates parseTemplatesFrom(String storyAsText) {
        List<Template> parsed = new ArrayList<Template>();
        for (String splitted : splitTemplates(storyAsText)) {
            Template template = parseTemplate(splitted);
            if (template != null) {
                parsed.add(template);
            }
        }
        return new Templates(parsed);
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
        Matcher matcher = findingTemplate().matcher(storyAsText);
        return matcher.find() ? matcher.group(1).trim() : RegexStoryParser.NONE;
    }

    private String findTemplateTitle(String templateAsText) {
        Matcher findingTitle = findingTemplateTitle().matcher(templateAsText);
        return findingTitle.find() ? findingTitle.group(1).trim() : NONE;
    }

    private Pattern findingTemplates() {
        String someString = concatenateWithOr(keywords.givenStories(), keywords.lifecycle(), keywords.scenario(), keywords.narrative());
        return compile("(" + keywords.givenTemplate() + ".*?)\\s*(" + someString + ").*", DOTALL);
    }

    private Pattern findingTemplate() {
        String stopWords = concatenateWithOr(keywords.givenStories(), keywords.lifecycle(), keywords.scenario(), keywords.narrative());
        return compile("(" + keywords.template() + ".*?)\\s*(" + stopWords + ").*", DOTALL);
    }

    private Pattern findingTemplateTitle() {
        String startingWords = concatenateWithOr("\\n", "", keywords.startingWords());
        return compile(keywords.template() + "(.*?)\\s*(" + startingWords + "|$).*", DOTALL);
    }
}
