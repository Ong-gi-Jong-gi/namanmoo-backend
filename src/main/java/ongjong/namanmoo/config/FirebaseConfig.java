package ongjong.namanmoo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    private static final String FIREBASE_CONFIG_PATH = "mooluck-fcm-firebase-adminsdk.json";

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        // FirebaseApp 인스턴스가 이미 존재하는지 확인
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(FIREBASE_CONFIG_PATH);

            if (serviceAccount == null) {
                throw new RuntimeException("Firebase config file not found" + FIREBASE_CONFIG_PATH);
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
