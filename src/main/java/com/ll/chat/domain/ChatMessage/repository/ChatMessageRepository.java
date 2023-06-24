package com.ll.chat.domain.ChatMessage.repository;

import com.ll.chat.domain.ChatMessage.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {


    List<ChatMessage> findByChatRoomId(Long roomId);
}
