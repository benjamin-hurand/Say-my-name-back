package com.oxyl.service;

import com.oxyl.core.model.User;
import com.oxyl.persistence.dao.UserDao;
import com.oxyl.persistence.entity.CustomUserDetails;
import com.oxyl.persistence.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
        // Encode le mot de passe avant de sauvegarder l'user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }

    public User setActive(User user) {
        // Encode le mot de passe avant de sauvegarder l'user
        user.setActive(true);
        return userDao.save(user);
    }

    public Boolean checkIfEmailExists(String email) {
        return userDao.checkIfEmailExists(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userDao.findEntityByEmail(username);
        return new CustomUserDetails(user);
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }
}
