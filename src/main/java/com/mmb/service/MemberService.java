package com.mmb.service;

import com.mmb.dto.ResultData;
import com.mmb.entity.Member;
import com.mmb.repository.MemberRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;
    @Value("${spring.mail.password:}")
    private String mailPassword;

    public boolean isUsernameTaken(String username) {
        return memberRepository.existsByUsername(username);
    }

    public boolean isNicknameTaken(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public void join(Member member) {
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        memberRepository.save(member);
    }

    @Transactional
    public void join(String username, String password, String name, String email,
                     String nickname, int age, String region, int dailyTarget) {

        Member member = Member.builder()
                .username(username)
                .password(password)
                .email(email)
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

    public Member getMemberById(int id) {
        return memberRepository.findById((long) id).orElse(null);
    }

    public ResultData<Map<String, Object>> validateLoginInfo(String username, String rawPassword) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-1", "해당 아이디를 찾을 수 없습니다.");
        }

        Member member = memberOpt.get();
        if (member.isDeleted()) {
            LocalDateTime now = LocalDateTime.now();
            if (member.getRestoreUntil() != null && member.getRestoreUntil().isAfter(now)) {
                String restoreUntilStr = member.getRestoreUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                Map<String, Object> data = new HashMap<>();
                data.put("restoreUntil", restoreUntilStr);
                return new ResultData<>("D-1", "계정이 " + restoreUntilStr + " 까지 삭제 예약되었습니다.", data);
            } else {
                return new ResultData<>("F-1", "이미 삭제된 계정입니다.");
            }
        }

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            return new ResultData<>("F-2", "비밀번호가 올바르지 않습니다.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", member.getId());
        data.put("authLevel", member.getAuthLevel());
        data.put("username", member.getUsername());

        return new ResultData<>("S-1", "로그인에 성공했습니다.", data);
    }

    public ResultData<Map<String, Object>> findLoginIdByNameAndEmail(String realName, String email) {
        Optional<Member> memberOpt = memberRepository.findByRealNameAndEmail(realName, email);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-1", "이름과 이메일이 일치하는 사용자를 찾을 수 없습니다.");
        }
        Member member = memberOpt.get();
        boolean sent = sendLoginIdEmail(member.getEmail(), member.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("emailSent", sent);
        return new ResultData<>("S-1",
                sent ? "아이디 안내 메일을 발송했습니다."
                        : "아이디 안내 메일 발송에 실패했습니다.",
                data);
    }

    @Transactional
    public ResultData<Map<String, Object>> resetPasswordWithEmail(String username, String email) {
        Optional<Member> memberOpt = memberRepository.findByUsernameAndEmail(username, email);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-1", "아이디/이메일이 일치하는 사용자를 찾을 수 없습니다.");
        }

        String tempPw = createTempPassword(10);
        Member member = memberOpt.get();
        member.setPassword(passwordEncoder.encode(tempPw));
        memberRepository.save(member);

        boolean sent = sendTempPasswordEmail(member.getEmail(), tempPw);
        Map<String, Object> data = new HashMap<>();
        data.put("emailSent", sent);

        return new ResultData<>("S-1",
                sent ? "임시 비밀번호 메일을 발송했습니다."
                        : "임시 비밀번호 메일 발송에 실패했습니다.",
                data);
    }

    @Transactional
    public ResultData<Map<String, Object>> updateNickname(String username, String newNickname) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "사용자를 찾을 수 없습니다.");
        }

        Member member = memberOpt.get();

        if (member.getNickname().equals(newNickname)) {
            return new ResultData<>("F-1", "현재 닉네임과 동일합니다.");
        }

        if (memberRepository.existsByNickname(newNickname)) {
            return new ResultData<>("F-2", "이미 사용 중인 닉네임입니다.");
        }

        LocalDateTime lastChanged = member.getNicknameUpdatedAt();
        if (lastChanged != null && lastChanged.plusDays(30).isAfter(LocalDateTime.now())) {
            LocalDateTime nextAvailable = lastChanged.plusDays(30);
            String nextDate = nextAvailable.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return new ResultData<>("F-3", nextDate + " 이후에 닉네임을 변경할 수 있습니다.");
        }

        member.setNickname(newNickname);
        member.setNicknameUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", newNickname);

        return new ResultData<>("S-1", "닉네임을 변경했습니다.", data);
    }

    @Transactional
    public ResultData<Map<String, Object>> updatePassword(String username, String newPassword) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "사용자를 찾을 수 없습니다.");
        }

        Member member = memberOpt.get();
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        return new ResultData<>("S-1", "비밀번호를 변경했습니다.");
    }

    @Transactional
    public ResultData<Map<String, Object>> updateProfile(String username, String newNickname, String email, String region, Integer dailyTarget) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "사용자를 찾을 수 없습니다.");
        }

        Member member = memberOpt.get();
        boolean nicknameChanged = newNickname != null && !newNickname.equals(member.getNickname());

        if (nicknameChanged) {
            if (memberRepository.existsByNickname(newNickname)) {
                return new ResultData<>("F-2", "이미 사용 중인 닉네임입니다.");
            }
            LocalDateTime lastChanged = member.getNicknameUpdatedAt();
            if (lastChanged != null && lastChanged.plusDays(30).isAfter(LocalDateTime.now())) {
                LocalDateTime nextAvailable = lastChanged.plusDays(30);
                String nextDate = nextAvailable.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return new ResultData<>("F-3", nextDate + " 이후에 닉네임을 변경할 수 있습니다.");
            }
            member.setNickname(newNickname);
            member.setNicknameUpdatedAt(LocalDateTime.now());
        }

        if (email != null && !email.isEmpty()) {
            member.setEmail(email);
        }
        member.setRegion(region);
        if (dailyTarget != null && dailyTarget > 0) {
            member.setDailyTarget(dailyTarget);
        }

        memberRepository.save(member);

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", member.getNickname());
        data.put("email", member.getEmail());
        data.put("region", member.getRegion());
        data.put("dailyTarget", member.getDailyTarget());

        return new ResultData<>("S-1", "프로필을 변경했습니다.", data);
    }

    @Transactional
    public ResultData<Map<String, Object>> withdraw(String username) {
        Optional<Member> memberOpt = memberRepository.findByUsernameAndDeletedAtIsNull(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "활성화된 계정을 찾을 수 없습니다.");
        }
        Member member = memberOpt.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime restoreUntil = now.plusDays(7);
        member.setDeletedAt(now);
        member.setRestoreUntil(restoreUntil);
        memberRepository.save(member);

        String restoreUntilStr = restoreUntil.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Map<String, Object> data = new HashMap<>();
        data.put("restoreUntil", restoreUntilStr);
        String msg = restoreUntilStr + "까지 같은 계정으로 로그인하면 복구할 수 있습니다.";
        return new ResultData<>("S-1", msg, data);
    }

    @Transactional
    public ResultData<Map<String, Object>> restore(String username, String rawPassword) {
        Optional<Member> memberOpt = memberRepository.findByUsernameAndDeletedAtIsNotNull(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "삭제된 계정을 찾을 수 없습니다.");
        }
        Member member = memberOpt.get();
        if (member.getRestoreUntil() == null || member.getRestoreUntil().isBefore(LocalDateTime.now())) {
            return new ResultData<>("F-1", "복구 가능한 기간이 지났습니다.");
        }
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            return new ResultData<>("F-2", "비밀번호가 올바르지 않습니다.");
        }
        member.setDeletedAt(null);
        member.setRestoreUntil(null);
        memberRepository.save(member);

        Map<String, Object> data = new HashMap<>();
        data.put("username", member.getUsername());
        return new ResultData<>("S-1", "계정을 복구했습니다.", data);
    }

    private boolean sendLoginIdEmail(String to, String username) {
        String subject = "[MMB] 아이디 안내";
        String body = """
                <html>
                  <body>
                    <h3>아이디 안내</h3>
                    <p>회원님의 아이디는 <b>%s</b> 입니다.</p>
                    <p><a href="http://localhost:8081/login" target="_blank">로그인 하러가기</a></p>
                  </body>
                </html>
                """.formatted(username);
        return sendEmail(to, subject, body);
    }

    private boolean sendTempPasswordEmail(String to, String tempPassword) {
        String subject = "[MMB] 임시 비밀번호 안내";
        String body = """
                <html>
                  <body>
                    <h3>임시 비밀번호 안내</h3>
                    <p>임시 비밀번호는 <b>%s</b> 입니다.</p>
                    <p>로그인 후 즉시 새 비밀번호로 변경해주세요.</p>
                    <p><a href="http://localhost:8081/login" target="_blank">로그인 하러가기</a></p>
                  </body>
                </html>
                """.formatted(tempPassword);
        return sendEmail(to, subject, body);
    }

    private boolean sendEmail(String to, String subject, String htmlBody) {
        // If mail credentials are not provided, skip sending to avoid blocking calls.
        if (mailPassword == null || mailPassword.isBlank()) {
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(mailFrom);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String createTempPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
