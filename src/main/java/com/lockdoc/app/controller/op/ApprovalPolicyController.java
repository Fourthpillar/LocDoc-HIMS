package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.ApprovalPolicyRequest;
import com.lockdoc.app.dto.op.ApprovalPolicyResponse;
import com.lockdoc.app.service.op.ApprovalPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/op/approval-policies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('APPROVAL_POLICY_MANAGE')")
public class ApprovalPolicyController {

    private final ApprovalPolicyService policyService;

    @GetMapping
    public ResponseEntity<List<ApprovalPolicyResponse>> listMine() {
        return ResponseEntity.ok(policyService.listMine());
    }

    @PutMapping
    public ResponseEntity<ApprovalPolicyResponse> upsert(@Valid @RequestBody ApprovalPolicyRequest request) {
        return ResponseEntity.ok(policyService.upsert(request));
    }
}
