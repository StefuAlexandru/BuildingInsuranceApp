package ro.insurance.buildinginsurance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ro.insurance.buildinginsurance.dto.clientDto.ClientRequestDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientResponseDTO;
import ro.insurance.buildinginsurance.dto.clientDto.ClientUpdateRequestDTO;
import ro.insurance.buildinginsurance.error.CustomExceptionHandler;
import ro.insurance.buildinginsurance.service.IClientService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    private String createClientRequestBody(String name, String clientType, String identificationNumber, String email, String phone, Long addressCityId, String addressStreet, String addressNumber) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("clientType", clientType);
        body.put("identificationNumber", identificationNumber);
        body.put("email", email);
        body.put("phone", phone);
        body.put("addressCityId", addressCityId);
        body.put("addressStreet", addressStreet);
        body.put("addressNumber", addressNumber);
        return objectMapper.writeValueAsString(body);
    }

    private String createClientUpdateRequestBody(String name, String email, String phone, Long addressCityId, String addressStreet, String addressNumber) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("phone", phone);
        body.put("addressCityId", addressCityId);
        body.put("addressStreet", addressStreet);
        body.put("addressNumber", addressNumber);
        return objectMapper.writeValueAsString(body);
    }

    private String createPatchIdentityBody(String newClientType, String newIdentificationNumber, String reason) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("newClientType", newClientType);
        body.put("newIdentificationNumber", newIdentificationNumber);
        body.put("reason", reason);
        return objectMapper.writeValueAsString(body);
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
        String body = createClientRequestBody("Gheo", "INDIVIDUAL", "1960101123456", "bad-email", "abc", 1L, "Str", "10A");

        // Act + Assert
        mockMvc.perform(post("/api/brokers/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clientService);
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
    void getClientByNameAndIdentificationNumber_shouldReturn200() throws Exception {
        // Arrange
        ClientResponseDTO clientResponseDTO = aClientResponse();

        when(clientService.getClientByNameAndIdentificationNumber("Gheo", "1960101123456")).thenReturn(clientResponseDTO);

        // Act + Assert
        mockMvc.perform(get("/api/brokers/clients/search")
                        .param("name", "Gheo")
                        .param("identificationNumber", "1960101123456"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.identificationNumber").value(clientResponseDTO.identificationNumber())
                );

        verify(clientService).getClientByNameAndIdentificationNumber("Gheo", "1960101123456");
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
        // Arrange
        Long clientId = 99L;
        ClientResponseDTO clientResponseDTO = aClientResponse();

        String body = createPatchIdentityBody("INDIVIDUAL", "NEW-CNP", "reason");

        when(clientService.patchIdentity(eq(clientId), any(), eq("system"))).thenReturn(clientResponseDTO);

        // Act + Assert
        mockMvc.perform(patch("/api/brokers/clients")
                        .param("clientId", clientId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clientResponseDTO.id().intValue()),
                        jsonPath("$.clientType").value(clientResponseDTO.clientType().name())
                );

        verify(clientService).patchIdentity(eq(clientId), any(), eq("system"));
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