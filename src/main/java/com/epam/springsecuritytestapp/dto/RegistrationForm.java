package com.epam.springsecuritytestapp.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Objects;

@Data
public class RegistrationForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String email;

    @NotBlank(message = "La clave es obligatoria")
    @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
    private String password;

    @NotBlank(message = "La confirmación de la clave es obligatoria")
    private String confirmPassword;

    @AssertTrue(message = "Las claves no coinciden")
    public boolean isPasswordConfirmed() {
        return Objects.equals(password, confirmPassword);
    }
}
