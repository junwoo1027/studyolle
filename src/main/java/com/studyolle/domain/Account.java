package com.studyolle.domain;

import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdateByEmail;

    private boolean studyUpdateByWeb;

    //token 값 생성
    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    //회원가입 완료
    public void completeSignUp() {
        this.emailVerified = true; //이메일 인증 true 변경
        this.joinedAt = LocalDateTime.now(); //가입날짜 현재 날짜로
    }

    //token 값이 맞는지 확인
    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }
}
