package ongjong.namanmoo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.challenge.Challenge;
import ongjong.namanmoo.domain.challenge.ChallengeType;
import ongjong.namanmoo.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class ChallengeDataInit {

    private final ChallengeRepository challengeRepository;

    @PostConstruct
    public void createChallenges() {
        // Normal 챌린지 질문
        String[] normalChallengeDescriptions = {
                "부모님에게 연락하기",
                "가족에게 가장 미안했던 순간은?",
                "가족과 함께 저녁 식사하기",
                "가족과 함께 사진 찍기",
                "가족과 함께 산책하기"
        };

        // Group Parent 챌린지 질문
        String[] groupParentChallengeDescriptions = {
                "자식들에게 미안했던 일",
                "자식들에게 서운했던 일"
        };

        // Group Child 챌린지 질문
        String[] groupChildChallengeDescriptions = {
                "부모님에게 미안했던 일",
                "부모님이에게 서운했던 일"
        };


        // FaceTime 챌린지 질문
        String[] facetimeChallengeDescriptions = {
                "부모님과 화상채팅하기"
        };

        // Photo 챌린지 질문
        String[] photoChallengeDescriptions = {
                "가족 사진 찍기"
        };

        // Voice 챌린지 질문
        String[] voiceChallengeDescriptions = {
                "음성 메시지 보내기"
        };

        List<Challenge> challenges = new ArrayList<>();     // 챌린지가 담길 리스트

        long nextChallengeNum = 0;      // 챌린지 번호

        // Normal 챌린지 생성
        for (String normalChallengeDescription : normalChallengeDescriptions) {
            nextChallengeNum++;
            Challenge challenge = new Challenge();
            challenge.setChallengeNum(nextChallengeNum);
            challenge.setChallengeTitle(normalChallengeDescription);
            challenge.setChallengeType(ChallengeType.NORMAL);
            challenges.add(challenge);
        }

        // Group Child 챌린지 생성
        for (int i = 0; i < groupChildChallengeDescriptions.length; i++) {

            nextChallengeNum++;
            // Group Parent 챌린지 생성
            Challenge parentChallenge = new Challenge();
            parentChallenge.setChallengeNum(nextChallengeNum);
            parentChallenge.setChallengeTitle(groupParentChallengeDescriptions[i]);
            parentChallenge.setChallengeType(ChallengeType.GROUP_PARENT);
            challenges.add(parentChallenge);

            // Group Child 챌린지 생성
            Challenge childeChallenge = new Challenge();
            childeChallenge.setChallengeNum(nextChallengeNum);
            childeChallenge.setChallengeTitle(groupChildChallengeDescriptions[i]);
            childeChallenge.setChallengeType(ChallengeType.GROUP_CHILD);
            challenges.add(childeChallenge);
        }

        // FaceTime 챌린지 생성
        for (String facetimeChallengeDescription : facetimeChallengeDescriptions) {
            nextChallengeNum++;
            Challenge challenge = new Challenge();
            challenge.setChallengeNum(nextChallengeNum);
            challenge.setChallengeTitle(facetimeChallengeDescription);
            challenge.setChallengeType(ChallengeType.FACETIME);
            challenges.add(challenge);
        }

        // Photo 챌린지 생성
        for (String photoChallengeDescription : photoChallengeDescriptions) {
            nextChallengeNum++;
            Challenge challenge = new Challenge();
            challenge.setChallengeNum(nextChallengeNum);
            challenge.setChallengeTitle(photoChallengeDescription);
            challenge.setChallengeType(ChallengeType.PHOTO);
            challenges.add(challenge);
        }

        // Voice 챌린지 생성
        for (String voiceChallengeDescription : voiceChallengeDescriptions) {
            nextChallengeNum++;
            Challenge challenge = new Challenge();
            challenge.setChallengeNum(nextChallengeNum);
            challenge.setChallengeTitle(voiceChallengeDescription);
            challenge.setChallengeType(ChallengeType.VOICE);
            challenges.add(challenge);
        }

        // 챌린지 리스트를 섞음
//        Collections.shuffle(challenges);

        // 챌린지를 저장
        challengeRepository.saveAll(challenges);
    }
}
