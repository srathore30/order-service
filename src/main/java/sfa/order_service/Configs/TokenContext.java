package sfa.order_service.Configs;

public class TokenContext {
    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();

    // Set the token in the current thread's context
    public static void setToken(String token) {
        tokenThreadLocal.set(token);
    }

    // Get the token from the current thread's context
    public static String getToken() {
        return tokenThreadLocal.get();
    }

    // Clear the token when done
    public static void clear() {
        tokenThreadLocal.remove();
    }
}
