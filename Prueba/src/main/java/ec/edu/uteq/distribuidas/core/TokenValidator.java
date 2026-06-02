package ec.edu.uteq.distribuidas.core;

public class TokenValidator {

    private static final String VALID_TOKEN = "TOKEN123";

    public static boolean validate(String token) {
        return VALID_TOKEN.equals(token);
    }
}