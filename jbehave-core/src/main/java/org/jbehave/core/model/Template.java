package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.List;

public class Template {

    public static final Template EMPTY = new Template();

    private final String title;
    private final List<String> steps;

    public Template() {
        title = "";
        steps = new ArrayList<String>();
    }

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
