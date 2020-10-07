package com.studyolle.modules.event.event;

import com.studyolle.modules.event.Enrollment;

public class EnrollmentAcceptedEvent extends EnrollmentEvent{

    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 수락했습니다");
    }
}
