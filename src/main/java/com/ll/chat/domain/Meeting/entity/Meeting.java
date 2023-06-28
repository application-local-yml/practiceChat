package com.ll.chat.domain.Meeting.entity;

import com.ll.chat.domain.ChatMember.entity.ChatMemberType;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.global.baseEntity.BaseEntity;
import com.ll.chat.global.rsData.RsData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
public class Meeting extends BaseEntity implements Serializable {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @OneToOne(mappedBy = "meeting", fetch = LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private ChatRoom chatRoom;

    private String subject;
    private Long capacity; // 참여 가능 인원
    private Long participantsCount; // 현재 참여자 수
    private String location;
    private String date;
    private String time;
    private String content;

    public RsData update(String subject, Long capacity, String location, String date, String time, String content) {

        this.subject = subject;
        this.capacity = capacity;
        this.location = location;
        this.date = date;
        this.time = time;
        this.content = content;

        return RsData.of("S-1", "성공");
    }

    public void setParticipantsCount(Long count){
        this.participantsCount = count;
    }

    public boolean canAddParticipant() {
        return this.participantsCount < this.capacity;
    }
}
