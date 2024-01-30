package lk.ijse.dep11.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.UserTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/login")
@CrossOrigin
public class UserHttpController {
    private final HikariDataSource pool;

    public UserHttpController() {
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("root");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/practicalExamJan1");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("maximumPoolSize", 30);
        pool = new HikariDataSource(config);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody @Validated UserTO userTO) {
        try (Connection connection = pool.getConnection()) {
            // Check if the provided username and password match any user in the database
            if (isValidUser(connection, userTO.getUsername(), userTO.getPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Login successful");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private boolean isValidUser(Connection connection, String username, String password) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0; // If count is greater than 0, the user is valid
            }
        }
    }
}
