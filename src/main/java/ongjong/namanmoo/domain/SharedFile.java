package ongjong.namanmoo.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedFileId;

    private String fileName;

    private String fileType;

    private int challengeNum;
}
