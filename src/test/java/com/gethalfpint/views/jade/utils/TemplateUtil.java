package com.gethalfpint.views.jade.utils;

import com.gethalfpint.views.jade.JadeTemplateLoader;
import io.dropwizard.views.View;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Created by halfpint on 1/4/16.
 */
public class TemplateUtil {

    public static String templateToString(JadeTemplateLoader loader, View view) throws IOException {
        return new BufferedReader(loader.getReader(view.getTemplateName()))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String templateRawFileToString(View view) {
        return new BufferedReader(new InputStreamReader(view.getClass().getResourceAsStream(view.getTemplateName())))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String renderedRawFileToString(View view) {
        return new BufferedReader(new InputStreamReader(view.getClass().getResourceAsStream(String.format("%s%s", FilenameUtils.getBaseName(view.getTemplateName()), ".html"))))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
