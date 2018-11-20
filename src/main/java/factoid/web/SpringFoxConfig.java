package factoid.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
//@Import(BeanValidatorPluginsConfiguration.class) //JSR-303 (if controllers have bean args and validation annotations)
public class SpringFoxConfig {
    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
//            .apis(RequestHandlerSelectors.any()) //this then shows built-in Spring actuators, e.g., health, devel.
            .apis(RequestHandlerSelectors.basePackage("factoid.web"))
//            .paths(PathSelectors.any())
            .paths(PathSelectors.ant("/v2/**"))
            .build()
            .useDefaultResponseMessages(false)
            .apiInfo(getApiInfo());
    }

    private ApiInfo getApiInfo() {
        return new ApiInfo(
            "Factoid data converters",
            "A RESTful web service, built with Spring Boot and Paxtools java libraries, that converts " +
              "Factoid documents to BioPAX, SBGN formats.",
            "1", //api
            "free",
            new Contact("Pathway Commons",
                "http://www.pathwaycommons.org",
                "pathway-commons-help@googlegroups.com"
            ),
            "MIT",
            "https://raw.githubusercontent.com/PathwayCommons/factoid-converters/master/LICENSE",
            Collections.emptyList()
        );
    }
}
