package com.gethalfpint.views.jade;

import com.google.common.base.Throwables;
import de.neuland.jade4j.template.TemplateLoader;
import io.dropwizard.views.View;
import javaslang.control.Match;
import javaslang.control.Try;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * JadeTemplateLoader handles loading the template files
 * from the same corresponding package that the original
 * views that utilizes the template is defined in.  The template
 * loader will also handle checking the last modified date
 * on the template file to reload the template if it has
 * been modified.
 */
public class JadeTemplateLoader implements TemplateLoader {

    private final Class<? extends View> clazz;

    /**
     * JadeTemplateLoader constructor takes the class for the views that is being used.
     * This will then be used to find the actual jade template in the same package
     * as the views class.
     * @param clazz
     */
    JadeTemplateLoader(Class<? extends View> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get the last modified date of actual resource if running from
     * file system otherwise return the last modified date of the jarfile
     * if we are running from inside a jar file
     * @param source
     * @return
     * @throws IOException
     */
    public long getLastModified(String source) throws IOException {
        return lastModifiedFromResource(clazz.getResource(source));
    }

    /**
     * Get a reader that can be used to read the template
     * @param source
     * @return
     * @throws IOException
     */
    public Reader getReader(String source) throws IOException {
        final InputStream is = clazz.getResourceAsStream(source);
        if (is == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    private long lastModifiedFromResource(URL url) {
        return new File(
                Match.of(url)
                        .when((URL val) -> new Boolean(val.getProtocol().equals("file"))).then(url::getFile)
                        .when((URL val) -> new Boolean(val.getProtocol().equals("jar"))).then((URL val) -> {
                    return Try.of(()->((JarURLConnection) val.openConnection()).getJarFile().getName())
                        .onFailure(Throwables::propagate)
                        .get();
                })
                .get())
            .lastModified();
    }
}
