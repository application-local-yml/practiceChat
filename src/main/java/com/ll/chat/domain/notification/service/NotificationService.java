package com.ll.chat.domain.notification.service;

import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.domain.notification.entity.Notification;
import com.ll.chat.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findByInvitedMember(Member member) {
        return notificationRepository.findByInvitedMemberOrderByIdDesc(member);
    }

    @Transactional
    public void markAsRead(Member invitedMember) {
        List<Notification> notifications = notificationRepository.findByInvitedMemberOrderByIdDesc(invitedMember);
        notifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach(Notification::markAsRead);
    }
}