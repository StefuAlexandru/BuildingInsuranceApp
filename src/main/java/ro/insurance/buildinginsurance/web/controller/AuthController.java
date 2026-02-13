package ro.insurance.buildinginsurance.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.insurance.buildinginsurance.application.service.impl.AuthService;
import ro.insurance.buildinginsurance.web.dto.auth_dto.AuthLoginRequestDTO;
import ro.insurance.buildinginsurance.web.dto.auth_dto.AuthRefreshRequestDTO;
import ro.insurance.buildinginsurance.web.dto.auth_dto.AuthResponseDTO;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody AuthLoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@Valid @RequestBody AuthRefreshRequestDTO request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody AuthRefreshRequestDTO request) {
        authService.logout(request);
    }
}
