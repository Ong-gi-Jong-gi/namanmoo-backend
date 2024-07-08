package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Member;

import java.sql.Timestamp;

public interface AnswerService {

    boolean checkUserResponse(Member member, Timestamp createDate);
}
