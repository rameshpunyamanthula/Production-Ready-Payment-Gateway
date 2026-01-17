package com.gateway.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final Pattern VPA_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    private static final String ALPHA_NUM =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    // ===============================
    // ORDER / PAYMENT ID GENERATOR
    // ===============================
    public String randomAlphaNumeric(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();
    java.util.Random random = new java.util.Random();

    for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
}

    // ===============================
    // VPA VALIDATION
    // ===============================
    public boolean isValidVPA(String vpa) {
        if (vpa == null) return false;
        return VPA_PATTERN.matcher(vpa).matches();
    }

    // ===============================
    // CARD NUMBER (LUHN)
    // ===============================
    public boolean isValidCard(String cardNumber) {

        if (cardNumber == null) return false;

        String digits = cardNumber.replaceAll("[^0-9]", "");

        if (digits.length() < 13 || digits.length() > 19) return false;

        int sum = 0;
        boolean alternate = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';

            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }

            sum += n;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    // ===============================
    // CARD NETWORK DETECTION
    // ===============================
    public String detectCardNetwork(String cardNumber) {

        if (cardNumber == null) return "unknown";

        String digits = cardNumber.replaceAll("[^0-9]", "");

        if (digits.startsWith("4")) return "visa";
        if (digits.matches("^5[1-5].*")) return "mastercard";
        if (digits.startsWith("34") || digits.startsWith("37")) return "amex";
        if (digits.startsWith("60") || digits.startsWith("65")
                || digits.matches("^8[1-9].*")) return "rupay";

        return "unknown";
    }

    // ===============================
    // EXPIRY DATE VALIDATION
    // ===============================
    public boolean isValidExpiry(String month, String year) {
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);

            if (m < 1 || m > 12) return false;

            if (year.length() == 2) {
                y = 2000 + y;
            }

            YearMonth expiry = YearMonth.of(y, m);
            YearMonth now = YearMonth.now();

            return !expiry.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }
    

}
