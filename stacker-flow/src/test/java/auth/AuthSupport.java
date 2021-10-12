package auth;

public interface AuthSupport {
    AuthQuestion createAuthQuestion();

    AuthAnswer getAuthAnswer();

    void setAuthAnswer(AuthAnswer answer);
}
