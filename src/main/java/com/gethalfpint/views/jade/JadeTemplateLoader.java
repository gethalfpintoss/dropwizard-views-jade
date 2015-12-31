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
 * Created by halfpint on 12/31/15.
 */
public class JadeTemplateLoader implements TemplateLoader {

    private final Class<? extends View> clazz;

    JadeTemplateLoader(Class<? extends View> clazz) {
        this.clazz = clazz;
    }

    public long getLastModified(String source) throws IOException {
        return lastModifiedFromResource(clazz.getResource(source));
    }

    public Reader getReader(String source) throws IOException {
        final InputStream is = clazz.getResourceAsStream(source);
        if (is == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    /**
     * Get the last modified date of actual resource if running from
     * file system otherwise return the last modified date of the jarfile
     * @param url
     * @return
     */
    private long lastModifiedFromResource(URL url) {
        return new File(
            Match.of(url)
                .when((URL val)->val.getProtocol().equals("file")).then(url::getFile)
                .when((URL val)->val.getProtocol().equals("jar"))
                    .then((URL val) -> {
                        return Try.of(()->((JarURLConnection) val.openConnection()).getJarFile().getName())
                            .onFailure(Throwables::propagate)
                            .get();
                    })
                .get()).lastModified();
    }
}
