package csd230.lab1.controllers;

import csd230.lab1.entities.TicketEntity;
import csd230.lab1.repositories.TicketEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tickets", description = "JSON API for managing tickets")
@RestController
@RequestMapping("/api/rest/tickets")
public class TicketRestController {
    private final TicketEntityRepository repository;

    public TicketRestController(TicketEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List tickets")
    @ApiResponse(responseCode = "200", description = "Tickets returned")
    public List<TicketEntity> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a ticket")
    @ApiResponse(responseCode = "200", description = "Ticket returned")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    public TicketEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PostMapping
    @Operation(summary = "Create a ticket")
    @ApiResponse(responseCode = "200", description = "Ticket created")
    public TicketEntity create(@RequestBody TicketEntity ticket) {
        return repository.save(ticket);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a ticket", description = "Replaces a ticket, creating it if the ID does not exist")
    @ApiResponse(responseCode = "200", description = "Ticket replaced or created")
    public TicketEntity replace(@RequestBody TicketEntity replacement, @PathVariable Long id) {
        replacement.setId(id);
        return repository.findById(id)
                .map(ticket -> {
                    ticket.setDescription(replacement.getDescription());
                    ticket.setPrice(replacement.getPrice());
                    return repository.save(ticket);
                })
                .orElseGet(() -> repository.save(replacement));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a ticket")
    @ApiResponse(responseCode = "200", description = "Ticket deleted")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
