package csd230.lab1.controllers;

import csd230.lab1.entities.DiscMagEntity;
import csd230.lab1.repositories.DiscMagEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DiscMags", description = "JSON API for managing disc magazines")
@RestController
@RequestMapping("/api/rest/discmags")
public class DiscMagRestController {
    private final DiscMagEntityRepository repository;

    public DiscMagRestController(DiscMagEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List disc magazines")
    @ApiResponse(responseCode = "200", description = "Disc magazines returned")
    public List<DiscMagEntity> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a disc magazine")
    @ApiResponse(responseCode = "200", description = "Disc magazine returned")
    @ApiResponse(responseCode = "404", description = "Disc magazine not found")
    public DiscMagEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new DiscMagNotFoundException(id));
    }

    @PostMapping
    @Operation(summary = "Create a disc magazine")
    @ApiResponse(responseCode = "200", description = "Disc magazine created")
    public DiscMagEntity create(@RequestBody DiscMagEntity discMag) {
        return repository.save(discMag);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a disc magazine", description = "Replaces a disc magazine, creating it if the ID does not exist")
    @ApiResponse(responseCode = "200", description = "Disc magazine replaced or created")
    public DiscMagEntity replace(@RequestBody DiscMagEntity replacement, @PathVariable Long id) {
        replacement.setId(id);
        return repository.findById(id)
                .map(discMag -> {
                    discMag.setTitle(replacement.getTitle());
                    discMag.setPrice(replacement.getPrice());
                    discMag.setCopies(replacement.getCopies());
                    discMag.setOrderQty(replacement.getOrderQty());
                    discMag.setCurrentIssue(replacement.getCurrentIssue());
                    discMag.setHasDisc(replacement.getHasDisc());
                    return repository.save(discMag);
                })
                .orElseGet(() -> repository.save(replacement));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a disc magazine")
    @ApiResponse(responseCode = "200", description = "Disc magazine deleted")
    @ApiResponse(responseCode = "404", description = "Disc magazine not found")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new DiscMagNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
