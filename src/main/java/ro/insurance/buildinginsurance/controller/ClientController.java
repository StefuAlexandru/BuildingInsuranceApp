package ro.insurance.buildinginsurance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.dto.clientDto.*;
import ro.insurance.buildinginsurance.service.IClientService;

@RestController
@RequestMapping("/api/brokers/clients")
@RequiredArgsConstructor
public class ClientController {
    private final IClientService clientService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@RequestBody @Valid ClientRequestDTO clientRequestDTO){
        ClientResponseDTO clientResponseDTO = clientService.createClient(clientRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientResponseDTO);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ClientResponseDTO>> searchClients(
            @RequestBody ClientSearchRequestDTO clientSearchRequestDTO,
            @PageableDefault(size = 5, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(clientService.searchClients(clientSearchRequestDTO.name(), clientSearchRequestDTO.identificationNumber(), pageable));
    }

    @PostMapping("/lookup")
    public ResponseEntity<ClientResponseDTO> lookupClient(
            @RequestBody @Valid ClientLookupRequestDTO request
    ) {
        return ResponseEntity.ok(
                clientService.getClientByNameAndIdentificationNumber(request.name(), request.identificationNumber())
        );
    }


    // ----------------------  GET  ----------------------

    @GetMapping("/{clientId}"   )
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long clientId){
        ClientResponseDTO clientResponseDTO = clientService.getClientById(clientId);
        return ResponseEntity.ok(clientResponseDTO);
    }

    @GetMapping("/{clientId}/details")
    public ResponseEntity<ClientDetailsResponseDTO> getClientDetails(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientService.getClientDetailsById(clientId));
    }

    @GetMapping
    public ResponseEntity<Page<ClientResponseDTO>> getAllClients(
            @PageableDefault(size = 5, sort = "name") Pageable pageable){
        return ResponseEntity.ok(clientService.getAllClients(pageable));
    }


    // ----------------------  PUT  ----------------------

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long clientId,
            @RequestBody @Valid ClientUpdateRequestDTO clientUpdateRequestDTO
    ){
        ClientResponseDTO clientResponseDTO = clientService.updateClient(clientId, clientUpdateRequestDTO);
        return ResponseEntity.ok(clientResponseDTO);
    }

    // ----------------------  PATCH  ----------------------

    @PatchMapping("/{clientId}/identity")
    public ResponseEntity<ClientResponseDTO> patchClientIdentity(
            @PathVariable Long clientId,
            @RequestBody @Valid ClientIdentityPatchDTO clientIdentityPatchDTO
    ) {
        String changedBy = "system";
        ClientResponseDTO clientResponseDTO = clientService.patchIdentity(clientId,clientIdentityPatchDTO,changedBy);
        return  ResponseEntity.ok(clientResponseDTO);
    }

    // ----------------------  DELETE  ----------------------

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long clientId){
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }


}
