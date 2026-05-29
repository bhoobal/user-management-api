package com.api.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

public class UserDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Username is required")
        private String username;
    }

    @Data
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String username;
        private Set<GroupDto.Summary> groups;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    public static class AddToGroupRequest {
        private Long groupId;
    }
}
