package kz.iitu.springlab.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String isbn = value.replaceAll("[-\\s]", "").toUpperCase();
        return switch (isbn.length()) {
            case 10 -> isValidIsbn10(isbn);
            case 13 -> isValidIsbn13(isbn);
            default -> false;
        };
    }

    private boolean isValidIsbn10(String isbn) {
        if (!isbn.substring(0, 9).chars().allMatch(Character::isDigit)) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * Character.digit(isbn.charAt(i), 10);
        }
        char last = isbn.charAt(9);
        int check = last == 'X' ? 10 : Character.digit(last, 10);
        return check >= 0 && (sum + check) % 11 == 0;
    }

    private boolean isValidIsbn13(String isbn) {
        if (!isbn.chars().allMatch(Character::isDigit)) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(isbn.charAt(i), 10);
            sum += digit * (i % 2 == 0 ? 1 : 3);
        }
        int expected = (10 - sum % 10) % 10;
        return expected == Character.digit(isbn.charAt(12), 10);
    }
}
