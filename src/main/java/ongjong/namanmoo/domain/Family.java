package ongjong.namanmoo.domain;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Getter @Setter
@Entity
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;        // familyId를 Id로 변경

    private String familyName;

    private int maxFamilySize = 4; // 가족 최대 인원 수, 기본값 4

    private String inviteCode;

    private Long familyOwnerId;

    @OneToMany(mappedBy = "family")
    @JsonManagedReference // 순환 참조 방지, 부모 엔티티
    private List<Member> members;

    @OneToMany(mappedBy = "family")
    private List<Lucky> luckies;
}