package com.post_it.blog.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestService {

    private final JavaMailSender javaMailSender;
    // 인증 코드 생성 (6자리)
    public String createCode() {
        Random random = new Random();
        int num = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(num);
    }

    // 인증 메일 보내기
    @Async
    public void sendVerifyMail(String email, String code) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/verifyCode.html");
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 인증코드 삽입
            // 인증을 보낼 메일을 html로 보낼거임
            html = html.replace("{{code}}", code);

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, StandardCharsets.UTF_8.name()
            );

            helper.setTo(email);
            helper.setSubject("테스트 인증코드");
            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("인증 메일 전송 실패: " + e.getMessage());
        }
    }
}
