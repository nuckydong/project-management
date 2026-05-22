package com.pm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "task_tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"task_id", "tag_id"})
})
public class TaskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
