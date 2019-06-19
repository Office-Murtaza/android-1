package system.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import system.model.User;
import system.model.exception.UserException;

import java.util.List;

@Repository
public class UserDao extends Dao {

    public User create(User user)
            throws UserException {
        try {
            begin();            
            getSession().save(user);     
            commit();
            return user;
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Exception while creating user: " + e.getMessage());
        }
    }

    public void delete(User user)
            throws UserException {
        try {
            begin();
            getSession().delete(user);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Could not delete user", e);
        }
    }
    
    public List<User> list() throws UserException{
    	
    	try {
            begin();
            Query q = getSession().createQuery("from User");
            List<User> adverts = q.list();
            commit();
            return adverts;
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Could not list users", e);
        }
    	
    }
}