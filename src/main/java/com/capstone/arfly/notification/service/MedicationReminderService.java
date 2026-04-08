package com.capstone.arfly.notification.service;

import com.capstone.arfly.common.exception.MedicationReminderNotFoundException;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.notification.domain.MedicationReminder;
import com.capstone.arfly.notification.dto.CreateMedicationReminderRequest;
import com.capstone.arfly.notification.dto.GetMedicationRemindersResponse;
import com.capstone.arfly.notification.dto.UpdateMedicationReminderRequest;
import com.capstone.arfly.notification.repository.MedicationReminderRepository;
import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicationReminderService {
    private final MedicationReminderRepository medicationReminderRepository;
    private final MemberRepository memberRepository;

    public void createReminder(@Valid CreateMedicationReminderRequest reminderRequest, Long userId) {
        Member member = memberRepository.getReferenceById(userId);
        MedicationReminder newReminder = MedicationReminder.create(reminderRequest, member);
        medicationReminderRepository.save(newReminder);
    }

    public List<GetMedicationRemindersResponse> getReminderList(Long userId) {
        List<MedicationReminder> reminders = medicationReminderRepository.findByMemberIdOrderByReminderTimeAsc(
                userId);

        List<GetMedicationRemindersResponse> reminderList = reminders.stream()
                .map(reminder -> {
                    return GetMedicationRemindersResponse.builder()
                            .id(reminder.getId())
                            .title(reminder.getTitle()).memo(reminder.getContent())
                            .reminderTime(reminder.getReminderTime()).active(reminder.getActive())
                            .createdAt(reminder.getCreatedAt()).build();
                }).toList();
        return reminderList;
    }

    public void updateMedicationReminder(Long alarmId, @Valid UpdateMedicationReminderRequest request,
                                         Long userId) {
        //유저의 알람이 맞는지 확인
        MedicationReminder reminder = medicationReminderRepository.findByIdAndMemberId(alarmId,userId)
                .orElseThrow(MedicationReminderNotFoundException::new);

        if(request.getTitle() != null){
            reminder.updateTitle(request.getTitle());
        }
        if(request.getMemo() != null){
            reminder.updateContent(request.getMemo());
        }
        if(request.getReminderTime() !=null){
            reminder.updateReminderTime(request.getReminderTime());
        }
    }
}
