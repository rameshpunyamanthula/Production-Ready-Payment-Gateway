package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class TestController {

    private final MerchantRepository merchantRepository;

    public TestController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @GetMapping("/api/v1/test/merchant")
    public ResponseEntity<?> getTestMerchant() {

        UUID testId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Optional<Merchant> merchantOpt = merchantRepository.findById(testId);

        if (merchantOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Test merchant not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Merchant m = merchantOpt.get();

        Map<String, Object> res = new HashMap<>();
        res.put("id", m.getId());
        res.put("email", m.getEmail());
        res.put("api_key", m.getApiKey());
        res.put("seeded", true);

        return ResponseEntity.ok(res);
    }
}
