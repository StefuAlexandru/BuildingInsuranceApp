package ro.insurance.buildinginsurance.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.insurance.buildinginsurance.application.security.AppSecurityProps;
import ro.insurance.buildinginsurance.application.security.jwt.JwtProperties;
import ro.insurance.buildinginsurance.application.security.jwt.JwtService;
import ro.insurance.buildinginsurance.application.service.impl.AuthService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ro.insurance.buildinginsurance.util.ExceptionAssertions.assertHasError;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock JwtService jwtService;
    @Mock JwtProperties jwtProps;
    @Mock AppSecurityProps props;
    @Mock BrokerRepository brokerRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks AuthService authService;

    private final String adminEmail = "admin@email.com";
    private final String brokerEmail = "broker@email.com";

    @BeforeEach
    void setUp() {
        AppSecurityProps.Admin admin = mock(AppSecurityProps.Admin.class);
        lenient().when(admin.username()).thenReturn(adminEmail);
        lenient().when(admin.password()).thenReturn("admin-pass");
        lenient().when(props.admin()).thenReturn(admin);

        AppSecurityProps.Broker broker = mock(AppSecurityProps.Broker.class);
        lenient().when(broker.password()).thenReturn("broker-pass");
        lenient().when(props.broker()).thenReturn(broker);

        lenient().when(jwtProps.refreshDays()).thenReturn(14L);
    }

    @Test
    void login_admin_validCredentials_returnsTokens_andSavesRefresh() {
        var request = new AuthLoginRequestDTO(adminEmail, "admin-pass");

        when(jwtService.generateAccessToken(adminEmail, List.of("ADMIN"))).thenReturn("access-token");

        ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponseDTO response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertNotNull(response.refreshToken());
        assertFalse(response.refreshToken().isBlank());

        verify(jwtService).generateAccessToken(adminEmail, List.of("ADMIN"));
        verify(brokerRepository, never()).existsByEmailIgnoreCase(anyString());

        verify(refreshTokenRepository).save(rtCaptor.capture());
        RefreshToken saved = rtCaptor.getValue();
        assertEquals(adminEmail, saved.getEmail());
        assertEquals(response.refreshToken(), saved.getToken());
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(OffsetDateTime.now().minusSeconds(1)));
    }

    @Test
    void login_admin_invalidPassword_throwsInvalidCredentials_andDoesNotSaveRefresh() {
        var request = new AuthLoginRequestDTO(adminEmail, "wrong");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertHasError(ex, ErrorCode.INVALID_CREDENTIALS);

        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(refreshTokenRepository, never()).save(any());
        verify(brokerRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void login_broker_validCredentials_returnsTokens_andSavesRefresh() {
        var request = new AuthLoginRequestDTO(brokerEmail, "broker-pass");

        when(brokerRepository.existsByEmailIgnoreCase(brokerEmail)).thenReturn(true);
        when(jwtService.generateAccessToken(brokerEmail, List.of("BROKER")))
                .thenReturn("access-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponseDTO response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertNotNull(response.refreshToken());

        verify(brokerRepository).existsByEmailIgnoreCase(brokerEmail);
        verify(jwtService).generateAccessToken(brokerEmail, List.of("BROKER"));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_broker_missingBroker_throwsInvalidCredentials() {
        var request = new AuthLoginRequestDTO(brokerEmail, "broker-pass");
        when(brokerRepository.existsByEmailIgnoreCase(brokerEmail)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertHasError(ex, ErrorCode.INVALID_CREDENTIALS);

        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_broker_wrongPassword_throwsInvalidCredentials() {
        var request = new AuthLoginRequestDTO(brokerEmail, "wrong");
        when(brokerRepository.existsByEmailIgnoreCase(brokerEmail)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertHasError(ex, ErrorCode.INVALID_CREDENTIALS);

        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_whenTokenNotFound_throwsInvalidRefreshToken() {
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.empty());

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("rt");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refresh(request));
        assertHasError(ex, ErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(refreshTokenRepository, never()).save(any());
        verify(brokerRepository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void refresh_whenRevoked_throwsInvalidRefreshToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("rt").email(brokerEmail)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(refreshToken));

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("rt");
        BusinessException ex = assertThrows(BusinessException.class,() -> authService.refresh(request));
        assertHasError(ex, ErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_whenExpired_throwsInvalidRefreshToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("rt").email(brokerEmail)
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(refreshToken));

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("rt");
        BusinessException ex = assertThrows(BusinessException.class,() -> authService.refresh(request));
        assertHasError(ex, ErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_broker_whenBrokerMissing_throwsInvalidRefreshToken_andDoesNotRotate() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("rt").email(brokerEmail)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(refreshToken));
        when(brokerRepository.existsByEmailIgnoreCase(brokerEmail)).thenReturn(false);

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("rt");
        BusinessException ex = assertThrows(BusinessException.class,() -> authService.refresh(request));
        assertHasError(ex, ErrorCode.INVALID_REFRESH_TOKEN);

        verify(refreshTokenRepository, never()).save(any()); // no revoke, no new token
        verify(jwtService, never()).generateAccessToken(anyString(), anyList());
    }

    @Test
    void refresh_broker_valid_rotatesRefresh_andReturnsNewTokens() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("oldRt").email(brokerEmail)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("oldRt")).thenReturn(Optional.of(refreshToken));
        when(brokerRepository.existsByEmailIgnoreCase(brokerEmail)).thenReturn(true);
        when(jwtService.generateAccessToken(brokerEmail, List.of("BROKER")))
                .thenReturn("new-access");

        // capture saves (should be 2: revoke old + save new)
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("oldRt");
        AuthResponseDTO response = authService.refresh(request);

        assertEquals("new-access", response.accessToken());
        assertNotNull(response.refreshToken());
        assertNotEquals("oldRt", response.refreshToken());

        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshToken> saved = captor.getAllValues();

        // first save: old revoked
        assertEquals("oldRt", saved.get(0).getToken());
        assertTrue(saved.get(0).isRevoked());

        // second save: new token not revoked
        assertEquals(brokerEmail, saved.get(1).getEmail());
        assertEquals(response.refreshToken(), saved.get(1).getToken());
        assertFalse(saved.get(1).isRevoked());
    }

    @Test
    void refresh_admin_valid_rotatesRefresh_andReturnsAdminAccess() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("oldRt").email(adminEmail)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("oldRt")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(adminEmail, List.of("ADMIN"))).thenReturn("admin-access");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("oldRt");
        AuthResponseDTO response = authService.refresh(request);

        assertEquals("admin-access", response.accessToken());
        assertNotNull(response.refreshToken());

        verify(brokerRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    void logout_whenTokenExists_marksRevoked() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("rt").email(brokerEmail)
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("rt");
        authService.logout(request);

        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void logout_whenTokenMissing_doesNothing() {
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.empty());

        AuthRefreshRequestDTO request = new AuthRefreshRequestDTO("rt");
        authService.logout(request);

        verify(refreshTokenRepository).findByToken("rt");
        verify(refreshTokenRepository, never()).save(any());
    }
}
