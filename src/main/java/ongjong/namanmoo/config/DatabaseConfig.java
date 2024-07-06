package ongjong.namanmoo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DatabaseConfig {

    @Value("${application.spring.datasource.url}")
    private String url;

    @Value("${application.spring.datasource.username}")
    private String username;

    @Value("${application.spring.datasource.password}")
    private String password;

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
}