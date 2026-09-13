package com.lockdoc.doctor.controller;

import com.lockdoc.doctor.dto.FreeReviewPolicyRequest;
import com.lockdoc.doctor.dto.FreeReviewPolicyResponse;
import com.lockdoc.doctor.service.FreeReviewPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Free-review policy (Master Spec §7.2) - Hospital/Clinic Admin, own facility only. */
@RestController
@RequestMapping("/facility/free-review-policies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('FREE_REVIEW_POLICY_MANAGE')")
public class FreeReviewPolicyController {

    private final FreeReviewPolicyService policyService;

    @GetMapping
    public ResponseEntity<List<FreeReviewPolicyResponse>> list() {
        return ResponseEntity.ok(policyService.listForFacility());
    }

    @PutMapping("/{doctorId}")
    public ResponseEntity<FreeReviewPolicyResponse> upsert(@PathVariable Long doctorId, @Valid @RequestBody FreeReviewPolicyRequest request) {
        return ResponseEntity.ok(policyService.upsert(doctorId, request));
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Void> delete(@PathVariable Long doctorId) {
        policyService.delete(doctorId);
        return ResponseEntity.noContent().build();
    }
}
