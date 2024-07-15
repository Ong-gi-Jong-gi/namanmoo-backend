package ongjong.namanmoo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lucky_id")
    private Lucky lucky;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private int challengeNum;

    private long createDate;

}
