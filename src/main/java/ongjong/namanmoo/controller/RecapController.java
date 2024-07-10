package ongjong.namanmoo.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.dto.lucky.LuckyListDto;
import ongjong.namanmoo.dto.lucky.LuckyStatusDto;
import ongjong.namanmoo.response.ApiResponse;
import ongjong.namanmoo.service.LuckyService;
import ongjong.namanmoo.service.LuckyServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recap")
@RequiredArgsConstructor
public class RecapController {

    private final LuckyService luckyService;

    @GetMapping("/list")
    public ResponseEntity<?> getLuckyList() {
        List<LuckyListDto> luckyListStatus = luckyService.getLuckyListStatus();
        return ResponseEntity.ok().body(new ApiResponse<>("200", "Success", luckyListStatus));
    }


}
