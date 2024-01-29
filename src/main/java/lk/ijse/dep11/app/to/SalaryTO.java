package lk.ijse.dep11.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryTO implements Serializable {
    @Null(message = "SalaryId should be null")
    private Integer salaryId;
    @NotBlank(message = "Year is required")
    @Pattern(regexp = "^\\d{4}$", message = "Year should be in the format YYYY")
    private String year;
    @NotBlank(message = "Month is required")
    @Pattern(regexp = "^(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)$", message = "Invalid month format")
    private String month;
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Salary must be greater than 0.00")
    private BigDecimal salary;
    @NotNull(message = "Create Date Time is required")
    private LocalDateTime createdDataTime;
    @NotBlank(message = "Employee ID is required")
    @Pattern(regexp = "^E\\d+", message = "Invalid format")
    private String empId;

}
