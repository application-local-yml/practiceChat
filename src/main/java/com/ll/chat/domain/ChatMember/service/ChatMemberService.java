package com.ll.chat.domain.ChatMember.service;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatMember.repository.ChatMemberRepository;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.ChatRoom.repository.ChatRoomRepository;
import com.ll.chat.domain.Member.entitiy.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMemberService {

    private final ChatMemberRepository chatMemberRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatMember findById(Long chatRoomUserId) {
        return chatMemberRepository.findById(chatRoomUserId).orElseThrow();
    }

    public List<ChatMember> findByChatRoomId(Long chatRoomId) {
        return chatMemberRepository.findByChatRoomId(chatRoomId);
    }

    public List<ChatMember> findByChatRoomIdAndChatMember(Long roomId, Long memberId) {
        ChatRoom chatRoom = findByRoomId(roomId);
        ChatMember chatMember = findChatMemberByMemberId(chatRoom, memberId);

        if (chatMember == null) {
            return null;
        }

        return chatMemberRepository.findByChatRoomId(roomId);
    }

    private ChatMember findChatMemberByMemberId(ChatRoom chatRoom, Long memberId) {
        return chatRoom.getChatMembers().stream()
                .filter(chatMember -> chatMember.getMember().getId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    public ChatRoom findByRoomId(Long roomId) {

        return chatRoomRepository.findById(roomId).orElseThrow();
    }

    // OwnerID 비교
    public List<ChatMember> findByChatRoomOwnerIdAndChatMember(Long roomId, Long memberId) {
        ChatRoom chatRoom = findByRoomId(roomId);
        Long ownerId = chatRoom.getOwner().getId();
        log.info("ownerId = {} ", ownerId);


        if (ownerId != memberId){
            return null;
        }

        return chatMemberRepository.findByChatRoomId(roomId);
    }

    public ChatMember findByChatRoomAndMember(ChatRoom chatRoom, Member member) {
        return chatMemberRepository.findByChatRoomAndMember(chatRoom, member);
    }
}
