package account.utils;

import java.util.Set;

public class PasswordBreachHandler {

    private final static Set<String> breachedPasswords = Set.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    private static final String HIBP_API_URL = "https://api.pwnedpasswords.com/range/";

    public static boolean isPasswordBreached(String password) {
        return breachedPasswords.contains(password);
    }
}
