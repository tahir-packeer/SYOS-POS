// File: src/main/java/org/example/dao/impl/UserDAOImpl.java
package org.example.dao.impl;

import org.example.dao.UserDAO;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);
    private Connection conn;

    public UserDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
                user.setEmail(rs.getString("email"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.error("Error fetching user by username: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
                user.setEmail(rs.getString("email"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.error("Error fetching user by ID: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> getUsersByRole(User.UserRole role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? AND is_active = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error fetching users by role: " + role, e);
        }
        return users;
    }

    @Override
    public int addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, email, is_active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRoleString());
            ps.setString(4, user.getEmail());
            ps.setBoolean(5, user.isActive());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    logger.info("User created successfully: {}", user.getUsername());
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating user: " + user.getUsername(), e);
        }
        return -1;
    }

    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, is_active = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setBoolean(2, user.isActive());
            ps.setInt(3, user.getUserId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User updated successfully: {}", user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating user: " + user.getUserId(), e);
        }
        return false;
    }

    @Override
    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User deactivated: {}", userId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deactivating user: " + userId, e);
        }
        return false;
    }

    @Override
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking username existence: " + username, e);
        }
        return false;
    }
}