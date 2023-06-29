package com.ll.chat.domain.notification.entity;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.global.baseEntity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@RequiredArgsConstructor
@Entity
@SuperBuilder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    private Member invitedMember; // 초대를 받은 사람

    @ManyToOne(fetch = LAZY)
    private Member invitingMember; // 초대를 보낸 사람

    private String matchingName;
    private LocalDateTime readDate;

    public boolean isRead() {
        return readDate != null;
    }

    public void markAsRead() {
        readDate = LocalDateTime.now();
    }

    public boolean isHot() {
        // 만들어진지 60분이 안되었다면 hot 으로 설정
        return getCreateDate().isAfter(LocalDateTime.now().minusMinutes(60));
    }
}
