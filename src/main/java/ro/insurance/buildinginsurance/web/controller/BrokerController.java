package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.IBrokerService;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.broker_dto.BrokerResponseDTO;

@RestController
@RequestMapping("/api/admin/brokers")
@RequiredArgsConstructor
public class BrokerController {

    private final IBrokerService brokerService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<BrokerResponseDTO> createBroker(@Valid @RequestBody BrokerRequestDTO request) {
        BrokerResponseDTO response = brokerService.createBroker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<BrokerResponseDTO> activateBroker(@PathVariable Long id) {
        BrokerResponseDTO response = brokerService.activateBroker(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<BrokerResponseDTO> deactivateBroker(@PathVariable Long id) {
        BrokerResponseDTO response = brokerService.deactivateBroker(id);
        return ResponseEntity.ok(response);
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{id}")
    public ResponseEntity<BrokerResponseDTO> getBroker(@PathVariable Long id) {
        return ResponseEntity.ok(brokerService.getBroker(id));
    }

    @GetMapping
    public ResponseEntity<Page<BrokerResponseDTO>> getAllBrokers(@PageableDefault(size = 5, sort = "name") Pageable pageable){
        return ResponseEntity.ok(brokerService.getBrokers(pageable));
    }

    // ----------------------  UPDATE  ----------------------

    @PutMapping("/{id}")
    public ResponseEntity<BrokerResponseDTO> updateBroker(
            @PathVariable Long id,
            @Valid @RequestBody BrokerRequestDTO request
    ) {
        return ResponseEntity.ok(brokerService.updateBroker(id,request));
    }


}
