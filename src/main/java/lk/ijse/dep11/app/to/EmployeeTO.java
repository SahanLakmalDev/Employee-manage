package lk.ijse.dep11.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeTO implements Serializable {
    @NotBlank(message = "Employee ID is required")
    @Size(max = 30, message = "Employee ID cannot exceed 30 characters")
    @Pattern(regexp = "^E\\d+")
    private String empId;
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Invalid Name")
    private String name;
    @NotBlank(message = "Contact is required")
    @Pattern(regexp = "^0\\d{2}-\\d{7}", message = "Invalid Contact")
    private String contact;
    @NotBlank(message = "Address is required")
    @Size(min = 4, message = "Address cannot less than 4 characters")
    private String address;
    @NotNull(message = "Status is required")
    private Boolean status;
}
