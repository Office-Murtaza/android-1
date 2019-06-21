package system.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import system.model.exception.CodeVerificationException;
import system.model.CodeVerification;
import system.model.exception.UserException;

import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CodeVerificationDao extends Dao {

    public static final int TEN_MIN_IN_MS = 600_000;

    public CodeVerification create(CodeVerification codeVerification)
            throws CodeVerificationException {
        try {
            begin();
            getSession().save(codeVerification);
            commit();
            return codeVerification;
        } catch (HibernateException e) {
            rollback();
            throw new CodeVerificationException("Exception while creating codeVerification: " + e.getMessage());
        }
    }

    public void update(CodeVerification verification)
            throws CodeVerificationException {
        try {
            begin();
            getSession().update(verification);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new CodeVerificationException("Exception while updating codeVerification: " + e.getMessage());
        }
    }

    public void delete(CodeVerification codeVerification)
            throws CodeVerificationException {
        try {
            begin();
            getSession().delete(codeVerification);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new CodeVerificationException("Could not delete codeVerification", e);
        }
    }

    public List<CodeVerification> list() throws CodeVerificationException{

    	try {
            begin();
            Query q = getSession().createQuery("from CodeVerification");
            List<CodeVerification> adverts = q.list();
            commit();
            return adverts;
        } catch (HibernateException e) {
            rollback();
            throw new CodeVerificationException("Could not list codeVerifications", e);
        }

    }

    public CodeVerificationResult isCodeTheSameAsTheLastCodeSentToUser(long userId, String smsCode) throws UserException {
        try {
            begin();

            CodeVerificationCode codeVerificationCode = CodeVerificationCode.NOT_MATCH;
            CodeVerification latestSentCode = null;
            try {

                List codeVerificationsAsObjects = getSession()
                        .createQuery("from CodeVerification")
                        .list();
                if(!codeVerificationsAsObjects.isEmpty()) {
                    LinkedList<CodeVerification> codeVerificationsCasted = new LinkedList<>(
                            (List<CodeVerification>) codeVerificationsAsObjects
                    );
                    codeVerificationsCasted = codeVerificationsCasted
                            .stream()
                            .filter(cv -> cv.getUser().getUserId() == userId).collect(Collectors.toCollection(LinkedList::new));
                    codeVerificationsCasted.sort(Comparator.comparing(CodeVerification::getCreateDate));
                    latestSentCode = codeVerificationsCasted.getLast();

                    int latestSentCodeInt = Integer.parseInt(latestSentCode.getCode().trim());
                    int smsCodeInt = Integer.parseInt(smsCode.trim());

                    long dateDifference = new Date().getTime() - latestSentCode.getCreateDate().getTime();
                    System.out.println("Date difference is " + (dateDifference / 60000) + " min");
                    if(latestSentCodeInt == smsCodeInt && dateDifference < TEN_MIN_IN_MS) {
                        codeVerificationCode = CodeVerificationCode.OK;
                    } else if(latestSentCodeInt == smsCodeInt && dateDifference >= TEN_MIN_IN_MS) {
                        codeVerificationCode = CodeVerificationCode.EXPIRED;
                    }
                }

            } catch (NoResultException e) {
                codeVerificationCode = CodeVerificationCode.NOT_MATCH;
            }

            commit();
            return new CodeVerificationResult(codeVerificationCode, latestSentCode);
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Could not check last code verification", e);
        }
    }
}