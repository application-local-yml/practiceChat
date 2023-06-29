package com.ll.chat.domain.notification.controller;

import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.domain.Member.service.MemberService;
import com.ll.chat.domain.notification.entity.Notification;
import com.ll.chat.domain.notification.service.NotificationService;
import com.ll.chat.global.rq.Rq;
import com.ll.chat.global.security.SecurityMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/usr/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final MemberService memberService;

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public String showList(Model model, @AuthenticationPrincipal SecurityMember member) {
        Long currentMemberId = member.getId();
        Member currentMember = memberService.findById(currentMemberId);

        List<Notification> notifications = notificationService.findByInvitedMember(currentMember);

//        notificationService.markAsRead(invitedMember);
        notificationService.markAsRead(currentMember);

        model.addAttribute("notifications", notifications);

        return "usr/notification/list";
    }
}
