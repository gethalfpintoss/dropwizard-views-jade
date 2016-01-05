package com.gethalfpint.views.jade;

import com.gethalfpint.views.jade.utils.TemplateUtil;
import com.gethalfpint.views.jade.views.TestView;
import io.dropwizard.views.View;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class JadeTemplateLoaderTest {

    private JadeTemplateLoader loader;

    @Before
    public void setup(){
        loader = new JadeTemplateLoader(TestView.class);
    }

    @Test
    public void testGetReader() throws IOException {
        TestView view = new TestView("views");
        assertThat(TemplateUtil.templateToString(loader, view),
            equalTo(TemplateUtil.templateRawFileToString(view)));
    }

    @Test
    public void testGetLastModified() throws IOException {
        final View view = view("assertion");
        final long actual = new File(view.getClass().getResource(view.getTemplateName()).getFile())
            .lastModified();
        assertThat(loader.getLastModified(view.getTemplateName()), equalTo(actual));
    }

    private View view(String assertion) {
        return new TestView(assertion);
    }

}
