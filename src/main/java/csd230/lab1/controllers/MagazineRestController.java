package csd230.lab1.controllers;

import csd230.lab1.entities.MagazineEntity;
import csd230.lab1.repositories.MagazineEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Magazines", description = "JSON API for managing magazines")
@RestController
@RequestMapping("/api/rest/magazines")
public class MagazineRestController {
    private final MagazineEntityRepository repository;

    public MagazineRestController(MagazineEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List magazines")
    @ApiResponse(responseCode = "200", description = "Magazines returned")
    public List<MagazineEntity> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a magazine")
    @ApiResponse(responseCode = "200", description = "Magazine returned")
    @ApiResponse(responseCode = "404", description = "Magazine not found")
    public MagazineEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new MagazineNotFoundException(id));
    }

    @PostMapping
    @Operation(summary = "Create a magazine")
    @ApiResponse(responseCode = "200", description = "Magazine created")
    public MagazineEntity create(@RequestBody MagazineEntity magazine) {
        return repository.save(magazine);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a magazine", description = "Replaces a magazine, creating it if the ID does not exist")
    @ApiResponse(responseCode = "200", description = "Magazine replaced or created")
    public MagazineEntity replace(@RequestBody MagazineEntity replacement, @PathVariable Long id) {
        replacement.setId(id);
        return repository.findById(id)
                .map(magazine -> {
                    magazine.setTitle(replacement.getTitle());
                    magazine.setPrice(replacement.getPrice());
                    magazine.setCopies(replacement.getCopies());
                    magazine.setOrderQty(replacement.getOrderQty());
                    magazine.setCurrentIssue(replacement.getCurrentIssue());
                    return repository.save(magazine);
                })
                .orElseGet(() -> repository.save(replacement));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a magazine")
    @ApiResponse(responseCode = "200", description = "Magazine deleted")
    @ApiResponse(responseCode = "404", description = "Magazine not found")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new MagazineNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
