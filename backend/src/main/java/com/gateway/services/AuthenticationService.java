package com.gateway.services;

import org.springframework.stereotype.Service;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final MerchantRepository merchantRepository;

    public AuthenticationService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public Merchant authenticate(String apiKey, String apiSecret) {

        if (apiKey == null || apiSecret == null) {
            return null;
        }

        Optional<Merchant> merchantOpt = merchantRepository.findByApiKey(apiKey);

        if (merchantOpt.isEmpty()) {
            return null;
        }

        Merchant merchant = merchantOpt.get();

        if (!merchant.getApiSecret().equals(apiSecret)) {
            return null;
        }

        if (!merchant.isActive()) {
            return null;
        }

        return merchant;
    }
}
