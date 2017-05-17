package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.List;

public class Templates {

    public static final Templates EMPTY = new Templates();

    private final List<Template> templates;

    public Templates() {
        templates = new ArrayList<Template>();
    }

    public Templates(List<Template> templates) {
        this.templates = templates;
    }

    public List<Template> getTemplates() {
        return templates;
    }

}
