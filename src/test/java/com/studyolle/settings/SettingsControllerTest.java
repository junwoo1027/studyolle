package com.studyolle.settings;

import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("junwoo1027")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }

    @WithAccount("junwoo1027")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        assertEquals(bio, junwoo1027.getBio());
    }

    @WithAccount("junwoo1027")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우 짧은 소개를 수정하는 경우" ;
        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        assertNull(junwoo1027.getBio());
    }

    @WithAccount("junwoo1027")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));

    }

    @WithAccount("junwoo1027")
    @DisplayName("패스워드 수정하기 - 입력값 정상")
    @Test
    void updatePassword() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword", "12341234")
                .param("newPasswordConfirm", "12341234")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        assertTrue(passwordEncoder.matches("12341234", junwoo1027.getPassword()));
    }

    @WithAccount("junwoo1027")
    @DisplayName("패스워드 수정하기 - 입력값 에러 - 페스워드 불일치")
    @Test
    void updatePassword_error() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword", "12341234")
                .param("newPasswordConfirm", "123412345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("junwoo1027")
    @DisplayName("알림 수정 폼")
    @Test
    void updateNotificationsForm() throws Exception {
        mockMvc.perform(get("/settings/notifications"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));

    }

    @WithAccount("junwoo1027")
    @DisplayName("알림 수정하기")
    @Test
    void updateNotifications() throws Exception {
        mockMvc.perform(post("/settings/notifications")
                .param("studyCreatedByEmail", "true")
                .param("studyEnrollmentResultByEmail", "true")
                .param("studyUpdatedByEmail", "true")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notifications"))
                .andExpect(flash().attributeExists("message"));

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        assertEquals(true, junwoo1027.isStudyCreatedByEmail());
        assertEquals(true, junwoo1027.isStudyEnrollmentResultByEmail());
        assertEquals(true, junwoo1027.isStudyUpdatedByEmail());
//        assertEquals(true, junwoo1027.isStudyCreatedByWeb());
//        assertEquals(true, junwoo1027.isStudyEnrollmentResultByWeb());
//        assertEquals(true, junwoo1027.isStudyUpdatedByWeb());
    }

    @WithAccount("junwoo1027")
    @DisplayName("닉네임 수정 폼")
    @Test
    void updateAccountForm() throws Exception {
        mockMvc.perform(get("/settings/account"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

    }

    @WithAccount("junwoo1027")
    @DisplayName("닉네 수정하기 - 입력값 정상")
    @Test
    void updateAccount() throws Exception {
        mockMvc.perform(post("/settings/account")
                .param("nickname", "junwoo")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/account"))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("junwoo"));
    }

    @WithAccount("junwoo1027")
    @DisplayName("닉네임 수정하기 - 입력값 에러")
    @Test
    void updateAccount_error() throws Exception {
        mockMvc.perform(post("/settings/account")
                .param("nickname", "123**")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }
}