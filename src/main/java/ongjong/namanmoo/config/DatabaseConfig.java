package ongjong.namanmoo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
class DatabaseConfig {

    @Value("${SPRING_DATASOURCE_URL}")
    private String url;

    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String username;

    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String password;

    @Value("${SECURITY_JWT_TOKEN_SECRET_KEY}")
    private String secretKey;
}