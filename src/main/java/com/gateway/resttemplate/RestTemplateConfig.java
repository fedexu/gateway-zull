package com.gateway.resttemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        converter.setObjectMapper(mapper);

        return restTemplateBuilder
                .errorHandler(new RestTemplateResponseErrorHandler())
                .interceptors(Collections.singletonList(new HeaderInterceptor()))
                .messageConverters(converter)
                .build();
    }

}