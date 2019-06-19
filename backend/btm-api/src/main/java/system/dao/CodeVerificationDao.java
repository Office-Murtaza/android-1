package system.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import system.model.exception.CodeVerificationException;
import system.model.CodeVerification;

import java.util.List;

@Repository
public class CodeVerificationDao extends Dao {

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
}