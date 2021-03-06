package com.studyolle.modules.study;

import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.account.AccountFactory;
import com.studyolle.modules.account.WithAccount;
import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class StudyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;

//    @AfterEach
//    void afterEach() {
//        accountRepository.deleteAll();
//    }

    @Test
    @WithAccount("junwoo1027")
    @DisplayName("스터디 개설 폼 조회")
    void creatStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithAccount("junwoo1027")
    @DisplayName("스터디 개설 - 완료")
    void createStudy() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short")
                .param("fullDescription", "full")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study byPath = studyRepository.findByPath("test-path");
        assertNotNull(byPath);
        Account account = accountRepository.findByNickname("junwoo1027");
        assertTrue(byPath.getManagers().contains(account));
    }

    @Test
    @WithAccount("junwoo1027")
    @DisplayName("스터디 개설 - 실패")
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "wrong path")
                .param("title", "study title")
                .param("shortDescription", "short")
                .param("fullDescription", "full")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));

        Study byPath = studyRepository.findByPath("wrong path");
        assertNull(byPath);
    }

    @Test
    @WithAccount("junwoo1027")
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("title");
        study.setShortDescription("short");
        study.setFullDescription("full");

        Account account = accountRepository.findByNickname("junwoo1027");
        studyService.createNewStudy(study ,account);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @DisplayName("스터디 가입")
    @WithAccount("junwoo")
    void joinStudy() throws Exception {
        Account junwoo1027 = accountFactory.createAccount("junwoo1027");
        Study study = studyFactory.createStudy("test-study", junwoo1027);

        mockMvc.perform(get("/study/" + study.getPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        Account account = accountRepository.findByNickname("junwoo");
        assertTrue(study.getMembers().contains(account));
    }

    @Test
    @DisplayName("스터디 탈퇴")
    @WithAccount("junwoo")
    void leaveStudy() throws Exception {
        Account junwoo1027 = accountFactory.createAccount("junwoo1027");
        Study study = studyFactory.createStudy("test-study", junwoo1027);

        Account account = accountRepository.findByNickname("junwoo");
        studyService.addMember(study, account);

        mockMvc.perform(get("/study/" + study.getPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

        assertFalse(study.getMembers().contains(account));
    }
}