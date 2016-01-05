package com.gethalfpint.views.jade.views;

import io.dropwizard.views.View;

/**
 * Created by halfpint on 1/4/16.
 */
public class TestView extends View {

    private final String assertion;

    public TestView(String assertion) {
        super("test.jade");
        this.assertion = assertion;
    }

    public String getAssertion() {
        return assertion;
    }
}
