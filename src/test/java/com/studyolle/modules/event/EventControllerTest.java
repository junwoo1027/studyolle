package com.studyolle.modules.event;

import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.account.AccountFactory;
import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.WithAccount;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.study.Study;
import com.studyolle.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("junwoo1027")
    void newEnrollment_FCFS_event_accepted() throws Exception {
        Account account = accountFactory.createAccount("junwoo");
        Study study = studyFactory.createStudy("test-study", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, account);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        isAccepted(event, junwoo1027);

    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithAccount("junwoo1027")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account account = accountFactory.createAccount("junwoo");
        Study study = studyFactory.createStudy("test-study", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, account);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, june);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        isNotAccepted(event, junwoo1027);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("junwoo1027")
    void accepted_account_cancelEnrolment_to_FCFS_event_not_accepted() throws Exception {
        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        Account junwoo = accountFactory.createAccount("junwoo");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", junwoo);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, junwoo);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, junwoo1027);
        eventService.newEnrollment(event, junwoo);

        isAccepted(event, may);
        isAccepted(event, junwoo1027);
        isNotAccepted(event, junwoo);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(event, may);
        isAccepted(event, junwoo);
        assertNull(enrollmentRepository.findByEventAndAccount(event, junwoo1027));
    }

    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("junwoo1027")
    void not_accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account junwoo1027 = accountRepository.findByNickname("junwoo1027");
        Account junwoo = accountFactory.createAccount("junwoo");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", junwoo);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, junwoo);

        isAccepted(event, may);
        isAccepted(event, junwoo);
        isNotAccepted(event, junwoo1027);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(event, may);
        isAccepted(event, junwoo);
        assertNull(enrollmentRepository.findByEventAndAccount(event, junwoo1027));
    }

    private void isAccepted(Event event, Account junwoo1027) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, junwoo1027).isAccepted());
    }

    private void isNotAccepted(Event event, Account junwoo1027) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, junwoo1027).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusHours(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1));
        event.setEndDateTime(LocalDateTime.now().plusDays(2));
        return eventService.createEvent(event, study, account);
    }
}