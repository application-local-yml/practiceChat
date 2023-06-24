package com.ll.chat.domain.ChatMember.entity;

import com.ll.chat.domain.ChatMessage.entity.ChatMessage;
import com.ll.chat.domain.ChatMessage.entity.ChatMessageType;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.global.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.ll.chat.domain.ChatMember.entity.ChatMemberType.*;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@Setter
@EqualsAndHashCode(of = {"member", "chatRoom"})
public class ChatMember extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private ChatRoom chatRoom;

    @Builder.Default
    @Enumerated(STRING)
    private ChatMemberType type = ROOMIN;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Builder
    public ChatMember(Member member, ChatRoom chatRoom, ChatMemberType type) {
        this.member = member;
        this.chatRoom = chatRoom;
        this.type = type;
    }

    public void changeType() {
        this.type = KICKED;
    }

    public void exitType() {
        this.type = EXIT;
    }

    public void changeMemberCommonType() {
        this.type = COMMON;
    }
}