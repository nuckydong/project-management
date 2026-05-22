package com.pm.repository;

import com.pm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByProjectIdAndStatus(Long projectId, String status);

    List<Task> findByProjectIdAndSprintId(Long projectId, Long sprintId);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByParentId(Long parentId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
            "AND (:sprintId IS NULL OR t.sprint.id = :sprintId) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> findByFilters(@Param("projectId") Long projectId,
                             @Param("status") String status,
                             @Param("assigneeId") Long assigneeId,
                             @Param("sprintId") Long sprintId,
                             @Param("priority") String priority,
                             @Param("keyword") String keyword);

    long countByProjectIdAndStatus(Long projectId, String status);

    long countByProjectId(Long projectId);

    long countBySprintId(Long sprintId);

    long countBySprintIdAndStatus(Long sprintId, String status);

    long countByAssigneeIdAndStatus(Long assigneeId, String status);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countByProjectIdGroupedByStatus(@Param("projectId") Long projectId);
}
