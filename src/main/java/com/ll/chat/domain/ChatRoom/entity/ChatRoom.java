package com.ll.chat.domain.ChatRoom.entity;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatMember.entity.ChatMemberType;
import com.ll.chat.domain.ChatMessage.entity.ChatMessage;
import com.ll.chat.domain.Meeting.entity.Meeting;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.global.baseEntity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ll.chat.domain.ChatMember.entity.ChatMemberType.*;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@RequiredArgsConstructor
@Entity
@SuperBuilder
@EqualsAndHashCode(of = {"id"}, callSuper = true)
public class ChatRoom extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    @OneToOne(fetch = LAZY, cascade = PERSIST, orphanRemoval = true)
    private Meeting meeting;

    @ManyToOne(fetch = LAZY)
    private Member owner;

    @OneToMany(mappedBy = "chatRoom", cascade = PERSIST, orphanRemoval = true)
    @Builder.Default
    private Set<ChatMember> chatMembers = new HashSet<>();

    @OneToMany(mappedBy = "chatRoom", cascade = PERSIST, orphanRemoval = true )
    private List<ChatMessage> chatMessages = new ArrayList<>();

    public static ChatRoom create(String name, Meeting meeting, Member owner) {

        Assert.notNull(name, "name는 널일 수 없습니다.");
        Assert.notNull(owner, "owner는 널일 수 없습니다.");

        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .meeting(meeting)
                .owner(owner)
                .build();

        return chatRoom;
    }

    public void addChatUser(Member owner) {
        ChatMember chatMember = ChatMember.builder()
                .member(owner)
                .chatRoom(this)
                .build();

        chatMembers.add(chatMember);
    }

    public void removeChatMember(ChatMember chatMember) {
        chatMembers.remove(chatMember);
    }

    public void updateName(String name){
        this.name = name;
    }
}
