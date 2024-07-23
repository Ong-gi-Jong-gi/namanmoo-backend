package ongjong.namanmoo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class FcmInitializer {
    @Value("${firebase.key-path}")
    private String firebaseKeyPath;

    @PostConstruct
    public void initializeFirebase() {
        try {
            log.info("Initializing Firebase with key path: {}", firebaseKeyPath);
            InputStream serviceAccount = new ClassPathResource(firebaseKeyPath).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase has been initialized");
            } else {
                log.info("FirebaseApp is already initialized");
            }
        } catch (IOException e) {
            log.error("Could not initialize Firebase", e);
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }
}
