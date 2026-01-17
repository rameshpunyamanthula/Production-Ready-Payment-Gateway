package com.gateway.services;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    /* =========================
       AUTHENTICATE MERCHANT
       ========================= */
    public Merchant authenticate(String apiKey, String apiSecret) {

        if (apiKey == null || apiSecret == null) {
            throw new RuntimeException("AUTHENTICATION_ERROR");
        }

        Merchant merchant = merchantRepository
                .findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("AUTHENTICATION_ERROR"));

        if (!merchant.getApiSecret().equals(apiSecret)) {
            throw new RuntimeException("AUTHENTICATION_ERROR");
        }

        if (!merchant.isActive()) {
            throw new RuntimeException("AUTHENTICATION_ERROR");
        }

        return merchant;
    }

    /* =========================
       TEST ENDPOINT SUPPORT
       ========================= */
    public Merchant getTestMerchant() {
        return merchantRepository
                .findByEmail("test@example.com")
                .orElse(null);
    }
}
