package com.studyolle.account;

import com.studyolle.domain.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("회원가입 화면 보이는지 테스트")
    @Test
    public void singUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원가입 처리 - 입력값 오류")
    @Test
    public void signSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "junwoo")
                .param("email", "junwoo")
                .param("password", "sdfsdfsfsf")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원가입 처리 - 입력값 정상")
    @Test
    public void signSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "junwoo")
                .param("email", "junwoo@naver.com")
                .param("password", "123456778")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("junwoo"));

        Account account = accountRepository.findByEmail("junwoo@naver.com");

        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        assertNotNull(account.getEmailCheckToken());
//        assertThat(accountRepository.existsByEmail("junwoo@naver.com"));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "sdfsdfsfs")
                .param("email", "junwoo@naver.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {
        Account account = Account.builder()
                .email("junwoo1027@naver.com")
                .nickname("junwoo")
                .password("12345678")
                .build();

        Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", "junwoo1027@naver.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("junwoo"));
    }
}