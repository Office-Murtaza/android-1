package com.batm.service;

import com.batm.entity.*;
import com.batm.entity.Error;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private HibernateTemplate hibernateTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SMSService smsService;

    @Autowired
    private AuthenticationService authenticationService;

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

        if (smsService.sendCode(user.getPhone())) {
            authenticationService.authenticate(loadUserByUsername(user.getPhone()));

            return Response.ok(Json.register(userId));
        } else {
            return Response.error(Json.register(userId), new Error(2, "Error send SMS code"));
        }
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

        if (smsService.sendCode(user.getPhone())) {
            return Response.ok(Json.login(true));
        } else {
            return Response.error(new Error(2, "Error send SMS code"));
        }
    }

    public List<User> test() {
        Session session = sessionFactory.getCurrentSession();

        List<User> users = session.createQuery("SELECT u FROM User u", User.class).getResultList();

        return users;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Optional<User> optionalUser = findByPhone(phone);

        return Optional.ofNullable(optionalUser).orElseThrow(() -> new UsernameNotFoundException("Phone not found"))
                .map(UserDetailsImpl::new).get();
    }

    private Optional<User> findByPhone(String phone) {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.createQuery("SELECT u FROM User u WHERE u.phone = :phone");
        query.setParameter("phone", phone);

        return Optional.of((User) query.getSingleResult());
    }
}