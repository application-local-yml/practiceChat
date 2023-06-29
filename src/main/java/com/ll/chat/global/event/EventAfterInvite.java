package com.ll.chat.global.event;

import com.ll.chat.domain.Member.entitiy.Member;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EventAfterInvite extends ApplicationEvent {
    private final Member member;

    public EventAfterInvite(Object source, Member member) {
        super(source);
        this.member = member;

    }
}