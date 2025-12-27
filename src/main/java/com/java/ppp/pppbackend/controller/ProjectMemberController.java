package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.ProjectMemberDTO;
import com.java.ppp.pppbackend.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projectmember/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    @GetMapping
    public ResponseEntity<List<ProjectMemberDTO>> getMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(memberService.getMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectMemberDTO> addMember(
            @PathVariable UUID projectId,
            @RequestBody ProjectMemberDTO dto) {
        dto.setProjectId(projectId);
        return ResponseEntity.ok(memberService.addMember(dto));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID memberId) {
        memberService.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }
}