package com.usian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Mr.D
 * @date 2021年12月08日 11:00
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                //api信息：标题和描述
                .apiInfo(apiInfo())
                //swagger的注解所在包
                .select().apis(RequestHandlerSelectors.basePackage("com.usian.controller")).paths(PathSelectors.any())
                .build();
    }

    /**
     * api的标题和描述
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("优思安商城接口文档")
                .description("这是优思安商城的接口文档")
                .version("1.0")
                .build();
    }

}
