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
        challenges.add(createChallenge(1, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(2, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(3, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(4, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(5, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(6, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(7, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(8, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(9, "자녀가 가장 자랑스러운 순간은?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(9, "부모님이 가장 멋있었던 적이 있다면?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(10, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(11, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(12, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(13, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(14, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(15, "도전! 한 소절!/우리 지금 만나, 당장 만나/우리 지금 만나-리쌍", ChallengeType.VOICE1));
        challenges.add(createChallenge(15, "도전! 한 소절!/너무나 많이 사랑한 죄/사랑앓이-FT아일랜드", ChallengeType.VOICE2));
        challenges.add(createChallenge(15, "도전! 한 소절!/행복하자 우리 행복하자 아프지 말고/양화대교-자이언티", ChallengeType.VOICE3));
        challenges.add(createChallenge(15, "도전! 한 소절!/난 너를 사랑해 이 세상은 너뿐이야/붉은 노을-빅뱅", ChallengeType.VOICE4));
        challenges.add(createChallenge(16, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(17, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(18, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(19, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(20, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(21, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(22, "가족에게 가장 서운했던 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(23, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(24, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(25, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(26, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그분의 최고 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(27, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(28, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(28, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(29, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(30, "인생네컷 챌린지!", ChallengeType.FACETIME));
        // 한 챌린지 주기 = 30일
        challenges.add(createChallenge(31, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(32, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(33, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(34, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(35, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(36, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(37, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(38, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(39, "자녀가 가장 자랑스러운 순간은?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(39, "부모님이 가장 멋있었던 적이 있다면?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(40, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(41, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(42, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(43, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(44, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(45, "도전! 한 소절!/우리 지금 만나, 당장 만나/우리 지금 만나-리쌍", ChallengeType.VOICE1));
        challenges.add(createChallenge(45, "도전! 한 소절!/너무나 많이 사랑한 죄/사랑앓이-FT아일랜드", ChallengeType.VOICE2));
        challenges.add(createChallenge(45, "도전! 한 소절!/행복하자 우리 행복하자 아프지 말고/양화대교-자이언티", ChallengeType.VOICE3));
        challenges.add(createChallenge(45, "도전! 한 소절!/난 너를 사랑해 이 세상은 너뿐이야/붉은 노을-빅뱅", ChallengeType.VOICE4));
        challenges.add(createChallenge(46, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(47, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(48, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(49, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(50, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(51, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(52, "가족에게 가장 서운했던 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(53, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(54, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(55, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(56, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그분의 최고 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(57, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(58, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(58, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(59, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(60, "인생네컷 챌린지!", ChallengeType.FACETIME));
        // 한 챌린지 주기 = 30일
        challenges.add(createChallenge(61, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(62, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(63, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(64, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(65, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(66, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(67, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(68, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(69, "자녀가 가장 자랑스러운 순간은?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(69, "부모님이 가장 멋있었던 적이 있다면?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(70, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(71, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(72, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(73, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(74, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(75, "도전! 한 소절!/우리 지금 만나, 당장 만나/우리 지금 만나-리쌍", ChallengeType.VOICE1));
        challenges.add(createChallenge(75, "도전! 한 소절!/너무나 많이 사랑한 죄/사랑앓이-FT아일랜드", ChallengeType.VOICE2));
        challenges.add(createChallenge(75, "도전! 한 소절!/행복하자 우리 행복하자 아프지 말고/양화대교-자이언티", ChallengeType.VOICE3));
        challenges.add(createChallenge(75, "도전! 한 소절!/난 너를 사랑해 이 세상은 너뿐이야/붉은 노을-빅뱅", ChallengeType.VOICE4));
        challenges.add(createChallenge(76, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(77, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(78, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(79, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(80, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(81, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(82, "가족에게 가장 서운했던 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(83, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(84, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(85, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(86, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그분의 최고 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(87, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(88, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(88, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(89, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(90, "인생네컷 챌린지!", ChallengeType.FACETIME));
        // 한 챌린지 주기 = 30
        challenges.add(createChallenge(91, "자신의 어릴 적 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(92, "오늘 하루 중 가장 즐거웠던 순간은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(93, "내가 가장 좋아하는 음식과 싫어하는 음식은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(94, "시간 여행을 한다면 언제로 돌아가고 싶은가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(95, "함께 먹고 싶은 음식 사진을 올려보세요. 이번 주에 먹었던 것 중 가장 맛있었던 것도 좋아요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(96, "가장 좋아하는 노래가 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(97, "가족 중 한 명이 갑자기 바퀴벌레 혹은 좀비가 된다면 당신의 반응은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(98, "여행을 간다면 어디로 가고 싶나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(99, "자녀가 가장 자랑스러운 순간은?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(99, "부모님이 가장 멋있었던 적이 있다면?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(100, "가장 마음에 드는 본인 사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(101, "가족이 가장 듬직했던 순간이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(102, "요즘 고민이 있다면 무엇일까요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(103, "나의 어렸을 때 장래희망은 ◯◯였다.", ChallengeType.NORMAL));
        challenges.add(createChallenge(104, "가족과 함께한 휴가나 여행 사진을 공유해 주세요.", ChallengeType.PHOTO));
        challenges.add(createChallenge(105, "도전! 한 소절!/우리 지금 만나, 당장 만나/우리 지금 만나-리쌍", ChallengeType.VOICE1));
        challenges.add(createChallenge(105, "도전! 한 소절!/너무나 많이 사랑한 죄/사랑앓이-FT아일랜드", ChallengeType.VOICE2));
        challenges.add(createChallenge(105, "도전! 한 소절!/행복하자 우리 행복하자 아프지 말고/양화대교-자이언티", ChallengeType.VOICE3));
        challenges.add(createChallenge(105, "도전! 한 소절!/난 너를 사랑해 이 세상은 너뿐이야/붉은 노을-빅뱅", ChallengeType.VOICE4));
        challenges.add(createChallenge(106, "어렸을 때 가장 좋아했던 놀이 또는 게임은 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(107, "가족과 가장 닮은 점, 혹은 닮고 싶은 점이 있나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(108, "어릴 적 가장 기억에 남는 생일 선물은 무엇이었나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(109, "살면서 가장 아팠던 날은 언제였나요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(110, "가족에게 한 가장 기억에 남는 거짓말은 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(111, "가장 좋아하는 가족사진을 올려보세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(112, "가족에게 가장 서운했던 순간은?", ChallengeType.NORMAL));
        challenges.add(createChallenge(113, "가장 기억에 남는 가족 여행지와 그 이유는 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(114, "가장 좋아하는 계절은 무엇인가요? 그 계절을 좋아하는 이유는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(115, "학생 때 졸업 사진을 올려주세요", ChallengeType.PHOTO));
        challenges.add(createChallenge(116, "가족 중에서 가장 요리를 잘 하는 사람은 누구인가요? 그분의 최고 요리는 무엇인가요?", ChallengeType.NORMAL));
        challenges.add(createChallenge(117, "가족에게 고마운 점이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(118, "첫 만남은 어디에서 또는 어떻게 이루어졌나요?", ChallengeType.GROUP_PARENT));
        challenges.add(createChallenge(118, "부모님이 가장 잘 어울린다고 느껴진 순간이 있나요?", ChallengeType.GROUP_CHILD));
        challenges.add(createChallenge(119, "가족에게 진심으로 미안하지만 사과하지 못한 일이 있다면 무엇입니까?", ChallengeType.NORMAL));
        challenges.add(createChallenge(120, "인생네컷 챌린지!", ChallengeType.FACETIME));

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

