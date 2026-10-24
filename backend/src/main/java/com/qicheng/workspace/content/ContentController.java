package com.qicheng.workspace.content;

import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
public class ContentController {
    private final ContentRepository contents;
    public ContentController(ContentRepository contents) { this.contents = contents; }

    @GetMapping
    @Transactional(readOnly = true)
    public ContentPage list(@RequestParam(defaultValue = "") @Size(max = 100) String q,
                            @RequestParam(defaultValue = "") @Pattern(regexp = "|PUBLISHED|DRAFT") String status,
                            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
                            @RequestParam(defaultValue = "6") @Min(1) @Max(100) int size) {
        Specification<ContentItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!q.isBlank()) {
                String escaped = q.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + escaped.toLowerCase(java.util.Locale.ROOT) + "%", '\\'));
            }
            if (!status.isEmpty()) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var result = contents.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt", "id")));
        var stats = new ContentStats(contents.count(), contents.countByStatus("PUBLISHED"), contents.countByStatus("DRAFT"));
        return new ContentPage(result.map(ContentView::from).getContent(), result.getTotalElements(), page, size, stats);
    }

    public record ContentPage(List<ContentView> items, long total, int page, int size, ContentStats stats) {}
    public record ContentStats(long total, long published, long draft) {}
    public record ContentView(Long id, String title, String summary, String category, String status, LocalDateTime updatedAt) {
        static ContentView from(ContentItem item) {
            return new ContentView(item.getId(), item.getTitle(), item.getSummary(), item.getCategory(), item.getStatus(), item.getUpdatedAt());
        }
    }
}
