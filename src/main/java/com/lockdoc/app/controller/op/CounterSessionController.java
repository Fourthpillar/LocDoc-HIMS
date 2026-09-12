package com.lockdoc.app.controller.op;

import com.lockdoc.app.dto.op.CounterSessionCloseRequest;
import com.lockdoc.app.dto.op.CounterSessionResponse;
import com.lockdoc.app.service.op.CounterSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Counter session open/close & reconciliation (Master Spec §7.5/§11.5, screens #12/#31) - shared entity, ?counterType=OP|PHARMACY picks the domain. */
@RestController
@RequestMapping("/counter-sessions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('COUNTER_SESSION_MANAGE')")
public class CounterSessionController {

    private final CounterSessionService counterSessionService;

    @PostMapping
    public ResponseEntity<CounterSessionResponse> open(@RequestParam String counterType) {
        return ResponseEntity.ok(counterSessionService.open(counterType));
    }

    @GetMapping("/current")
    public ResponseEntity<CounterSessionResponse> current(@RequestParam String counterType) {
        return ResponseEntity.ok(counterSessionService.current(counterType));
    }

    @GetMapping
    public ResponseEntity<List<CounterSessionResponse>> history(@RequestParam String counterType) {
        return ResponseEntity.ok(counterSessionService.history(counterType));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CounterSessionResponse> close(@PathVariable Long id, @Valid @RequestBody CounterSessionCloseRequest request) {
        return ResponseEntity.ok(counterSessionService.close(id, request));
    }
}
