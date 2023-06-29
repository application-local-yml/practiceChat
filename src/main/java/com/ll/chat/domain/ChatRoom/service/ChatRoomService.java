package com.ll.chat.domain.ChatRoom.service;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatMember.entity.ChatMemberType;
import com.ll.chat.domain.ChatMember.service.ChatMemberService;
import com.ll.chat.domain.ChatMessage.entity.ChatMessage;
import com.ll.chat.domain.ChatRoom.dto.ChatRoomDto;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.ChatRoom.repository.ChatRoomRepository;
import com.ll.chat.domain.Meeting.entity.Meeting;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.domain.Member.service.MemberService;
import com.ll.chat.domain.notification.entity.Notification;
import com.ll.chat.domain.notification.repository.NotificationRepository;
import com.ll.chat.global.event.EventAfterInvite;
import com.ll.chat.global.rsData.RsData;
import com.ll.chat.global.security.SecurityMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ll.chat.domain.ChatMember.entity.ChatMemberType.KICKED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberService memberService;
    private final ChatMemberService chatMemberService;
    private final SimpMessageSendingOperations template;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher publisher;


    @Transactional
    public ChatRoom createAndConnect(String subject, Meeting meeting, Long ownerId) {
        Member owner = memberService.findByIdElseThrow(ownerId);
        ChatRoom chatRoom = ChatRoom.create(subject, meeting, owner);

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        log.info("savedChatRoom = {} ", savedChatRoom);
        log.info("Owner = {} ", owner);
        log.info("meeting = {}", meeting);

        savedChatRoom.addChatUser(owner);

        return savedChatRoom;
    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow();
    }

    @Transactional
    //@Cacheable(value = "chatroom", key = "#roomId + '_' + #memberId")
    public ChatRoomDto getByIdAndUserId(Long roomId, long memberId) {
        Member member = memberService.findByIdElseThrow(memberId);

        ChatRoom chatRoom = findById(roomId);

        addChatRoomMember(chatRoom, member, memberId);

        chatRoom.getChatMembers().stream()
                .filter(chatMember -> chatMember.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow();

        return ChatRoomDto.fromChatRoom(chatRoom, member);
    }

    private Optional<ChatMember> getChatUser(ChatRoom chatRoom, Member member, Long memberId) {
        // 방에 해당 유저가 있으면 가져오기
        Optional<ChatMember> existingMember = chatRoom.getChatMembers().stream()
                .filter(chatMember -> chatMember.getMember().getId().equals(memberId))
                .findFirst();

        log.info("memberId = {}", memberId);
        log.info("member.getId = {}", member.getId());

        return existingMember;
    }

    private void addChatRoomMember(ChatRoom chatRoom, Member member, Long memberId) {

        if (getChatUser(chatRoom, member, memberId).isEmpty()) {
            chatRoom.addChatUser(member);
        }
    }

    // 참여자 추가 가능한지 확인하는 메서드
    public RsData canAddChatRoomMember(ChatRoom chatRoom, Long memberId, Meeting meeting) {

        Member member = memberService.findByIdElseThrow(memberId);

        // 이미 채팅방에 동일 유저가 존재하는 경우
        if (!getChatUser(chatRoom, member, memberId).isEmpty()) {
            ChatMember chatMember = getChatUser(chatRoom, member, memberId).get();
            return checkChatMemberType(chatMember);
        }

        if (!meeting.canAddParticipant()) return RsData.of("F-2", "모임 정원 초과!");

        return RsData.of("S-1", "새로운 모임 채팅방에 참여합니다.");
    }

    public RsData checkChatMemberType(ChatMember chatMember) {
        if (chatMember.getType().equals(KICKED)) return RsData.of("F-1", "강퇴당한 모임입니다!");

        return RsData.of("S-1", "기존 모임 채팅방에 참여합니다.");
    }

    /**
     * 채팅방 삭제
     */
    @Transactional
    // @CacheEvict(value = "chatroom", allEntries=true)
    public void remove(Long roomId, Long OwnerId) {
        Member owner = memberService.findByIdElseThrow(OwnerId);
        log.info("roomId = {}", roomId);
        log.info("OnwerId = {}", owner.getId());

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if(!chatRoom.getOwner().equals(owner)) {
            throw new IllegalArgumentException("방 삭제 권한이 없습니다.");
        }

        removeChatRoom(chatRoom);
    }

    public void removeChatRoom(ChatRoom chatRoom) {
        chatRoom.getChatMembers().clear();
        chatRoomRepository.delete(chatRoom);
    }


    /**
     * 유저가 방 나가기
     * 현재는 사용안하고 있음!
     */
    @Transactional
    public void exitChatRoom(Long roomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        log.info("memberId = {} ", memberId);

        // 해당 유저의 ChatMember를 제거합니다.
        ChatMember chatMember = findChatMemberByMemberId(chatRoom, memberId);
        log.info("chatMember = {} ", chatMember);

        if (chatMember != null) {
            chatMember.exitType();
        }
    }

    private ChatMember findChatMemberByMemberId(ChatRoom chatRoom, Long memberId) {
        return chatRoom.getChatMembers().stream()
                .filter(chatMember -> chatMember.getMember().getId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void updateChatRoomName(ChatRoom chatRoom, String subject) {
        log.info("update subject = {}", subject);
        chatRoom.updateName(subject);
        chatRoomRepository.save(chatRoom);
    }

    // 유저 강퇴하기
    @Transactional
    public void kickChatMember(Long roomId, Long chatMemberId, @AuthenticationPrincipal SecurityMember member) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        checkOwner(chatRoom, member.getId());

        ChatMember chatMember = chatMemberService.findById(chatMemberId);
        Member kickMember = chatMember.getMember();

        // 강톼당할 MemberId를 받아와야한다.
        Long originMemberId = kickMember.getId();

        chatMember.changeType(); // 강퇴된 유저의 type 을 "KICKED"로 변경

        List<ChatMessage> chatMessages = chatRoom.getChatMessages();

        chatMessages.stream()
                .filter(chatMessage -> chatMessage.getSender().getId().equals(chatMemberId))
                .forEach(chatMessage -> chatMessage.removeChatMessages("강퇴된 사용자의 메시지입니다."));

        template.convertAndSend("/topic/chats/" + roomId + "/kicked", originMemberId);
    }

    public void checkOwner(ChatRoom chatRoom, Long ownerId) {
        Member owner = memberService.findByIdElseThrow(ownerId);

        log.info("roomId = {}", chatRoom.getId());
        log.info("OwnerId = {}", owner.getId());

        if (!chatRoom.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("강퇴 권한이 없습니다.");
        }
    }

    @Transactional
    public void updateChatUserType(Long roomId, Long memberId) {
        ChatRoom chatRoom = chatMemberService.findByRoomId(roomId);
        Member member = memberService.findByIdElseThrow(memberId);

        UserTypeChange(chatRoom, member);
    }

    /**
     * COMMON으로 Type 수정
     */
    public void UserTypeChange(ChatRoom chatRoom, Member member) {
        ChatMember chatMember = chatMemberService.findByChatRoomAndMember(chatRoom, member);

        log.info("chatRoomId = {} ", chatRoom.getId());
        log.info("getMemberId = {} ", member.getId());

        chatMember.changeMemberCommonType();

    }

    public Long getCommonParticipantsCount(ChatRoom chatRoom) {
        return chatRoom.getChatMembers().stream()
                .filter(chatMember -> chatMember.getType().equals(ChatMemberType.COMMON))
                .count();
    }

    @Transactional
    public void changeParticipant(ChatRoom chatRoom) {
        Long commonParticipantsCount = getCommonParticipantsCount(chatRoom);
        log.info("commonParticipantsCount = {} ", commonParticipantsCount);
        chatRoom.getMeeting().setParticipantsCount(commonParticipantsCount);
    }

    @Transactional
    public RsData<Member> inviteMember(Long roomId, SecurityMember member, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        // 현재 로그인된 사용자가 방에 있는지 확인하는 로직
        Long currentMemberId = member.getId();
        Member currentMember = memberService.findById(currentMemberId);
        ChatMember chatMemberByMemberId = findChatMemberByMemberId(chatRoom, currentMemberId);

        // return 값 바꿀 거면 수정하기
        if (chatMemberByMemberId == null){
            return RsData.of("F-1", "현재 당신은 %s 방에 들어있지 않습니다.".formatted(chatRoom.getName()));
        }

        Member invitedMember = memberService.findById(memberId);

        // 알림 저장
        Notification notification = Notification.builder()
                .invitedMember(invitedMember)
                .invitingMember(currentMember)
                .matchingName(chatRoom.getName())
                .build();

        notificationRepository.save(notification);

        // TODO: 멤버 초대 알림 로직 ( eventListener 추가 )

        publisher.publishEvent(new EventAfterInvite(this, invitedMember));


        return RsData.of("S-1", "%s 님에게 초대를 보냈습니다".formatted(invitedMember.getUsername()));


    }
}