package org.allurefw.allure1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import ru.yandex.qatools.allure.model.TestSuiteResult;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 07.03.16
 */
public class Allure1RouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        JaxbDataFormat jaxb = new JaxbDataFormat();
        jaxb.setContextPath("ru.yandex.qatools.allure.model");

        from("direct:allure1:testSuiteXml")
                .unmarshal(jaxb)
                .bean(Allure1Result.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().with(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
        JacksonDataFormat json = new JacksonDataFormat(mapper, TestSuiteResult.class);

        from("direct:allure1:testSuiteJson")
                .unmarshal(json)
                .bean(Allure1Result.class);
    }
}
