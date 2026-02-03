package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.insurance.buildinginsurance.application.service.IBrokerService;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerRequestDTO;
import ro.insurance.buildinginsurance.web.dto.brokerDto.BrokerResponseDTO;

@RestController
@RequestMapping("/api/admin/brokers")
@RequiredArgsConstructor
public class BrokerController {

    private final IBrokerService brokerService;

    @PostMapping
    public ResponseEntity<BrokerResponseDTO> createBroker(@Valid @RequestBody BrokerRequestDTO request) {
        BrokerResponseDTO response = brokerService.createBroker(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
