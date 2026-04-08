package com.capstone.arfly.common.exception;

import lombok.Getter;

@Getter
public class MedicationReminderNotFoundException extends BusinessException {
    public MedicationReminderNotFoundException() {
        super(ErrorCode.MEDICATION_REMINDER_NOT_FOUND);
    }
}
