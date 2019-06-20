package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import system.dao.CodeVerificationDao;
import system.dao.CodeVerificationCode;
import system.dao.CodeVerificationResult;
import system.model.CodeVerification;
import system.model.exception.CodeVerificationException;
import system.model.exception.UserException;

import java.util.LinkedList;
import java.util.List;

@Service
public class CodeVerificationService {

    @Autowired
    CodeVerificationDao dao;

    public CodeVerification create(CodeVerification user) {
        try {
            return dao.create(user);
        } catch (CodeVerificationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(CodeVerification verification) {
        try {
            dao.delete(verification);
        } catch (CodeVerificationException e) {
            e.printStackTrace();
        }
    }

    public void update(CodeVerification verification) {
        try {
            dao.update(verification);
        } catch (CodeVerificationException e) {
            e.printStackTrace();
        }
    }

    public List<CodeVerification> list() {
        try {
            return dao.list();
        } catch (CodeVerificationException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    public CodeVerificationResult isCodeTheSameAsTheLastCodeSentToUser(long userId, String smsCode) {
        try {
            return dao.isCodeTheSameAsTheLastCodeSentToUser(userId, smsCode);
        } catch (UserException e) {
            e.printStackTrace();
        }
        return null;
    }
}
