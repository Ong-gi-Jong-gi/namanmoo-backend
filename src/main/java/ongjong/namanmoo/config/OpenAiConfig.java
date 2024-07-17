package ongjong.namanmoo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAiConfig {
    @Value("${OPENAI_API_KEY}")
    private String openAiKey;

    @Value("${openai-service.http-client.read-timeout}")
    private int readTimeout;

    @Value("${openai-service.http-client.connect-timeout}")
    private int connectTimeout;

    @Bean
    public RestTemplate template() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + openAiKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);
        return factory;
    }
}
