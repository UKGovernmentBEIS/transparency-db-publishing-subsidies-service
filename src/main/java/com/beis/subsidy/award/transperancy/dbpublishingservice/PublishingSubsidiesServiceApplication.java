package com.beis.subsidy.award.transperancy.dbpublishingservice;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.feign.GraphAPIFeignClient;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.feign.GraphAPILoginFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@SpringBootApplication
@EnableFeignClients(basePackages = {
		"com.beis.subsidy.award.transperancy.dbpublishingservice.controller.feign" },
		basePackageClasses = { GraphAPIFeignClient.class, GraphAPILoginFeignClient.class })
public class PublishingSubsidiesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublishingSubsidiesServiceApplication.class, args);
	}
	
	@Bean
    public OpenAPI customOpenAPI(@Value("${application-description}") String appDesciption, @Value("${application-version}") String appVersion) {
     return new OpenAPI()
          .info(new Info()
          .title("BEIS Subsidy Control - Publishing Subsidy Awards")
          .version("1.0")
          .description("BEIS Subsidy Control - Publishing Subsidy Awards APIs for transparency database")
          .termsOfService("http://swagger.io/terms/")
          .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
	
	
}
