package com.ajouchong.service;

import com.ajouchong.entity.Member;
import com.ajouchong.entity.enumClass.MemberRole;
import com.ajouchong.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public Member updateMemberRole(Long id, MemberRole newRole) {
        return memberRepository.findById(id)
                .map(member -> {
                    member.setRole(newRole);
                    return memberRepository.save(member);
                })
                .orElseThrow(() -> new EntityNotFoundException("id를 찾을 수 없습니다.: " + id));
    }
}

