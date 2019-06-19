package system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import system.dao.UserDao;
import system.model.User;
import system.model.exception.UserException;

import java.util.LinkedList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserDao dao;

    public User create(User user) {
        try {
            return dao.create(user);
        } catch (UserException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(User user) {
        try {
            dao.delete(user);
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    public List<User> list() {
        try {
            return dao.list();
        } catch (UserException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

}
