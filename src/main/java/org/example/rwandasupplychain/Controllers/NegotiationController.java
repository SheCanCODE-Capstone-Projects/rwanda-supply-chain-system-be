package org.example.rwandasupplychain.Controllers;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.NegotiationDtos.NegotiationRequest;
import org.example.rwandasupplychain.DTOs.NegotiationDtos.NegotiationResponse;
import org.example.rwandasupplychain.Services.NegotiationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/negotiations")
public class NegotiationController {

    private final NegotiationService negotiationService;

    public NegotiationController(NegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }

    @PostMapping
    public ResponseEntity<NegotiationResponse> addMessage(@Valid @RequestBody NegotiationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(negotiationService.addMessage(request));
    }

    @GetMapping("/quotation/{quotationId}")
    public List<NegotiationResponse> getThread(@PathVariable UUID quotationId) {
        return negotiationService.getThread(quotationId);
    }
}
