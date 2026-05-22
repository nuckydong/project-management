package com.pm.repository;

import com.pm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByWorkspaceId(Long workspaceId);

    List<Project> findByWorkspaceIdAndStatus(Long workspaceId, String status);
}
