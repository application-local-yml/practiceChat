package com.ll.chat.domain.ChatMember.entity;

import com.ll.chat.domain.ChatMessage.entity.ChatMessage;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.global.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@EqualsAndHashCode(of = {"member", "chatRoom"})
public class ChatMember extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Member member;

    @ManyToOne(fetch = LAZY)
    private ChatRoom chatRoom;

    @Builder.Default
    @Enumerated(STRING)
    private ChatMemberType type = ENTER;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Builder
    public ChatMember(Member member, ChatRoom chatRoom) {
        this.member = member;
        this.chatRoom = chatRoom;
    }

    public void changeType() {
        this.type = KICKED;
    }

    public void exitType() {
        this.type = LEAVE;
    }
}