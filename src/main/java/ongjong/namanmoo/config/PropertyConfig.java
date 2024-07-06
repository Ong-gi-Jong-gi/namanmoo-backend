package ongjong.namanmoo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.io.InputStream;
import java.util.Properties;

@Profile("!docker")
@Configuration
@PropertySource("classpath:env.properties")
public class PropertyConfig {
}