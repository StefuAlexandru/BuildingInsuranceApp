package ro.insurance.buildinginsurance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.dto.cityDto.CityRequestDTO;
import ro.insurance.buildinginsurance.dto.cityDto.CityResponseDTO;
import ro.insurance.buildinginsurance.service.ICityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brokers/cities")
public class CityController {

    private final ICityService cityService;

    // ----------------------  POST  ----------------------

    @PostMapping
    public ResponseEntity<CityResponseDTO> registerCity(@Valid @RequestBody CityRequestDTO cityRequestDTO) {
        CityResponseDTO cityResponseDTO = cityService.registerCity(cityRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cityResponseDTO);
    }

    // ----------------------  GET  ----------------------

    @GetMapping("/{cityId}")
    public ResponseEntity<CityResponseDTO> getCity(@PathVariable Long cityId) {
        CityResponseDTO cityResponseDTO = cityService.getCityById(cityId);
        return ResponseEntity.ok().body(cityResponseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<CityResponseDTO>> getCities(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 5, sort = "name") Pageable pageable)
    {
        String cityName = (name == null) ? "" : name;
        return ResponseEntity.ok().body(cityService.getCityByPartialName(cityName,pageable));
    }

    // ----------------------  PUT  ----------------------

    @PutMapping("/{cityId}")
    public ResponseEntity<CityResponseDTO> updateCity(
            @PathVariable Long cityId,
            @Valid @RequestBody CityRequestDTO cityRequestDTO)
    {
        CityResponseDTO cityResponseDTO = cityService.updateCity(cityId, cityRequestDTO);
        return ResponseEntity.ok().body(cityResponseDTO);
    }

    // ----------------------  DELETE  ----------------------

    @DeleteMapping("/{cityId}")
    public ResponseEntity<Void>  deleteCity(@PathVariable Long cityId) {
        cityService.deleteCity(cityId);
        return ResponseEntity.noContent().build();
    }
}
