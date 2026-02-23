package ro.insurance.buildinginsurance.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.insurance.buildinginsurance.application.security.AppSecurityProps;
import ro.insurance.buildinginsurance.application.security.jwt.JwtProperties;
import ro.insurance.buildinginsurance.application.security.jwt.JwtService;
import ro.insurance.buildinginsurance.domain.entity.RefreshToken;
import ro.insurance.buildinginsurance.domain.enums.ErrorCode;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.BrokerRepository;
import ro.insurance.buildinginsurance.infrastructure.persistance.repository.RefreshTokenRepository;
import ro.insurance.buildinginsurance.web.dto.auth_dto.AuthLoginRequestDTO;
import ro.insurance.buildinginsurance.web.dto.auth_dto.AuthRefreshRequestDTO;
import ro.insurance.buildinginsurance.web.dto.auth_dto.AuthResponseDTO;
import ro.insurance.buildinginsurance.web.error.BusinessException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final JwtProperties jwtProps;
    private final AppSecurityProps props;
    private final BrokerRepository brokerRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String ROLE_ADMIN  = "ADMIN";
    private static final String ROLE_BROKER = "BROKER";

    public AuthResponseDTO login(AuthLoginRequestDTO req) {
        log.info("Login attempt for email: {}", req.email());
        String email = req.email();
        String password = req.password();

        List<String> roles = resolveRolesAndValidate(email, password);

        String access = jwtService.generateAccessToken(email, roles);

        String refresh = newRefreshValue();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refresh)
                .email(email)
                .expiresAt(OffsetDateTime.now().plusDays(jwtProps.refreshDays()))
                .revoked(false)
                .build());

        log.info("Login successful for email: {}, roles: {}", email, roles);

        return new AuthResponseDTO(access, refresh);
    }

    public AuthResponseDTO refresh(AuthRefreshRequestDTO req) {
        log.info("Refresh token attempt with token: {}", req.refreshToken());

        RefreshToken rt = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token attempt: {}", req.refreshToken());
                    return new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            log.warn("Invalid refresh token attempt: {}", req.refreshToken());
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = rt.getEmail();

        // if broker login, check if still exists
        if (!isAdmin(email) && !brokerRepository.existsByEmailIgnoreCase(email)){
            log.warn("Refresh token belongs to non-existing user: {}", email);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // rotate refresh
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        String newRefresh = newRefreshValue();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(newRefresh)
                .email(email)
                .expiresAt(OffsetDateTime.now().plusDays(jwtProps.refreshDays()))
                .revoked(false)
                .build());

        List<String> roles = isAdmin(email) ? List.of(ROLE_ADMIN) : List.of(ROLE_BROKER);
        String access = jwtService.generateAccessToken(email, roles);

        return new AuthResponseDTO(access, newRefresh);
    }

    public void logout(AuthRefreshRequestDTO req) {
        log.info("Logout attempt with refresh token: {}", req.refreshToken());
        refreshTokenRepository.findByToken(req.refreshToken()).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
        log.info("Logout successful for refresh token: {}", req.refreshToken());
    }

    private List<String> resolveRolesAndValidate(String email, String password) {
        if (isAdmin(email)) {
            if (!password.equals(props.admin().password())) {
                log.warn("Invalid admin login attempt for email: {}", email);
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }
            return List.of(ROLE_ADMIN);
        }

        if(!brokerRepository.existsByEmailIgnoreCase(email)) {
            log.warn("Invalid user login attempt for email: {}", email);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!password.equals(props.broker().password())) {
            log.warn("Invalid login attempt for email: {}", email);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return List.of(ROLE_BROKER);
    }

    private boolean isAdmin(String email) {
        return email != null && email.equalsIgnoreCase(props.admin().username());
    }

    private String newRefreshValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
