package com.chutneytesting;

import com.chutneytesting.tools.ui.MyMixInForIgnoreType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.sql.DataSource;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class WebConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .findAndRegisterModules();
    }

    @Bean
    public ObjectMapper reportObjectMapper() {
        SimpleModule jdomElementModule = new SimpleModule();
        jdomElementModule.addSerializer(Element.class, new JDomElementSerializer());

        return new ObjectMapper()
            .addMixIn(Resource.class, MyMixInForIgnoreType.class)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
            .registerModule(jdomElementModule)
            .findAndRegisterModules();
    }

    @Bean
    ObjectMapper persistenceObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper.setVisibility(
            objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        );
    }

    @Bean
    JdbcTemplate uiJdbcTemplate(DataSource internalDataSource) {
        return new JdbcTemplate(internalDataSource);
    }

    @Bean
    NamedParameterJdbcTemplate uiNamedParameterJdbcTemplate(JdbcTemplate uiJdbcTemplate) {
        return new NamedParameterJdbcTemplate(uiJdbcTemplate);
    }

    @Bean
    WebServerFactoryCustomizer<AbstractServletWebServerFactory> embeddedServletContainerCustomizer() {
        // We need to explicitly change the assets path
        // because even when in DEV asset are always generated by webpack in target/www
        return this::setLocationForStaticAssets;
    }

    private void setLocationForStaticAssets(AbstractServletWebServerFactory container) {
        File root = new File(resolvePathPrefix() + "ui/dist/chutney/"); // TODO use Path instead ?
        if (root.exists() && root.isDirectory()) {
            container.setDocumentRoot(root);
        }
    }

    /**
     * Resolve path prefix to static resources.
     */
    private String resolvePathPrefix() {
        String fullExecutablePath = this.getClass().getResource("").getPath();
        String rootPath = Paths.get(".").toUri().normalize().getPath();
        String extractedPath = fullExecutablePath.replace(rootPath, "");
        int extractionEndIndex = extractedPath.indexOf("target/");
        if (extractionEndIndex < 0) {
            return "";
        } else if (extractionEndIndex == 0) {
            return "../";
        }
        return rootPath;
    }

    static class JDomElementSerializer extends StdSerializer<Element> {

        private static final long serialVersionUID = 1L;

        protected JDomElementSerializer() {
            this(null);
        }

        protected JDomElementSerializer(Class<Element> t) {
            super(t);
        }

        @Override
        public void serialize(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            String xmlString = new XMLOutputter(Format.getCompactFormat()).outputString(element);
            jsonGenerator.writeObject(xmlString);
        }
    }
}
