package com.kristalball.military.dto;

import com.kristalball.military.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, message = "Password must contain at least 12 characters") String password,
        @NotNull RoleName role,
        Long baseId) {
}