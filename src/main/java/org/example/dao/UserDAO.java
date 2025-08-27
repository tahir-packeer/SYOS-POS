// File: src/main/java/org/example/dao/UserDAO.java
package org.example.dao;

import org.example.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserById(int userId);
    List<User> getUsersByRole(User.UserRole role);
    int addUser(User user);
    boolean updateUser(User user);
    boolean deactivateUser(int userId);
    boolean isUsernameExists(String username);
}