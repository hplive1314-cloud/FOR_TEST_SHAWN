package com.qicheng.workspace.content;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_items")
public class ContentItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, length = 500)
    private String summary;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(nullable = false, length = 24)
    private String status;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ContentItem() {}
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
