package com.api.usermanagement.controller;

import com.api.usermanagement.dto.ApiResponse;
import com.api.usermanagement.dto.GroupDto;
import com.api.usermanagement.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupDto.Response>> createGroup(
            @Valid @RequestBody GroupDto.CreateRequest request) {
        log.info("POST /api/v1/groups - creating group: name={}", request.getName());
        GroupDto.Response group = groupService.createGroup(request);
        log.info("Group created successfully: id={}, name={}", group.getId(), group.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", group));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupDto.Response>>> getAllGroups() {
        log.info("GET /api/v1/groups - fetching all groups");
        List<GroupDto.Response> groups = groupService.getAllGroups();
        log.info("Returning {} group(s)", groups.size());
        return ResponseEntity.ok(ApiResponse.success("Groups retrieved successfully", groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupDto.Response>> getGroupById(@PathVariable Long id) {
        log.info("GET /api/v1/groups/{} - fetching group by id", id);
        GroupDto.Response group = groupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponse.success("Group retrieved successfully", group));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        log.info("DELETE /api/v1/groups/{} - deleting group", id);
        groupService.deleteGroup(id);
        log.info("Group id={} deleted successfully", id);
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }
}
