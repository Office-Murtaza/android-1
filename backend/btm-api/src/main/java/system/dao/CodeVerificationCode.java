package system.dao;

public enum CodeVerificationCode {

    OK(0),
    NOT_MATCH(1),
    EXPIRED(2);

    private int code;
    CodeVerificationCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
