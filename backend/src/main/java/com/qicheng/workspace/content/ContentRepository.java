package com.qicheng.workspace.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContentRepository extends JpaRepository<ContentItem, Long>, JpaSpecificationExecutor<ContentItem> {
    long countByStatus(String status);
}
