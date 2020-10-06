package com.studyolle.modules.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.modules.account.WithAccount;
import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.AccountService;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import com.studyolle.modules.tag.TagForm;
import com.studyolle.modules.account.form.ZoneForm;
import com.studyolle.modules.tag.TagRepository;
import com.studyolle.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }
    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
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

    @WithAccount("junwoo1027")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("junwoo1027")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        assertTrue(junwoo1027.getTags().contains(newTag));
    }

    @WithAccount("junwoo1027")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        accountService.addTag(junwoo1027, newTag);

        assertTrue(junwoo1027.getTags().contains(newTag));

        mockMvc.perform(post("/settings/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(junwoo1027.getTags().contains(newTag));
    }

    @WithAccount("junwoo1027")
    @DisplayName("지역 수정 폼")
    @Test
    void updateZoneForm() throws Exception {
        mockMvc.perform(get("/settings/zones"))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/zones"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("junwoo1027")
    @DisplayName("계정에 지역 추가")
    @Test
    void addZones() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/settings/zones/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(junwoo1027.getZones().contains(zone));
    }

    @WithAccount("junwoo1027")
    @DisplayName("계정에 지역 삭제")
    @Test
    void removeZone() throws Exception {
        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());

        ZoneForm zoneForm =new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post("/settings/zones/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(junwoo1027.getZones().contains(zoneForm));
    }
}