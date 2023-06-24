package com.ll.chat.domain.ChatMember.repository;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    List<ChatMember> findByChatRoomId(Long chatRoomId);
}
