package com.pm.repository;

import com.pm.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {

    List<TaskTag> findByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);
}
