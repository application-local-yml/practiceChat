package com.ll.chat.domain.notification.repository;

import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByInvitedMemberOrderByIdDesc(Member member);

}