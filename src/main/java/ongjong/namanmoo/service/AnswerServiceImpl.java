package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.repository.AnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {
    private final AnswerRepository answerRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean checkUserResponse(Member member, Timestamp createDate) {
        return answerRepository.existsByMemberAndCreateDateAndAnswerIsNotNull(member, createDate);
    }


}
