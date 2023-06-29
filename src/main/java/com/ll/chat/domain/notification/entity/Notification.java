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

@Getter
@RequiredArgsConstructor
@Entity
@SuperBuilder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member invitedMember; // 초대를 받은 사람

    @ManyToOne(fetch = FetchType.LAZY)
    private Member invitingMember; // 초대를 보낸 사람

    private String matchingName;
    private LocalDateTime readDate;

}
