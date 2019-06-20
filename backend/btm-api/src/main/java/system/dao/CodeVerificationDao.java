package system.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import system.model.User;
import system.model.exception.CodeVerificationException;
import system.model.CodeVerification;
import system.model.exception.UserException;

import javax.persistence.NoResultException;
import java.util.*;

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

    public boolean isCodeTheSameAsTheLastCodeSentToUser(long userId, String smsCode) throws UserException {
        try {
            begin();

            boolean goodCodeExists = false;
            try {
                List codeVerificationsAsObjects = getSession()
                        .createQuery("from CodeVerification v where v.userId = :userId")
                        .setParameter("userId", userId)
                        .list();
                if(!codeVerificationsAsObjects.isEmpty()) {
                    LinkedList<CodeVerification> codeVerificationsCasted = new LinkedList<>(
                            (List<CodeVerification>) codeVerificationsAsObjects
                    );
                    codeVerificationsCasted.sort(Comparator.comparing(CodeVerification::getCreateDate));
                    CodeVerification latestSentCode = codeVerificationsCasted.getFirst();//todo check if it will be first or last
                    long dateDifference = new Date().getTime() - latestSentCode.getCreateDate().getTime();
                    if(latestSentCode.getCode().equals(smsCode) && dateDifference < TEN_MIN_IN_MS) {
                        goodCodeExists = true;
                    }
                    System.out.println(
                            "Latest is " + codeVerificationsCasted.getFirst().getCode() +
                                    " created at " + codeVerificationsCasted.getFirst().getCreateDate()
                    );
                    System.out.println(
                            "Difference is " +
                            ((new Date().getTime() - codeVerificationsCasted.getFirst().getCreateDate().getTime()) / TEN_MIN_IN_MS)
                    );
                }

            } catch (NoResultException e) {
                goodCodeExists = false;
            }

            commit();
            return goodCodeExists;
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Could not check last code verification", e);
        }
    }
}