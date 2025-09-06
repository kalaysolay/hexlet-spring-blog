package io.hexlet.demo.controllers;

import io.hexlet.demo.model.Page;
import io.hexlet.demo.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PostController {

    List<Post> posts = new ArrayList<Post>();

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> index(@RequestParam(defaultValue = "10") Integer limit) {
        var res=  posts.stream().limit(limit).toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.size()))
                .body(res);
    }

    @GetMapping("/posts/{id}")
    public Optional<Post> show(@PathVariable String id) {
        return posts.stream()
                .filter(p -> p.getTitle().equals(id))
                .findFirst();
    }

    @PostMapping("/posts") // Создание поста
    public ResponseEntity<Post> create(@RequestBody Post post) {
        posts.add(post);
        return ResponseEntity.status(201).body(post);
    }

    @DeleteMapping("/posts/{id}") // Удаление поста
    public void destroy(@PathVariable String id) {
        posts.removeIf(p -> p.getTitle().equals(id));
    }

    @PutMapping("/posts/{id}") // Обновление страницы
    public ResponseEntity<Post> update(@PathVariable String id, @RequestBody Post data) {
        var maybePost = posts.stream()
                .filter(p -> p.getTitle().equals(id))
                .findFirst();
        if (maybePost.isPresent()) {
            var page = maybePost.get();
            page.setAuthor(data.getAuthor());
            page.setTitle(data.getTitle());
            page.setContent(data.getContent());
            page.setCreatedAt(data.getCreatedAt());
        }
        return ResponseEntity.ok(data);
    }

}
