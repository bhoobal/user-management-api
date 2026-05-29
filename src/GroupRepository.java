package com.api.usermanagement.repository;

import com.api.usermanagement.model.ProjectGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<ProjectGroup, Long> {
    Optional<ProjectGroup> findByName(String name);
    boolean existsByName(String name);
}
