package com.api.usermanagement.service;

import com.api.usermanagement.dto.GroupDto;
import com.api.usermanagement.exception.DuplicateResourceException;
import com.api.usermanagement.exception.ResourceNotFoundException;
import com.api.usermanagement.model.ProjectGroup;
import com.api.usermanagement.model.User;
import com.api.usermanagement.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupDto.Response createGroup(GroupDto.CreateRequest request) {
        log.debug("Checking for duplicate group name: {}", request.getName());
        if (groupRepository.existsByName(request.getName())) {
            log.warn("Group already exists: {}", request.getName());
            throw new DuplicateResourceException("Group already exists: " + request.getName());
        }

        ProjectGroup group = ProjectGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        ProjectGroup saved = groupRepository.save(group);
        log.debug("Group persisted: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupDto.Response> getAllGroups() {
        List<GroupDto.Response> groups = groupRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.debug("getAllGroups: found {} group(s)", groups.size());
        return groups;
    }

    @Transactional(readOnly = true)
    public GroupDto.Response getGroupById(Long id) {
        log.debug("Looking up group id={}", id);
        GroupDto.Response group = toResponse(findGroupById(id));
        log.debug("Found group: name={}, members={}", group.getName(), group.getMembers().size());
        return group;
    }

    public void deleteGroup(Long id) {
        ProjectGroup group = findGroupById(id);
        log.debug("Deleting group id={}, removing {} member(s) from group", id, group.getMembers().size());
        group.getMembers().forEach(user -> user.getGroups().remove(group));
        groupRepository.delete(group);
    }

    private ProjectGroup findGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    private GroupDto.Response toResponse(ProjectGroup group) {
        GroupDto.Response response = new GroupDto.Response();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setCreatedAt(group.getCreatedAt() != null ? group.getCreatedAt().toString() : null);
        response.setUpdatedAt(group.getUpdatedAt() != null ? group.getUpdatedAt().toString() : null);
        response.setMembers(
                group.getMembers().stream().map(u -> {
                    GroupDto.UserSummary summary = new GroupDto.UserSummary();
                    summary.setId(u.getId());
                    summary.setUsername(u.getUsername());
                    summary.setEmail(u.getEmail());
                    summary.setFirstName(u.getFirstName());
                    summary.setLastName(u.getLastName());
                    return summary;
                }).collect(Collectors.toSet())
        );
        return response;
    }
}
