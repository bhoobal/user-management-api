package com.api.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

public class GroupDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Group name is required")
        private String name;

        private String description;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Set<UserSummary> members;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    public static class Summary {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
    }
}
