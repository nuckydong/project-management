package com.pm.repository;

import com.pm.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByProjectId(Long projectId);

    List<Document> findByProjectIdAndType(Long projectId, String type);
}
