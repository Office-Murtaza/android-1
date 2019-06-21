package system.dao;

import system.model.CodeVerification;

public class CodeVerificationResult {

    private CodeVerificationCode code;
    private CodeVerification latestVerification;

    public CodeVerificationResult() {
    }

    public CodeVerificationResult(CodeVerificationCode code, CodeVerification latestVerification) {
        this.code = code;
        this.latestVerification = latestVerification;
    }

    public CodeVerificationCode getCode() {
        return code;
    }

    public void setCode(CodeVerificationCode code) {
        this.code = code;
    }

    public CodeVerification getLatestVerification() {
        return latestVerification;
    }

    public void setLatestVerification(CodeVerification latestVerification) {
        this.latestVerification = latestVerification;
    }
}
