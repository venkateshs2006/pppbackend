package com.java.ppp.pppbackend.controller;

import com.java.ppp.pppbackend.dto.ClientDTO;
import com.java.ppp.pppbackend.dto.ClientDashboardStatsDTO;

import com.java.ppp.pppbackend.service.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "client Management", description = "client management API")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllclients() {
        return ResponseEntity.ok(clientService.getAllclients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getclient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createclient(@RequestBody ClientDTO dto) {
        return ResponseEntity.ok(clientService.createClient(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateclient(@PathVariable Long id, @RequestBody ClientDTO dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteclient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/stats")
    public ResponseEntity<ClientDashboardStatsDTO> getDashboardStats() {
        ClientDashboardStatsDTO stats = clientService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
