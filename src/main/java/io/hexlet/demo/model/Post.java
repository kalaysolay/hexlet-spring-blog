package io.hexlet.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class Post {
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;

}
