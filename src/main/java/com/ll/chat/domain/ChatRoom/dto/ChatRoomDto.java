package com.ll.chat.domain.ChatRoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatMember.entity.ChatMemberType;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.Member.dto.MemberDto;
import com.ll.chat.domain.Member.entitiy.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ChatRoomDto {

    @JsonProperty("chat_room_id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("owner")
    private MemberDto owner;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // 추가: 사용자 상태를 나타내는 필드
    @JsonProperty("type")
    private ChatMemberType type;

    public static ChatRoomDto fromChatRoom(ChatRoom chatRoom, Member member) {
        MemberDto userDto = MemberDto.fromUser(chatRoom.getOwner());

        log.info("member = {} ", member);

        ChatMember chatMember = chatRoom.getChatMembers().stream()
                .filter(cm -> cm.getMember().getId().equals(member.getId()))
                .findFirst()
                .orElse(null);

        log.info("chatMemberId = {}", chatMember.getId());
        log.info("memberId = {}", member.getId());

        ChatMemberType nowType = chatMember.getType();

        log.info("nowType = {}", nowType);

        ChatRoomDto chatRoomDto = ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .owner(userDto)
                .createdAt(chatRoom.getCreateDate())
                .updatedAt(chatRoom.getModifyDate())
                .type(nowType)
                .build();

        return chatRoomDto;
    }
}
