package ongjong.namanmoo.controller.family;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.family.FamilyDto;
import ongjong.namanmoo.response.family.FamilyCreateResponse;
import ongjong.namanmoo.service.FamilyService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/family")
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 가족 생성
     */
    // TODO: 현재 Member role에 입력받은 role을 넣어줘야 한다.
    @PostMapping("/create")
    public String createFamily(@RequestParam String familyName,
                               @RequestParam int maxFamilySize,
                               @RequestParam Long familyOwnerId,
                               Model model) {
        Family family = familyService.createFamily(familyName, maxFamilySize, familyOwnerId);
        model.addAttribute("family", family);
        return new FamilyCreateResponse("200", "Family created successfully", family.getInviteCode()).toString();
    }

    /**
     * 모든 가족 조회
     */
//    @GetMapping
//    public String showAllFamilies(Model model) {
//        model.addAttribute("families", familyService.findAll());
//        return "families";
//    }

    /**
     * 초대 코드 처리
     */
    @GetMapping("/family")
    public FamilyCreateResponse handleInvite(@RequestParam("code") String code, Model model) {
        Optional<Family> family = familyService.findFamilyByInviteCode(code);
        if (family.isPresent()) {
            model.addAttribute("family", family.get());
            return new FamilyCreateResponse("200", "Family created successfully", new FamilyDto(family.get()));
        } else {
            return null;
        }
    }



    /**
     * 가족 참여
     */
    @PostMapping("/join")
    public String joinFamily(@RequestParam("familyId") Long familyId, @RequestParam("memberId") Long memberId) {
        familyService.addMemberToFamily(familyId, memberId);
        return "redirect:/family/" + familyId;
    }


    /**
     * 가족 이름으로 가족 조회
     */
    @GetMapping("/member/{memberId}")
    public String getMemberFamily(@PathVariable Long memberId, Model model) {
        Optional<Member> member = familyService.findMemberById(memberId);
        if (member.isPresent()) {
            Family family = member.get().getFamily();
            model.addAttribute("family", family);
            return "family-detail";
        } else {
            return "member-not-found";
        }
    }
}