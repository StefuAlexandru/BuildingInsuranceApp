package ro.insurance.buildinginsurance.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ro.insurance.buildinginsurance.dto.clientDto.ClientDetailsResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.enums.ClientType;
import ro.insurance.buildinginsurance.error.CustomExceptionHandler;
import ro.insurance.buildinginsurance.service.IClientService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ro.insurance.buildinginsurance.util.ClientTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IClientService clientService;

    @InjectMocks
    private ClientController clientController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }


    // ---------------------  CREATE  ---------------------


    @Test
    void createClient_shouldReturn201AndResponseBody() throws Exception {
        // Arrange
        ClientRequestDTO clientRequestDTO = aClientRequest();
        ClientResponseDTO clientResponseDTO = aClientResponse();

        String body = createClientRequestBody(
                clientRequestDTO.name(), clientRequestDTO.clientType(), clientRequestDTO.identificationNumber(),
                clientRequestDTO.email(), clientRequestDTO.phone(),
                clientRequestDTO.addressCityId(), clientRequestDTO.addressStreet(), clientRequestDTO.addressNumber()
        );

        when(clientService.createClient(any(ClientRequestDTO.class))).thenReturn(clientResponseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.clientType").value(clientResponseDTO.clientType().name()),
                        jsonPath("$.name").value(clientResponseDTO.name()),
                        jsonPath("$.identificationNumber").value(clientResponseDTO.identificationNumber()),
                        jsonPath("$.email").value(clientResponseDTO.email()),
                        jsonPath("$.phone").value(clientResponseDTO.phone()),
                        jsonPath("$.addressStreet").value(clientResponseDTO.addressStreet()),
                        jsonPath("$.addressNumber").value(clientResponseDTO.addressNumber()),
                        jsonPath("$.city.id").value(clientResponseDTO.city().id().intValue()),
                        jsonPath("$.city.name").value(clientResponseDTO.city().name()),
                        jsonPath("$.city.countyName").value(clientResponseDTO.city().countyName()),
                        jsonPath("$.city.countryName").value(clientResponseDTO.city().countryName())
                );

        verify(clientService).createClient(any(ClientRequestDTO.class));
        verifyNoMoreInteractions(clientService);
    }

    @Test
    void createClient_shouldReturn400WhenInvalid() throws Exception {
        // Arrange
        String body = createClientRequestBody("Gheo", ClientType.INDIVIDUAL, "1960101123456", "bad-email", "abc", 1L, "Str", "10A");

        // Act + Assert
        mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clientService);
    }

    @Test
    void searchClients_byPartialName_shouldReturn200_andDefaultPageable() throws Exception {
        // Arrange
        ClientResponseDTO dto = aClientResponse();
        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<ClientResponseDTO> page = new PageImpl<>(List.of(dto), expectedPageable, 1);

        when(clientService.searchClients(eq("ghe"), isNull(), any(Pageable.class))).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // Act + Assert
        mockMvc.perform(post("/api/brokers/clients/search")
                        .contentType("application/json")
                        .content("""
                        {
                          "name": "ghe",
                          "identificationNumber": null
                        }
                        """))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(dto.id().intValue()),
                        jsonPath("$.content[0].identificationNumber").value(dto.identificationNumber()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(clientService).searchClients(eq("ghe"), isNull(), pageableCaptor.capture());

        Pageable used = pageableCaptor.getValue();
        assertAll(
                () -> assertEquals(0, used.getPageNumber()),
                () -> assertEquals(5, used.getPageSize()),
                () -> assertNotNull(used.getSort().getOrderFor("name"))
        );

        verifyNoMoreInteractions(clientService);
    }

    @Test
    void searchClients_byIdentificationNumber_shouldReturn200() throws Exception {
        // Arrange
        ClientResponseDTO dto = aClientResponse();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<ClientResponseDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

        when(clientService.searchClients(isNull(), eq("1960101123456"), any(Pageable.class))).thenReturn(page);

        // Act + Assert
        mockMvc.perform(post("/api/brokers/clients/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .contentType("application/json")
                        .content("""
                        {
                          "name": null,
                          "identificationNumber": "1960101123456"
                        }
                        """))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content[0].id").value(dto.id().intValue()),
                        jsonPath("$.content[0].identificationNumber").value(dto.identificationNumber()),
                        jsonPath("$.totalElements").value(1)
                );

        verify(clientService).searchClients(isNull(), eq("1960101123456"), any(Pageable.class));
        verifyNoMoreInteractions(clientService);
    }

    @Test
    void lookupClient_shouldReturn200() throws Exception {
        // Arrange
        ClientResponseDTO clientResponseDTO = aClientResponse();

        when(clientService.getClientByNameAndIdentificationNumber("Gheo", "1960101123456")).thenReturn(clientResponseDTO);

        // Act + Assert
        mockMvc.perform(post("/api/brokers/clients/lookup")
                        .contentType("application/json")
                        .content("""
                        {
                          "name": "Gheo",
                          "identificationNumber": "1960101123456"
                        }
                        """))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.identificationNumber").value(clientResponseDTO.identificationNumber())
                );

        verify(clientService).getClientByNameAndIdentificationNumber("Gheo", "1960101123456");
        verifyNoMoreInteractions(clientService);
    }


    // --------------------- GET ---------------------


    @Test
    void getClientById_shouldReturn200AndResponseBody() throws Exception {
        // Arrange
        ClientResponseDTO clientResponseDTO = aClientResponse();

        when(clientService.getClientById(1L)).thenReturn(clientResponseDTO);

        // Act + Assert
        mockMvc.perform(get("/api/brokers/clients/{clientId}", 1L))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.name").value(clientResponseDTO.name())
                );

        verify(clientService).getClientById(1L);
        verifyNoMoreInteractions(clientService);
    }

    @Test
    void getAllClients_shouldReturn200AndPageContent() throws Exception {
        // Arrange
        ClientResponseDTO dto = aClientResponse();

        when(clientService.getAllClients(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 5), 1));

        // Act
        var actions = mockMvc.perform(get("/api/brokers/clients")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc")
        );

        // Assert
        actions.andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].id").value(dto.id().intValue()),
                jsonPath("$.content[0].name").value(dto.name())
        );

        verify(clientService).getAllClients(any(Pageable.class));
        verifyNoMoreInteractions(clientService);
    }

    @Test
    void getClientDetails_shouldReturn200_andBody() throws Exception {
        // Arrange
        Long clientId = 1L;
        ClientDetailsResponseDTO dto = aClientDetailsResponse();

        when(clientService.getClientDetailsById(clientId)).thenReturn(dto);

        // Act + Assert
        mockMvc.perform(get("/api/brokers/clients/{clientId}/details", clientId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(dto.id().intValue()),
                        jsonPath("$.name").value(dto.name()),
                        jsonPath("$.identificationNumber").value(dto.identificationNumber()),
                        jsonPath("$.buildings.length()").value(dto.buildings().size()),
                        jsonPath("$.buildings[0].id").value(dto.buildings().getFirst().id().intValue())
                );

        verify(clientService).getClientDetailsById(clientId);
        verifyNoMoreInteractions(clientService);
    }


    // --------------------- UPDATE ---------------------


    @Test
    void updateClient_shouldReturn200AndBody() throws Exception {
        // Arrange
        Long clientId = 99L;
        ClientUpdateRequestDTO clientUpdateRequestDTO = aClientUpdateRequest();
        ClientResponseDTO clientResponseDTO = aClientResponse();

        String body = createClientUpdateRequestBody(
                clientUpdateRequestDTO.name(), clientUpdateRequestDTO.email(), clientUpdateRequestDTO.phone(),
                clientUpdateRequestDTO.addressCityId(), clientUpdateRequestDTO.addressStreet(), clientUpdateRequestDTO.addressNumber()
        );

        when(clientService.updateClient(eq(clientId), any(ClientUpdateRequestDTO.class))).thenReturn(clientResponseDTO);

        // Act + Assert
        mockMvc.perform(put("/api/brokers/clients/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.name").value(clientResponseDTO.name())
                );

        verify(clientService).updateClient(eq(clientId), any(ClientUpdateRequestDTO.class));
        verifyNoMoreInteractions(clientService);
    }


    // --------------------- PATCH ---------------------


    @Test
    void patchClientIdentity_shouldReturn200AndBody() throws Exception {
        Long clientId = 99L;
        ClientResponseDTO clientResponseDTO = aClientResponse();

        String body = createPatchIdentityBody("INDIVIDUAL", "NEW-CNP", "reason");

        var auth = new UsernamePasswordAuthenticationToken(
                "broker",
                "N/A",
                AuthorityUtils.createAuthorityList("ROLE_BROKER")
        );

        when(clientService.patchIdentity(eq(clientId), any(), eq("broker"))).thenReturn(clientResponseDTO);

        mockMvc.perform(patch("/api/brokers/clients/{clientId}/identity", clientId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.clientType").value(clientResponseDTO.clientType().name())
                );

        verify(clientService).patchIdentity(eq(clientId), any(), eq("broker"));
        verifyNoMoreInteractions(clientService);
    }


    // --------------------- DELETE ---------------------


    @Test
    void deleteClient_shouldReturn204() throws Exception {
        Long clientId = 99L;
        doNothing().when(clientService).deleteClient(clientId);

        mockMvc.perform(delete("/api/brokers/clients/{clientId}", clientId))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClient(clientId);
        verifyNoMoreInteractions(clientService);
    }
}