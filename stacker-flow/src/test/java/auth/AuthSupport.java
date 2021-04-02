package auth;

public interface AuthSupport {
    AuthQuestion createAuthQuestion();

    void setAuthAnswer(AuthAnswer answer);

    AuthAnswer getAuthAnswer();
}
