package com.example.nfe.api.controller;

import com.example.nfe.api.dto.NfeEmissionRequest;
import com.example.nfe.api.dto.NfeEmissionResponse;
import com.example.nfe.application.service.NfeEmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nfe")
public class NfeController {

    private final NfeEmissionService nfeEmissionService;

    public NfeController(NfeEmissionService nfeEmissionService) {
        this.nfeEmissionService = nfeEmissionService;
    }

    @PostMapping
    public ResponseEntity<NfeEmissionResponse> issueNfe(@Valid @RequestBody NfeEmissionRequest request) {
        NfeEmissionResponse response = nfeEmissionService.issue(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
