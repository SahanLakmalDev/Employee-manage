package lk.ijse.dep11.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTO implements Serializable {
    @Null(message = "UserID should be null")
    private Integer userId;
    @NotBlank(message = "Username should not be blank")
    private String username;
    @NotBlank(message = "Password should not be blank")
    private String password;
}
