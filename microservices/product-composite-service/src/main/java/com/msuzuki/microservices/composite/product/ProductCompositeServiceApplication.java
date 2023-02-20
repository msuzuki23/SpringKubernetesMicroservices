package com.msuzuki.microservices.composite.product;

import com.msuzuki.microservices.composite.product.services.ProductCompositeIntegration;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
@ComponentScan("com.msuzuki")
public class ProductCompositeServiceApplication {
    @Value("${api.common.version}") String apiVersion;
    @Value("${api.common.title}") String apiTitle;
    @Value("${api.common.description}") String apiDescription;
    @Value("${api.common.termsOfService}") String apiTermsOfService;
    @Value("${api.common.license}") String apiLicense;
    @Value("${api.common.licenseUrl}") String apiLicenseUrl;
    @Value("${api.common.externalDocDesc}") String apiExternalDocDesc;
    @Value("${api.common.externalDocUrl}") String apiExternalDocUrl;
    @Value("${api.common.contact.name}") String apiContactName;
    @Value("${api.common.contact.url}") String apiContactUrl;
    @Value("${api.common.contact.email}") String apiContactEmail;

    /**
     *  Will be exposed on $HOST:/$PORT/swagger-ui.html
     *
     * @return the common OpenAPI documentation
     */

    @Bean
    public OpenAPI getOpenApiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .description(apiDescription)
                        .version(apiVersion)
                        .contact(new Contact()
                                    .name(apiContactName)
                                    .url(apiContactUrl)
                                    .email(apiContactEmail))
                        .termsOfService(apiTermsOfService)
                        .license(new License()
                                    .name(apiLicense)
                                    .url(apiLicenseUrl)
                        ))
                .externalDocs(new ExternalDocumentation()
                                    .description(apiExternalDocDesc)
                                    .url(apiExternalDocUrl)
                );
    }

    private final Integer threadPoolSize;
    private final Integer taskQueueSize;

    @Autowired
    public ProductCompositeServiceApplication(
            @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
            @Value("${app.taskQueueSize:100}") Integer taskQueueSize
    ) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    public Scheduler publishEventScheduler() {
        log.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
        return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalanceWebClientBuilder() { return WebClient.builder(); }

    public static void main(String[] args) {
        SpringApplication.run(ProductCompositeServiceApplication.class, args);
    }
}