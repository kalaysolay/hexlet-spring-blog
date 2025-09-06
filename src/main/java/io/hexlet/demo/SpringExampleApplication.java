package io.hexlet.demo;

import io.hexlet.demo.model.Page;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@SpringBootApplication
@RestController
public class SpringExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringExampleApplication.class, args);
	}
	List<Page> pages = new ArrayList<Page>();

	@GetMapping("/page")
	public List<Page> index(@RequestParam(defaultValue = "10") Integer limit) {
		return pages.stream().limit(limit).toList();
	}

	@PostMapping("/pages") // Создание страницы
	public Page create(@RequestBody Page page) {
		pages.add(page);
		return page;
	}


	@GetMapping("/pages/{id}") // Вывод страницы
	public Optional<Page> show(@PathVariable String id) {
		var page = pages.stream()
				.filter(p -> p.getSlug().equals(id))
				.findFirst();
		return page;
	}

	@PutMapping("/pages/{id}") // Обновление страницы
	public Page update(@PathVariable String id, @RequestBody Page data) {
		var maybePage = pages.stream()
				.filter(p -> p.getSlug().equals(id))
				.findFirst();
		if (maybePage.isPresent()) {
			var page = maybePage.get();
			page.setSlug(data.getSlug());
			page.setName(data.getName());
			page.setBody(data.getBody());
		}
		return data;
	}

	@DeleteMapping("/pages/{id}") // Удаление страницы
	public void destroy(@PathVariable String id) {
		pages.removeIf(p -> p.getSlug().equals(id));
	}
}
