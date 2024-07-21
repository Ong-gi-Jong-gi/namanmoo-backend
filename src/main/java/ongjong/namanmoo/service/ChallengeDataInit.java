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
        List<Challenge> challenges = new ArrayList<>(); // 챌린지가 담길 리스트

        // 챌린지 생성 및 추가
        challenges.add(createChallenge(1, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(2, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(3, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(4, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(5, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(6, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(7, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(8, "자녀의 성장 과정 중에서 가장 기억에 남는 순간이 있나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(8, "부모님에게 가장 자랑하고 싶었던 일이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(9, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(10, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(11, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(12, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(13, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(14, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(15, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(16, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(17, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(18, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(19, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(20, "가족에게 가장 속상했던(서운했던) 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(21, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(22, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(23, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(24, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그 사람이 만든 요리 중 가장 좋아하는 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(25, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(26, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(26, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(27, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(28, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(29, "명대사를 말해라1!", ChallengeType.VOICE1));
        challenges.add(createChallenge(29, "명대사를 말해라2!", ChallengeType.VOICE2));
        challenges.add(createChallenge(29, "명대사를 말해라3!", ChallengeType.VOICE3));
        challenges.add(createChallenge(29, "명대사를 말해라4!", ChallengeType.VOICE4));
        challenges.add(createChallenge(30, "인생네컷 챌린지!", ChallengeType.FACETIME));
        // 한 챌린지 주기 = 30일
        challenges.add(createChallenge(31, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(32, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(33, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(34, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(35, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(36, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(37, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(38, "자녀의 성장 과정 중에서 가장 기억에 남는 순간이 있나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(38, "부모님에게 가장 자랑하고 싶었던 일이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(39, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(40, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(41, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(42, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(43, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(44, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(45, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(46, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(47, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(48, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(49, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(50, "가족에게 가장 속상했던(서운했던) 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(51, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(52, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(53, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(54, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그 사람이 만든 요리 중 가장 좋아하는 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(55, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(56, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(56, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(57, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(58, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(59, "명대사를 말해라1!", ChallengeType.VOICE1));
        challenges.add(createChallenge(59, "명대사를 말해라2!", ChallengeType.VOICE2));
        challenges.add(createChallenge(59, "명대사를 말해라3!", ChallengeType.VOICE3));
        challenges.add(createChallenge(59, "명대사를 말해라4!", ChallengeType.VOICE4));
        challenges.add(createChallenge(60, "인생네컷 챌린지!", ChallengeType.FACETIME));
        // 한 챌린지 주기 = 30일
        challenges.add(createChallenge(61, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(62, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(63, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(64, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(65, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(66, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(67, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(68, "자녀의 성장 과정 중에서 가장 기억에 남는 순간이 있나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(68, "부모님에게 가장 자랑하고 싶었던 일이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(69, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(70, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(71, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(72, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(73, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(74, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(75, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(76, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(77, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(78, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(79, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(80, "가족에게 가장 속상했던(서운했던) 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(81, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(82, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(83, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(84, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그 사람이 만든 요리 중 가장 좋아하는 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(85, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(86, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(86, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(87, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(88, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(89, "명대사를 말해라1!", ChallengeType.VOICE1));
        challenges.add(createChallenge(89, "명대사를 말해라2!", ChallengeType.VOICE2));
        challenges.add(createChallenge(89, "명대사를 말해라3!", ChallengeType.VOICE3));
        challenges.add(createChallenge(89, "명대사를 말해라4!", ChallengeType.VOICE4));
        challenges.add(createChallenge(90, "인생네컷 챌린지!", ChallengeType.FACETIME));


        // 챌린지를 저장
        if (challengeRepository.count() == 0) {
            challengeRepository.saveAll(challenges);
        }
    }

    private Challenge createChallenge(int challengeNum, String challengeTitle, ChallengeType challengeType) {
        Challenge challenge = new Challenge();
        challenge.setChallengeNum(challengeNum);
        challenge.setChallengeTitle(challengeTitle);
        challenge.setChallengeType(challengeType);
        return challenge;
    }
}

