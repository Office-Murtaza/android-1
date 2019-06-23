package com.batm.service;

import com.batm.entity.Error;
import com.batm.entity.Json;
import com.batm.entity.Response;
import com.batm.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Response register(User user) {
        Session session = sessionFactory.getCurrentSession();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Query query = session.createQuery("SELECT u FROM User u WHERE u.phone = :phone");
        query.setParameter("phone", user.getPhone());

        User dbUser = (User) query.getSingleResult();

        if (dbUser == null) {
            return Response.error(new Error(1, "Phone already registered"));
        }

        Long userId = (Long) session.save(user);

        //send SMS
        return Response.ok(Json.register(userId));
    }

    public Response login(User user) {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery("SELECT u FROM User u WHERE u.phone = :phone");
        query.setParameter("phone", user.getPhone());

        User dbUser = (User) query.getSingleResult();

        if (dbUser == null) {
            return Response.error(new Error(1, "User doesn't exist"));
        } else if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            return Response.error(new Error(2, "Wrong phone or password"));
        }

        //send SMS
        return Response.ok(Json.login(true));
    }

    public List<User> test() {
        Session session = sessionFactory.getCurrentSession();

        List<User> users = session.createQuery("SELECT u FROM User u", User.class).getResultList();

        return users;
    }
}