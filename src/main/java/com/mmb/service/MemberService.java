package com.mmb.service;

import com.mmb.dto.ResultData;
import com.mmb.entity.Member;
import com.mmb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
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
            return new ResultData<>("F-1", "議댁옱?섏? ?딅뒗 ID?낅땲??");
        }

        Member member = memberOpt.get();
        if (member.isDeleted()) {
            LocalDateTime now = LocalDateTime.now();
            if (member.getRestoreUntil() != null && member.getRestoreUntil().isAfter(now)) {
                String restoreUntilStr = member.getRestoreUntil().format(DateTimeFormatter.ofPattern("yyyy??MM??dd??HH??mm遺?));
                Map<String, Object> data = new HashMap<>();
                data.put("restoreUntil", restoreUntilStr);
                return new ResultData<>("D-1", "?덊눜??怨꾩젙?낅땲?? " + restoreUntilStr + "源뚯? 蹂듦뎄?????덉뒿?덈떎.", data);
            } else {
                return new ResultData<>("F-1", "怨꾩젙??李얠쓣 ???놁뒿?덈떎.");
            }
        }

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            return new ResultData<>("F-2", "鍮꾨?踰덊샇媛 ??몄뒿?덈떎.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", member.getId());
        data.put("authLevel", member.getAuthLevel());
        data.put("username", member.getUsername());

        return new ResultData<>("S-1", "濡쒓렇??媛?ν빀?덈떎.", data);
    }

    public ResultData<Map<String, Object>> findLoginIdByNameAndEmail(String realName, String email) {
        Optional<Member> memberOpt = memberRepository.findByRealNameAndEmail(realName, email);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-1", "No user matches that name/email.");
        }
        Member member = memberOpt.get();
        boolean sent = sendLoginIdEmail(member.getEmail(), member.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("emailSent", sent);
        return new ResultData<>("S-1",
                sent ? "아이디를 이메일로 보냈습니다."
                        : "이메일 발송에 실패했습니다.",
                data);
    }

    public ResultData<Map<String, Object>> resetPasswordWithEmail(String username, String email) {
        Optional<Member> memberOpt = memberRepository.findByUsernameAndEmail(username, email);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-1", "입력한 정보와 일치하는 사용자가 없습니다.");
        }

        String tempPw = createTempPassword(10);
        Member member = memberOpt.get();
        member.setPassword(passwordEncoder.encode(tempPw));
        memberRepository.save(member);

        boolean sent = sendTempPasswordEmail(member.getEmail(), tempPw);
        Map<String, Object> data = new HashMap<>();
        data.put("emailSent", sent);

        return new ResultData<>("S-1",
                sent ? "임시 비밀번호를 이메일로 보냈습니다."
                        : "이메일 발송에 실패했습니다.",
                data);
    }

    @Transactional
    public ResultData<Map<String, Object>> updateNickname(String username, String newNickname) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }

        Member member = memberOpt.get();

        if (member.getNickname().equals(newNickname)) {
            return new ResultData<>("F-1", "湲곗〈 ?됰꽕?꾧낵 ?숈씪?⑸땲??");
        }

        if (memberRepository.existsByNickname(newNickname)) {
            return new ResultData<>("F-2", "以묐났???됰꽕?꾩엯?덈떎.");
        }

        LocalDateTime lastChanged = member.getNicknameUpdatedAt();
        if (lastChanged != null && lastChanged.plusDays(30).isAfter(LocalDateTime.now())) {
            LocalDateTime nextAvailable = lastChanged.plusDays(30);
            String nextDate = nextAvailable.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return new ResultData<>("F-3", "?됰꽕?꾩? " + nextDate + " ?댄썑??蹂寃쏀븷 ???덉뒿?덈떎.");
        }

        member.setNickname(newNickname);
        member.setNicknameUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", newNickname);

        return new ResultData<>("S-1", "?됰꽕?꾩씠 蹂寃쎈릺?덉뒿?덈떎.", data);
    }

    @Transactional
    public ResultData<Map<String, Object>> updatePassword(String username, String newPassword) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }

        Member member = memberOpt.get();
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        return new ResultData<>("S-1", "鍮꾨?踰덊샇媛 蹂寃쎈릺?덉뒿?덈떎.");
    }

    @Transactional
    public ResultData<Map<String, Object>> updateProfile(String username, String newNickname, String email, String region, Integer dailyTarget) {
        Optional<Member> memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "濡쒓렇?몄씠 ?꾩슂?⑸땲??");
        }

        Member member = memberOpt.get();
        boolean nicknameChanged = newNickname != null && !newNickname.equals(member.getNickname());

        if (nicknameChanged) {
            if (memberRepository.existsByNickname(newNickname)) {
                return new ResultData<>("F-2", "以묐났???됰꽕?꾩엯?덈떎.");
            }
            LocalDateTime lastChanged = member.getNicknameUpdatedAt();
            if (lastChanged != null && lastChanged.plusDays(30).isAfter(LocalDateTime.now())) {
                LocalDateTime nextAvailable = lastChanged.plusDays(30);
                String nextDate = nextAvailable.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return new ResultData<>("F-3", "?됰꽕?꾩? " + nextDate + " ?댄썑??蹂寃쏀븷 ???덉뒿?덈떎.");
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

        return new ResultData<>("S-1", "?뺣낫媛 ??λ릺?덉뒿?덈떎.", data);
    }

    @Transactional
    public ResultData<Map<String, Object>> withdraw(String username) {
        Optional<Member> memberOpt = memberRepository.findByUsernameAndDeletedAtIsNull(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "議댁옱?섏? ?딅뒗 ?뚯썝?낅땲??");
        }
        Member member = memberOpt.get();
        LocalDateTime now = LocalDateTime.now();
        member.setDeletedAt(now);
        member.setRestoreUntil(now.plusDays(7));
        memberRepository.save(member);

        Map<String, Object> data = new HashMap<>();
        data.put("restoreUntil", member.getRestoreUntil().format(DateTimeFormatter.ofPattern("yyyy??MM??dd??HH??mm遺?)));
        return new ResultData<>("S-1", "?덊눜 泥섎━?섏뿀?듬땲?? 7?쇨컙 ?곗씠?곕? 蹂닿??⑸땲??", data);
    }

    @Transactional
    public ResultData<Map<String, Object>> restore(String username, String rawPassword) {
        Optional<Member> memberOpt = memberRepository.findByUsernameAndDeletedAtIsNotNull(username);
        if (memberOpt.isEmpty()) {
            return new ResultData<>("F-0", "蹂듦뎄??怨꾩젙??李얠쓣 ???놁뒿?덈떎.");
        }
        Member member = memberOpt.get();
        if (member.getRestoreUntil() == null || member.getRestoreUntil().isBefore(LocalDateTime.now())) {
            return new ResultData<>("F-1", "蹂듦뎄 媛??湲곌컙??吏?ъ뒿?덈떎.");
        }
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            return new ResultData<>("F-2", "鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.");
        }
        member.setDeletedAt(null);
        member.setRestoreUntil(null);
        memberRepository.save(member);

        Map<String, Object> data = new HashMap<>();
        data.put("username", member.getUsername());
        return new ResultData<>("S-1", "怨꾩젙??蹂듦뎄?섏뿀?듬땲??", data);
    }

    private boolean sendLoginIdEmail(String to, String username) {
        String subject = "[MMB] Account username";
        String body = """
                <html>
                  <body>
                    <h3>아이디 안내</h3>
                    <p>가입하신 아이디는 <b>%s</b> 입니다.</p>
                    <p><a href=\"http://localhost:8081/login\" target=\"_blank\">로그인하러 가기</a></p>
                  </body>
                </html>
                """.formatted(username);
        return sendEmail(to, subject, body);
    }

    private boolean sendTempPasswordEmail(String to, String tempPassword) {
        String subject = "[MMB] Temporary password";
        String body = """
                <html>
                  <body>
                    <h3>임시 비밀번호</h3>
                    <p>임시 비밀번호는 <b>%s</b> 입니다.</p>
                    <p>로그인 후 마이페이지에서 비밀번호를 변경해주세요.</p>
                    <p><a href=\"http://localhost:8081/login\" target=\"_blank\">로그인하러 가기</a></p>
                  </body>
                </html>
                """.formatted(tempPassword);
        return sendEmail(to, subject, body);
    }

    private boolean sendEmail(String to, String subject, String htmlBody) {
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




