package com.gethalfpint.views.jade;

import com.gethalfpint.views.jade.utils.TemplateUtil;
import com.gethalfpint.views.jade.views.TestNoPropsView;
import com.gethalfpint.views.jade.views.TestView;
import io.dropwizard.views.View;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class JadeViewRendererTest {

    private JadeViewRenderer renderer;

    @Before
    public void setup() {
        renderer = new JadeViewRenderer();
    }

    @Test
    public void testRender() throws IOException {
        View view = new TestView("assertion");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        renderer.render(view, Locale.US, output);
        assertThat(output.toString(), equalTo(TemplateUtil.renderedRawFileToString(view)));
    }

    @Test
    public void testNoProps() throws IOException {
        View view = new TestNoPropsView();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        renderer.render(view, Locale.US, output);
        assertThat(output.toString(), equalTo(TemplateUtil.renderedRawFileToString(view)));
    }

}
