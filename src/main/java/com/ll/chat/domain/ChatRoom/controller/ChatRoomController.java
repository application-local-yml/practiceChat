package com.ll.chat.domain.ChatRoom.controller;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatMember.service.ChatMemberService;
import com.ll.chat.domain.ChatMessage.dto.response.SignalResponse;
import com.ll.chat.domain.ChatMessage.service.ChatMessageService;
import com.ll.chat.domain.ChatRoom.dto.ChatRoomDto;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.ChatRoom.service.ChatRoomService;
import com.ll.chat.domain.Meeting.entity.Meeting;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.domain.Member.service.MemberService;
import com.ll.chat.global.rq.Rq;
import com.ll.chat.global.rsData.RsData;
import com.ll.chat.global.security.SecurityMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ll.chat.domain.ChatMember.entity.ChatMemberType.*;
import static com.ll.chat.domain.ChatMessage.dto.response.SignalType.NEW_MESSAGE;
import static com.ll.chat.domain.ChatMessage.entity.ChatMessageType.ENTER;
import static com.ll.chat.domain.ChatMessage.entity.ChatMessageType.LEAVE;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/usr/chat")
public class ChatRoomController {

    private final Rq rq;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations template;
    private final ChatMemberService chatMemberService;
    private final MemberService memberService;

    /**
     * 방 조회
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/rooms")
    public String showRooms(Model model) {

        List<ChatRoom> chatRooms = chatRoomService.findAll();

        model.addAttribute("chatRooms", chatRooms);
        return "usr/chat/rooms";
    }

    /**
     * 방 입장
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/rooms/{roomId}")
    public String showRoom(@PathVariable Long roomId, Model model, @AuthenticationPrincipal SecurityMember member) {

        log.info("showRoom SecurityMember = {}", member);
        log.info("member.getUsername = {}", member.getUsername());
        log.info("member.getAuthorities = {}", member.getAuthorities());
        log.info("member.getId = {}", member.getId());

        ChatRoom chatRoom = chatRoomService.findById(roomId);
        Meeting meeting = chatRoom.getMeeting();

        RsData rsData = chatRoomService.canAddChatRoomMember(chatRoom, member.getId(), meeting);

        if (rsData.isFail()) return rq.historyBack(rsData);

        ChatRoomDto chatRoomDto = chatRoomService.getByIdAndUserId(roomId, member.getId());

        if (chatRoomDto.getType().equals(ROOMIN) || chatRoomDto.getType().equals(EXIT)){
            // 사용자가 방에 입장할 때 메시지 생성
            String enterMessage = " < " + member.getUsername() + "님이 입장하셨습니다. >";
            chatMessageService.createAndSave(enterMessage, member.getId(), roomId, ENTER);

            // 실시간으로 입장 메시지 전송
            SignalResponse signalResponse = SignalResponse.builder()
                    .type(NEW_MESSAGE)
                    .message(enterMessage)  // 입장 메시지 설정
                    .build();

            template.convertAndSend("/topic/chats/" + chatRoom.getId(), signalResponse);
        }

        chatRoomService.updateChatUserType(roomId, member.getId());
        chatRoomService.changeParticipant(chatRoom);

        model.addAttribute("chatRoom", chatRoomDto);
        model.addAttribute("member", member);


        return "usr/chat/room";
    }

    /**
     * 채팅방 삭제(Owner만 가능)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/rooms/{roomId}")
    public String removeRoom(@PathVariable Long roomId, @AuthenticationPrincipal SecurityMember member) {
        chatRoomService.remove(roomId, member.getId());
        return "redirect:/usr/meeting/list";
    }

    /**
     * Member가 채팅방 나가기
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/rooms/{roomId}")
    public String exitChatRoom(@PathVariable Long roomId, @AuthenticationPrincipal SecurityMember member){
        ChatRoom chatRoom = chatRoomService.findById(roomId);

        ChatRoomDto chatRoomDto = chatRoomService.getByIdAndUserId(roomId, member.getId());

        if (chatRoomDto.getType().equals(COMMON)){
            // 사용자가 방에서 퇴장할 때 메시지 생성
            String exitMessage = " < " + member.getUsername() + "님이 퇴장하셨습니다. >";
            chatMessageService.createAndSave(exitMessage, member.getId(), roomId, LEAVE);

            // 실시간으로 퇴장 메시지 전송
            SignalResponse signalResponseLeave = SignalResponse.builder()
                    .type(NEW_MESSAGE)
                    .message(exitMessage)  // 퇴장 메시지 설정
                    .build();

            template.convertAndSend("/topic/chats/" + chatRoom.getId(), signalResponseLeave);
        }

        chatRoomService.exitChatRoom(roomId, member.getId());
        chatRoomService.changeParticipant(chatRoom);

        return "redirect:/usr/meeting/list";
    }

    // 방장이 유저 강퇴시키기
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{roomId}/kick/{memberId}")
    public String kickChatMember(@PathVariable Long roomId, @PathVariable Long memberId,
                                 @AuthenticationPrincipal SecurityMember member){
        ChatRoom chatRoom = chatRoomService.findById(roomId);
        chatRoomService.kickChatMember(roomId, memberId, member);

        Long chatRoomId = chatMemberService.findById(memberId).getChatRoom().getId();
        chatRoomService.changeParticipant(chatRoom);

        return ("redirect:/usr/meeting/detail/%d").formatted(chatRoomId);
    }

    // 유저 정보 가져오기
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{roomId}/memberList")
    public String memberList(Model model, @PathVariable Long roomId,
                             @AuthenticationPrincipal SecurityMember member) {
        List<Member> memberList = memberService.findAll();
        List<ChatMember> chatMemberList = chatMemberService.findByChatRoomIdAndChatMember(roomId, member.getId());
        ChatRoom chatRoom = chatRoomService.findById(roomId);

        if (chatMemberList == null) {
            return rq.historyBack("해당 방에 참가하지 않았습니다.");
        }

        model.addAttribute("chatMemberList", chatMemberList);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("memberList", memberList);
        model.addAttribute("COMMON", COMMON);
        return "usr/chat/memberList";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inviteList")
    public String inviteList(Model model) {
        List<Member> memberList = memberService.findAll();
        log.info("memberList = {}", memberList);
        model.addAttribute("memberList", memberList);

        return "usr/chat/inviteList";
    }

}
