package lk.ijse.dep11.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.EmployeeTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employee")
@CrossOrigin
public class EmployeeHttpController {

    private final HikariDataSource pool;

    public EmployeeHttpController() {
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("root");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/practicalExamJan1");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("maximumPoolSize", 30);
        pool = new HikariDataSource(config);
    }
//    @GetMapping
//    public String greeting(){
//        return "Hello sahan";
//    }

    //Create new Employee API
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EmployeeTO> createEmployee(@RequestBody @Validated EmployeeTO employeeTO) throws SQLException {
        try(Connection connection = pool.getConnection()){
            //Check if contact number is already exists
            if(isContactNumberExists(connection, employeeTO.getContact())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            //Insert the new Employee
            String insertQuery = "INSERT INTO employee(emp_id, name, contact, address, status) VALUES (?, ?, ?, ?, ?)";
            try(PreparedStatement pstm = connection.prepareStatement(insertQuery)){
                pstm.setString(1, employeeTO.getEmpId());
                pstm.setString(2, employeeTO.getName());
                pstm.setString(3, employeeTO.getContact());
                pstm.setString(4, employeeTO.getAddress());
                pstm.setBoolean(5, true);

                pstm.executeUpdate();
                return ResponseEntity.status(HttpStatus.CREATED).body(employeeTO);
            }

        }catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.out.println("error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // You can customize the response as needed
        }
    }
    //Helper method to check contact number is already exists or not
    private boolean isContactNumberExists(Connection connection, String contact) throws SQLException {
        String query = "SELECT emp_id FROM employee WHERE contact = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, contact);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If there is a result, it means the contact number already exists
            }
        }
    }

    //Update an existing Employee API
    @PatchMapping(value = "/{empId}", consumes = "application/json")
    public ResponseEntity<Void> updateEmployee(@PathVariable String empId, @RequestBody @Validated EmployeeTO employeeTO) throws SQLException {
        try(Connection connection = pool.getConnection()){
            // Check if the contact number already exists for other employees (excluding the current employee being updated)
            if(isContactNumberExistsForOtherEmployees(connection, empId, employeeTO.getContact())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            // Update the existing employee
            String updateQuery = "UPDATE employee SET contact = ?, address = ? WHERE emp_id = ?";
            try (PreparedStatement pstm = connection.prepareStatement(updateQuery)) {
                pstm.setString(1, employeeTO.getContact());
                pstm.setString(2, employeeTO.getAddress());
                pstm.setString(3, empId);

                int updatedRows = pstm.executeUpdate();
                if (updatedRows > 0) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Employee not found
                }
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isContactNumberExistsForOtherEmployees(Connection connection, String empId, String contact) throws SQLException {
        String query = "SELECT emp_id FROM employee WHERE contact = ? AND emp_id <> ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, contact);
            preparedStatement.setString(2, empId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If there is a result, it means the contact number already exists for another employee
            }
        }
    }

    //Deactivate current active employee API
    @PatchMapping(value = "/deactivate/{empId}")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable String empId ) throws SQLException {
        try(Connection connection = pool.getConnection()){
            // Check the current status of the employee
            Boolean currentStatus = getCurrentStatus(connection, empId);
            if(currentStatus == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Employee not found
            }
            if(!currentStatus){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Employee is already deactivated
            }
            // Update the status of the employee to false
            String updateStatusQuery = "UPDATE employee SET status = false WHERE emp_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateStatusQuery)) {
                preparedStatement.setString(1, empId);

                int updatedRows = preparedStatement.executeUpdate();
                if (updatedRows > 0) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Employee not found
                }
            }

        }catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Boolean getCurrentStatus(Connection connection, String empId) throws SQLException {
        String query = "SELECT status FROM employee WHERE emp_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, empId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("status");
                }
                return null; // Employee not found
            }
        }
    }

    //Get all the employees API
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<EmployeeTO>> getAllEmployees() {
        try (Connection connection = pool.getConnection()) {
            List<EmployeeTO> employees = new ArrayList<>();

            String getAllEmployeesQuery = "SELECT emp_id, name, contact, address, status FROM employee ORDER BY emp_id";
            try (PreparedStatement preparedStatement = connection.prepareStatement(getAllEmployeesQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        EmployeeTO employee = new EmployeeTO();
                        employee.setEmpId(resultSet.getString("emp_id"));
                        employee.setName(resultSet.getString("name"));
                        employee.setContact(resultSet.getString("contact"));
                        employee.setAddress(resultSet.getString("address"));
                        employee.setStatus(resultSet.getBoolean("status"));

                        employees.add(employee);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(employees);

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Get All employees by page wise
//    @GetMapping(produces = "application/json")
//    public ResponseEntity<List<EmployeeTO>> getAllPaginatedEmployees(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int pageSize
//    ) throws SQLException {
//        // Calculate the offset based on page and pageSize
//        int offset = (page - 1) * pageSize;
//
//        try(Connection connection = pool.getConnection()){
//
//            List<EmployeeTO> employees = new ArrayList<>();
//            String getAllEmployeesQuery = "SELECT emp_id, name, contact, address, status FROM employee LIMIT ? OFFSET ?";
//            try (PreparedStatement preparedStatement =connection.prepareStatement(getAllEmployeesQuery)) {
//                preparedStatement.setInt(1, pageSize);
//                preparedStatement.setInt(2, offset);
//
//                try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                    while (resultSet.next()) {
//                        // Same code to populate employees
//                    }
//                }
//            }
//            return ResponseEntity.status(HttpStatus.OK).body(employees);
//        }catch (SQLException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//
//
//    }

    //Get all the active employees API
    @GetMapping(value = "/active", produces = "application/json")
    public ResponseEntity<List<EmployeeTO>> getActiveEmployees() {
        try (Connection connection = pool.getConnection()) {
            List<EmployeeTO> activeEmployees = new ArrayList<>();

            String getActiveEmployeesQuery = "SELECT emp_id, name, contact, address, status FROM employee WHERE status = true";
            try (PreparedStatement preparedStatement = connection.prepareStatement(getActiveEmployeesQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        EmployeeTO employee = new EmployeeTO();
                        employee.setEmpId(resultSet.getString("emp_id"));
                        employee.setName(resultSet.getString("name"));
                        employee.setContact(resultSet.getString("contact"));
                        employee.setAddress(resultSet.getString("address"));
                        employee.setStatus(resultSet.getBoolean("status"));

                        activeEmployees.add(employee);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(activeEmployees);

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
