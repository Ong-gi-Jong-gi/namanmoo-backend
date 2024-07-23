package ongjong.namanmoo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.key-path}")
    private Resource firebaseConfigPath;

    @Bean
    public FirebaseApp initializeFirebase() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(firebaseConfigPath.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("complete:{}",firebaseConfigPath);
                return FirebaseApp.initializeApp(options);
            } else {
                return FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }
}