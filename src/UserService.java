package com.api.usermanagement.service;

import com.api.usermanagement.dto.GroupDto;
import com.api.usermanagement.dto.UserDto;
import com.api.usermanagement.exception.DuplicateResourceException;
import com.api.usermanagement.exception.ResourceNotFoundException;
import com.api.usermanagement.model.ProjectGroup;
import com.api.usermanagement.model.User;
import com.api.usermanagement.repository.GroupRepository;
import com.api.usermanagement.repository.UserRepository;
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
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserDto.Response createUser(UserDto.CreateRequest request) {
        log.debug("Checking for duplicate email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already in use: {}", request.getEmail());
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        log.debug("Checking for duplicate username: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already taken: {}", request.getUsername());
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .build();

        User saved = userRepository.save(user);
        log.debug("User persisted: id={}, username={}", saved.getId(), saved.getUsername());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDto.Response> getAllUsers() {
        List<UserDto.Response> users = userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.debug("getAllUsers: found {} user(s)", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public UserDto.Response getUserById(Long id) {
        log.debug("Looking up user id={}", id);
        UserDto.Response user = toResponse(findUserById(id));
        log.debug("Found user: username={}", user.getUsername());
        return user;
    }

    public UserDto.Response addUserToGroup(Long userId, Long groupId) {
        log.debug("Adding user id={} to group id={}", userId, groupId);
        User user = findUserById(userId);
        ProjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        user.getGroups().add(group);
        group.getMembers().add(user);

        UserDto.Response response = toResponse(userRepository.save(user));
        log.debug("User id={} now belongs to {} group(s)", userId, response.getGroups().size());
        return response;
    }

    public UserDto.Response removeUserFromGroup(Long userId, Long groupId) {
        log.debug("Removing user id={} from group id={}", userId, groupId);
        User user = findUserById(userId);
        ProjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        user.getGroups().remove(group);
        group.getMembers().remove(user);

        UserDto.Response response = toResponse(userRepository.save(user));
        log.debug("User id={} now belongs to {} group(s)", userId, response.getGroups().size());
        return response;
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        int groupCount = user.getGroups().size();
        log.debug("Deleting user id={}, removing from {} group(s)", id, groupCount);
        user.getGroups().forEach(group -> group.getMembers().remove(user));
        userRepository.delete(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserDto.Response toResponse(User user) {
        UserDto.Response response = new UserDto.Response();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        response.setGroups(
                user.getGroups().stream().map(g -> {
                    GroupDto.Summary summary = new GroupDto.Summary();
                    summary.setId(g.getId());
                    summary.setName(g.getName());
                    summary.setDescription(g.getDescription());
                    return summary;
                }).collect(Collectors.toSet())
        );
        return response;
    }
}
