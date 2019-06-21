package system.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import system.model.CodeVerification;
import system.model.User;
import system.model.exception.CodeVerificationException;
import system.model.exception.UserException;

import javax.persistence.NoResultException;
import java.util.LinkedList;
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

    public void update(User user)
            throws UserException {
        try {
            begin();
            getSession().update(user);
            commit();
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Exception while updating user: " + e.getMessage());
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
            List<User> users = q.list();
            commit();
            return users;
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Could not list users", e);
        }

    }

    public boolean isUserWithConfirmedPhoneExist(User u) throws UserException {
        try {
            begin();

            boolean userExists;
            try {
                userExists = !getSession().createQuery("from User u where u.phone = :phone and u.phoneConfirmed = :phone_confirmed ")
                        .setParameter("phone", u.getPhone())
                        .setParameter("phone_confirmed", 1L)
                        .list().isEmpty();
            } catch (NoResultException e) {
                userExists = false;
            }

            commit();
            return userExists;
        } catch (HibernateException e) {
            rollback();
            throw new UserException("Could not list users", e);
        }
    }
}