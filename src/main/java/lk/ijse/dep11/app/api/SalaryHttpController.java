package lk.ijse.dep11.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.SalaryTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/salary")
@CrossOrigin
public class SalaryHttpController {
    private final HikariDataSource pool;

    public SalaryHttpController() {
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("root");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/practicalExamJan1");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("maximumPoolSize", 20);
        pool = new HikariDataSource(config);
    }

    //Get salary details based on particular employee and given year
    @GetMapping(value = "/{empId}", params = "year", produces = "application/json")
    public ResponseEntity<List<SalaryTO>> getSalaryDetailsByEmployeeAndYear(@PathVariable String empId, @RequestParam int year) throws SQLException {
        try(Connection connection = pool.getConnection()){

//             Check if the employee with the specified empId exists
            if(!employeeExists(connection, empId)){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Employee not found
            }
            // Retrieve salary details for the specified employee and year
            List<SalaryTO> salaryDetails = new ArrayList<>();
            String getSalaryDetailsQuery = "SELECT salary_id, year, month, salary, create_date_time, emp_id FROM salary WHERE emp_id = ? AND year = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(getSalaryDetailsQuery)) {
                preparedStatement.setString(1, empId);
                preparedStatement.setInt(2, year);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        SalaryTO salaryTO = new SalaryTO();
                        salaryTO.setSalaryId(resultSet.getInt("salary_id"));
                        salaryTO.setYear(resultSet.getString("year"));
                        salaryTO.setMonth(resultSet.getString("month"));
                        salaryTO.setSalary(resultSet.getBigDecimal("salary"));
                        salaryTO.setCreatedDataTime(resultSet.getTimestamp("create_date_time"));
                        salaryTO.setEmpId(resultSet.getString("emp_id"));

                        salaryDetails.add(salaryTO);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(salaryDetails);

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    private boolean employeeExists(Connection connection, String empId) throws SQLException {
        String query = "SELECT emp_id FROM employee WHERE emp_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, empId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If there is a result, the employee exists
            }
        }
    }

    //Make salary payment transaction API
    @PostMapping(value = "/make-payment", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> makeSalaryPayment(@RequestBody SalaryTO salaryTO) {
        Connection connection = null;
        try {
            connection = pool.getConnection();

            // Disable auto-commit to start a transaction
            connection.setAutoCommit(false);

            // Check if the employee with the specified empId exists
            if (!employeeExists(connection, salaryTO.getEmpId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Employee not found
            }
            // Check if the employee is active
            if (!isEmployeeActive(connection, salaryTO.getEmpId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot make payment for a deactivated employee.");
            }

            // Check if the salary payment for the specified employee, year, and month already exists
            if (salaryPaymentExists(connection, salaryTO.getEmpId(), Integer.parseInt(salaryTO.getYear()), salaryTO.getMonth())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Salary payment already made for this month
            }

            // Perform the salary payment transaction
            String insertSalaryPaymentQuery = "INSERT INTO salary (emp_id, year, month, salary, create_date_time) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSalaryPaymentQuery)) {
                preparedStatement.setString(1, salaryTO.getEmpId());
                preparedStatement.setString(2, salaryTO.getYear());
                preparedStatement.setString(3, salaryTO.getMonth());
                preparedStatement.setBigDecimal(4, salaryTO.getSalary());
                preparedStatement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    // Commit the transaction if successful
                    connection.commit();
                    return ResponseEntity.status(HttpStatus.CREATED).build(); // Salary payment successful
                } else {
                    // Rollback the transaction in case of failure
                    connection.rollback();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Failed to insert salary payment
                }
            }

        } catch (SQLException e) {
            // Handle the exception according to your needs
            e.printStackTrace();

            // Rollback the transaction in case of exception
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            // Enable auto-commit after the transaction
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException closeException) {
                closeException.printStackTrace();
            }
        }
    }

    private boolean isEmployeeActive(Connection connection, String empId) throws SQLException {
        String query = "SELECT status FROM employee WHERE emp_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, empId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean("status");
            }
        }
    }
    private boolean salaryPaymentExists(Connection connection, String empId, int year, String month) throws SQLException {
        String query = "SELECT COUNT(*) FROM salary WHERE emp_id = ? AND year = ? AND month = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, empId);
            preparedStatement.setInt(2, year);
            preparedStatement.setString(3, month);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0; // If count is greater than 0, a salary payment entry exists
            }
        }
    }



}
