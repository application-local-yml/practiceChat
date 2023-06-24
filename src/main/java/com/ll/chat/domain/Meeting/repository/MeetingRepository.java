package com.ll.chat.domain.Meeting.repository;

import com.ll.chat.domain.Meeting.entity.Meeting;
import com.ll.chat.domain.Member.entitiy.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByMember(Member member);
    Page<Meeting> findAll(Pageable pageable);

    Page<Meeting> findBySubjectContainingIgnoreCase(String value, Pageable pageable);
}
