package com.ll.chat.domain.ChatMember.repository;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.Member.entitiy.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    List<ChatMember> findByChatRoomId(Long chatRoomId);

    ChatMember findByChatRoomAndMember(ChatRoom chatRoom, Member member);
}
