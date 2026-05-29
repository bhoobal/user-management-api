package com.api.usermanagement.controller;

import com.api.usermanagement.dto.ApiResponse;
import com.api.usermanagement.dto.UserDto;
import com.api.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto.Response>> createUser(
            @Valid @RequestBody UserDto.CreateRequest request) {
        log.info("POST /api/v1/users - creating user: username={}, email={}", request.getUsername(), request.getEmail());
        UserDto.Response user = userService.createUser(request);
        log.info("User created successfully: id={}, username={}", user.getId(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> getAllUsers() {
        log.info("GET /api/v1/users - fetching all users");
        List<UserDto.Response> users = userService.getAllUsers();
        log.info("Returning {} user(s)", users.size());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{} - fetching user by id", id);
        UserDto.Response user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PostMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<ApiResponse<UserDto.Response>> addUserToGroup(
            @PathVariable Long userId,
            @PathVariable Long groupId) {
        log.info("POST /api/v1/users/{}/groups/{} - adding user to group", userId, groupId);
        UserDto.Response user = userService.addUserToGroup(userId, groupId);
        return ResponseEntity.ok(ApiResponse.success("User added to group successfully", user));
    }

    @DeleteMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<ApiResponse<UserDto.Response>> removeUserFromGroup(
            @PathVariable Long userId,
            @PathVariable Long groupId) {
        log.info("DELETE /api/v1/users/{}/groups/{} - removing user from group", userId, groupId);
        UserDto.Response user = userService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.ok(ApiResponse.success("User removed from group successfully", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/v1/users/{} - deleting user", id);
        userService.deleteUser(id);
        log.info("User id={} deleted successfully", id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
