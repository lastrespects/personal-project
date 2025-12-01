package com.mmb.service;

import com.mmb.entity.Member;
import com.mmb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isUsernameTaken(String username) {
        return memberRepository.existsByUsername(username);
    }

    public boolean isNicknameTaken(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // Member 엔티티를 직접 받는 join
    @Transactional
    public void join(Member member) {
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        memberRepository.save(member);
    }

    // 기존 컨트롤러에서 쓰는 편의 메서드
    @Transactional
    public void join(String username, String password, String name,
                     String nickname, int age, String region, int dailyTarget) {

        Member member = Member.builder()
                .username(username)
                .password(password)
                .realName(name)
                .nickname(nickname)
                .age(age)
                .region(region)
                .dailyTarget(dailyTarget)
                .build();

        join(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    // FullLearningService에서 쓰는 용도
    public Member getMemberById(int id) {
        return memberRepository.findById((long) id).orElse(null);
    }
}
