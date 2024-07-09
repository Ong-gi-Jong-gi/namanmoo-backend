package ongjong.namanmoo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServiceProperties {

    @Value("${SECURITY_JWT_TOKEN_SECRET_KEY}")
    private String secretKey;

    @Value("${SPRING_DATASOURCE_URL}")
    private String url;

    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String username;

    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String password;

    @Value("${S3_ACCESS_KEY_ID}")
    private String s3AccessKeyId;

    @Value("${S3_SECRET_ACCESS_KEY}")
    private String s3SecretAccessKey;

    @Value("${S3_BUCKET_NAME}")
    private String s3BucketName;
}