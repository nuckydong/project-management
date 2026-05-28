package com.pm.service.impl;

import com.pm.dto.request.DocumentCreateRequest;
import com.pm.dto.request.DocumentUpdateRequest;
import com.pm.dto.response.DocumentResponse;
import com.pm.dto.response.DocumentVersionResponse;
import com.pm.dto.response.UserResponse;
import com.pm.entity.Document;
import com.pm.entity.DocumentVersion;
import com.pm.entity.Project;
import com.pm.entity.User;
import com.pm.repository.DocumentRepository;
import com.pm.repository.DocumentVersionRepository;
import com.pm.repository.ProjectRepository;
import com.pm.repository.UserRepository;
import com.pm.service.DocumentService;
import com.pm.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;

    @Override
    public List<DocumentResponse> list(Long projectId) {
        return documentRepository.findByProjectId(projectId).stream()
                .map(this::toDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentResponse create(Long projectId, Long userId, DocumentCreateRequest request) {
        Project project = projectRepository.getReferenceById(projectId);
        User createdBy = userRepository.getReferenceById(userId);

        Document document = new Document();
        document.setProject(project);
        document.setTitle(request.getTitle());
        document.setType(request.getType() != null ? request.getType() : "OTHER");
        document.setCreatedBy(createdBy);
        document.setCurrentVersion(0);
        document = documentRepository.save(document);

        return toDocumentResponse(document);
    }

    @Override
    @Transactional
    public DocumentResponse update(Long id, DocumentUpdateRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getType() != null) {
            document.setType(request.getType());
        }

        document = documentRepository.save(document);
        return toDocumentResponse(document);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        documentRepository.deleteById(id);
    }

    @Override
    public DocumentResponse getById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return toDocumentResponse(document);
    }

    @Override
    @Transactional
    public DocumentVersionResponse uploadVersion(Long documentId, MultipartFile file, Long userId, String changeSummary) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        int newVersionNo = document.getCurrentVersion() + 1;
        document.setCurrentVersion(newVersionNo);

        String filePath = minioService.uploadDocument(document.getProject().getId(), file);

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNo(newVersionNo);
        version.setFilePath(filePath);
        version.setFileName(file.getOriginalFilename());
        version.setFileSize(file.getSize());
        version.setUploadedBy(userRepository.getReferenceById(userId));
        version.setChangeSummary(changeSummary);
        version = documentVersionRepository.save(version);

        documentRepository.save(document);

        return toDocumentVersionResponse(version);
    }

    @Override
    public List<DocumentVersionResponse> getVersions(Long documentId) {
        return documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(documentId).stream()
                .map(this::toDocumentVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String downloadVersion(Long versionId) {
        DocumentVersion version = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Document version not found"));
        return minioService.getPubDocUrl(version.getFilePath());
    }

    @Override
    public String previewVersion(Long versionId) {
        return downloadVersion(versionId);
    }

    private DocumentResponse toDocumentResponse(Document document) {
        DocumentVersionResponse currentVersionInfo = null;
        if (document.getCurrentVersion() > 0) {
            List<DocumentVersion> versions = documentVersionRepository
                    .findByDocumentIdOrderByVersionNoDesc(document.getId());
            if (!versions.isEmpty()) {
                currentVersionInfo = toDocumentVersionResponse(versions.getFirst());
            }
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .projectId(document.getProject().getId())
                .title(document.getTitle())
                .type(document.getType())
                .currentVersion(document.getCurrentVersion())
                .createdBy(toUserResponse(document.getCreatedBy()))
                .currentVersionInfo(currentVersionInfo)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private DocumentVersionResponse toDocumentVersionResponse(DocumentVersion version) {
        String downloadUrl = minioService.getPresignedUrl("docs", version.getFilePath());

        return DocumentVersionResponse.builder()
                .id(version.getId())
                .documentId(version.getDocument().getId())
                .versionNo(version.getVersionNo())
                .fileName(version.getFileName())
                .fileSize(version.getFileSize())
                .uploadedBy(toUserResponse(version.getUploadedBy()))
                .changeSummary(version.getChangeSummary())
                .downloadUrl(downloadUrl)
                .createdAt(version.getCreatedAt())
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
