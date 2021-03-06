package com.gethalfpint.views.jade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.neuland.jade4j.JadeConfiguration;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;
import javaslang.control.Match;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * JadeViewRenderer is a views renderer designed to be used for dropwizard
 * in order to render jade templates.
 */
public class JadeViewRenderer implements ViewRenderer {

    private final static Logger logger = LoggerFactory.getLogger(JadeViewRenderer.class);

    private final ObjectMapper mapper;
    private final LoadingCache<Class<? extends View>, JadeConfiguration> cache;

    private Map<String, String> props;

    /**
     * JadeViewRenderer default constructor will create a new instance of
     * the jackson object mapper
     */
    public JadeViewRenderer() {
        this(new ObjectMapper());
    }

    /**
     * JadeViewRenderer constructor that allows you to provide an instance
     * of the object mapper.
     * @param objectMapper
     */
    private JadeViewRenderer(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
        this.cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<? extends View>, JadeConfiguration>() {
                @Override
                public JadeConfiguration load(Class<? extends View> aClass) throws Exception {
                    logger.debug("building new JadeConfiguration for views class {}", aClass);
                    JadeConfiguration config = new JadeConfiguration();
                    config.setTemplateLoader(new JadeTemplateLoader(aClass));
                    return config;
                }
            });
    }

    /**
     * Render method will write the rendered output utilizing the
     * properties on the views class and the template specified by the
     * views to the provided outputstream
     * @param view
     * @param locale
     * @param outputStream
     * @throws IOException
     */
    public void render(View view, Locale locale, OutputStream outputStream) throws IOException {

        logger.debug("rendering jade views {}", view);

        final Map<String, Object> model = Try.of(() -> convert(view))
            .recover(this::recoverFromObjectMapping)
            .orElseThrow(Throwables::propagate);

        logger.debug("views class converted into the following model {}", model);

        Try.of(() -> cache.get(view.getClass()))
            .andThen((config) -> {
                logger.debug("rendering template with name {} and model {}", view.getTemplateName(), model);
                final String rendered = config.renderTemplate(config.getTemplate(view.getTemplateName()), model);
                outputStream.write(
                        rendered.getBytes(view.getCharset().or(Charset.defaultCharset())));
            })
            .onFailure(Throwables::propagate);
    }

    /**
     * Converts the views object into a hashmap using the provided
     * object mapper.
     * @param view
     * @return
     */
    private Map<String, Object> convert(final View view) {
        logger.debug("converting views {} to hash map using provided object mapper", view);
        return mapper.convertValue(view, Map.class);
    }

    /**
     * Method that performs recovery from known exceptions when
     * mapping views object into hash map
     * @param throwable
     * @return
     */
    private Map<String, Object> recoverFromObjectMapping(Throwable throwable) {
        return Match.of(throwable)
            .when((IllegalArgumentException e) -> e.getCause().getMessage().contains("no properties discovered"))
            .then(()->new HashMap())
            .orElseThrow(() -> Throwables.propagate(throwable));
    }


    /**
     * TODO: specify which properties we can configure
     * @param map
     */
    public void configure(Map<String, String> map) {
        this.props = map;
    }

    /**
     * Checks the views to see if the template ends with the jade suffix
     * @param view
     * @return
     */
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(getSuffix());
    }

    /**
     * Returns the suffix to expect for templates
     * @return
     */
    public String getSuffix() {
        return ".jade";
    }

    /**
     * Builder class that can be used to set a custom object mapper on the
     * JadeViewRenderer
     */
    public class Builder {

        private ObjectMapper mapper;

        public Builder withObjectMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public JadeViewRenderer build() {
            return new JadeViewRenderer(this.mapper);
        }

    }
}
