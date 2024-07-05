package ongjong.namanmoo.domain;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Getter @Setter
@Entity
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;

    private String familyName;

    private int maxFamilySize = 4; // 가족 최대 인원 수, 기본값 4

    private int currentFamilySize;

    private String inviteCode;

    @Column(columnDefinition = "bigint default 0")
    private Long challengeFamilyCount;

    private Long familyOwnerId;

    @OneToMany(mappedBy = "family")
    @JsonManagedReference // 순환 참조 방지, 부모 엔티티
    private List<Member> members;

    @OneToMany(mappedBy = "family")
    private List<Lucky> luckies;
}