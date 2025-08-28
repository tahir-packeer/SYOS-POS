package org.example;// File: DatabaseConnectionTest.java
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/syos";
    private static final String USER = "root";
    private static final String PASSWORD = "9900@tahir";

    @Test
    void testConnectionEstablished() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        } catch (SQLException e) {
            fail("Should connect without exception, but got: " + e.getMessage());
        }
    }

    @Test
    void testConnectionWithInvalidCredentials() {
        assertThrows(SQLException.class, () -> {
            DriverManager.getConnection(JDBC_URL, "wronguser", "wrongpass");
        }, "Should throw SQLException for invalid credentials");
    }

    @Test
    void testConnectionClosedProperly() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        conn.close();
        assertTrue(conn.isClosed(), "Connection should be closed after calling close()");
    }
}