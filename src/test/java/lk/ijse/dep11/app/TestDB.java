package lk.ijse.dep11.app;

import com.github.javafaker.Faker;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class TestDB {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/practicalExamJan1", "root", "root")) {
            PreparedStatement checkEmpId = connection.prepareStatement("SELECT COUNT(*) FROM employee WHERE emp_id = ?");
            PreparedStatement insertEmployee = connection.prepareStatement("INSERT INTO employee (emp_id, name, contact, address, status) VALUES (?,?,?,?,?)");

            Faker faker = new Faker();
            Set<String> generatedEmpIds = new HashSet<>();

            for (int i = 0; i < 200; i++) {
                String empId;
                do {
                    empId = faker.regexify("E\\d{3}");
                } while (!isEmpIdUnique(checkEmpId, empId));

                insertEmployee.setString(1, empId);
                insertEmployee.setString(2, faker.name().fullName());
                insertEmployee.setString(3, faker.regexify("0\\d{2}-\\d{7}"));
                insertEmployee.setString(4, faker.address().cityName());
                insertEmployee.setBoolean(5, faker.bool().bool());

                insertEmployee.executeUpdate();
            }
        }

    }

    private static boolean isEmpIdUnique(PreparedStatement checkEmpId, String empId) throws SQLException {
        checkEmpId.setString(1, empId);
        ResultSet resultSet = checkEmpId.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) == 0;
    }
}
