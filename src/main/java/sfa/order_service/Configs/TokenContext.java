package sfa.order_service.Configs;

public class TokenContext {
    private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();
    public static void setToken(String token) {
        tokenThreadLocal.set(token);
    }
    public static String getToken() {
        return tokenThreadLocal.get();
    }
    public static void clear() {
        tokenThreadLocal.remove();
    }
}
