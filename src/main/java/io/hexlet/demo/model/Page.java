package io.hexlet.demo.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class Page {
    // Используется как идентификатор в ссылках /pages/about
    // Здесь about — это слаг
    private String slug;
    private String name;
    private String body;

}
