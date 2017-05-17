package org.jbehave.core.model;

import java.util.List;

public class Template {

    private final String title;
    private final List<String> steps;

    public Template(String title, List<String> steps) {
        this.title = title;
        this.steps = steps;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getSteps() {
        return steps;
    }

}
