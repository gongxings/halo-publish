package com.biyebang.halo.publish.dto;

import lombok.Data;
import java.util.List;

@Data
public class ArticleDTO {
    private Long id;
    private String title;
    private String contentHtml;
    private String summary;
    private String coverImageUrl;
    private String authorName;
    private List<String> tags;
    private List<String> imageUrls;
}
