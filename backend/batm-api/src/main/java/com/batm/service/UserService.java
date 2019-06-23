package com.batm.service;

import com.batm.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private SessionFactory sessionFactory;

    public Long register(User user) {
        Session session = sessionFactory.getCurrentSession();

        return (Long) session.save(user);
    }

    public int test() {
        Session session = sessionFactory.getCurrentSession();

        List<User> users = session.createQuery("SELECT u FROM User u", User.class).getResultList();

        return users.size();
    }
}
