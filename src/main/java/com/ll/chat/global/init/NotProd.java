package com.ll.chat.global.init;

import com.ll.chat.domain.ChatMember.entity.ChatMember;
import com.ll.chat.domain.ChatRoom.entity.ChatRoom;
import com.ll.chat.domain.ChatRoom.service.ChatRoomService;
import com.ll.chat.domain.Meeting.entity.Meeting;
import com.ll.chat.domain.Meeting.service.MeetingService;
import com.ll.chat.domain.Member.entitiy.Member;
import com.ll.chat.domain.Member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile({"local"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService,
            ChatRoomService chatRoomService,
            MeetingService meetingService

    ) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                Member memberUser1 = memberService.join("user1", "1234").getData();
                Member memberUser2 = memberService.join("user2", "1234").getData();
                Member memberUser3 = memberService.join("user3", "1234").getData();

                Meeting meeting1 = meetingService.create("오늘 한강에서 러닝하실 분 구합니다!!",
                        memberUser1, 8L, "용산구", "2023-07-08", "14:00",
                        "한강에서 2시간 정도 같이 달리실 분 구합니다!");
                Meeting meeting2 = meetingService.create("이번주 토요일 바이크 타실 분 구합니다 :)",
                        memberUser2,3L, "잠실", "2023-06-28", "10:00",
                        "오전에 같이 운동해요!");

                ChatRoom chatRoom1 = chatRoomService.createAndConnect(meeting1.getSubject(), meeting1, memberUser1.getId());
                ChatRoom chatRoom2 = chatRoomService.createAndConnect(meeting2.getSubject(), meeting2, memberUser2.getId());


            }
        };
    }
}