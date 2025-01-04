package sleep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sleep.dto.SleepSessionDto;
import sleep.dto.SleepSessionResponse;
import sleep.exceptions.SleepPersonNotFoundException;
import sleep.exceptions.SleepSessionNotFoundException;
import sleep.models.SleepPerson;
import sleep.models.SleepSession;
import sleep.models.User;
import sleep.repository.SleepSessionRepository;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.SleepPersonService;
import sleep.service.SleepSessionService;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SleepSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SleepSessionService sessionService;

    @MockBean
    private JwtGenerator jwtGenerator;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SleepSessionRepository sleepSessionRepository;

    @MockBean
    private SleepPersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token = "validToken";
    private String username = "testUser";
    private SleepPerson person;
    private User user;
    private SleepSessionResponse response;
    private SleepSessionDto sessionDto;
    private SleepSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        response = new SleepSessionResponse();

        user = new User();
        user.setUsername(username);

        person = new SleepPerson();
        person.setUser(user);
        user.setPerson(person);

        sessionDto = new SleepSessionDto();
        sessionDto.setId(1);
        sessionDto.setDuration(3600);

        session = new SleepSession();
        session.setPerson(person);
    }

    @Test
    void testGetSleepSessionsByDateSuccess() throws Exception {
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));
        when(personService.getAllSessionsByDateAndPersonId(any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/session/getByDate")
                        .with(csrf()
                        )
                        .cookie(new Cookie("auth_token", token))
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-10")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSleepSessionsByDateExpectInternalServerError() throws Exception {
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));
        when(personService.getAllSessionsByDateAndPersonId(any(), any(), anyInt(), anyInt(), anyInt()))
                .thenThrow(NullPointerException.class);

        mockMvc.perform(get("/api/session/getByDate")
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-10")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSleepSessionsByDateExpectBadRequest() throws Exception {
        String token = null;
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(new User()));
        when(personService.getAllSessionsByDateAndPersonId(any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(get("/api/session/getByDate")
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-10")
                        .param("pageNo", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testCreateSleepSessionSuccess() throws Exception {
        when(sessionService.createSleepSession(any())).thenReturn(sessionDto);
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));


        mockMvc.perform(post("/api/session/create")
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.duration").value(3600));
    }

    @Test
    void testCreateSleepSessionExpectInternalServerError() throws Exception {
        sessionDto.setDate(new Date());

        when(sessionService.createSleepSession(any())).thenThrow(SleepPersonNotFoundException.class);
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));


        mockMvc.perform(post("/api/session/create")
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateSleepSessionExpectBadRequest() throws Exception {
        when(sessionService.createSleepSession(any())).thenReturn(sessionDto);
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenThrow(NullPointerException.class);


        mockMvc.perform(post("/api/session/create")
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateSleepSessionSuccess() throws Exception {
        sessionDto.setStartTime(new Date());
        sessionDto.setEndTime(new Date());

        SleepSession sleepSession = new SleepSession();
        sleepSession.setPerson(person);

        when(sleepSessionRepository.findById(any())).thenReturn(java.util.Optional.of(sleepSession));
        when(sessionService.updateSleepSession(any(), any())).thenReturn(sessionDto);
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(put("/api/session/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto))
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.duration").value(3600));
    }

    @Test
    void testUpdateSleepSessionExpectInternalServerError() throws Exception {
        when(sessionService.updateSleepSession(any(), any())).thenThrow(SleepSessionNotFoundException.class);
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(put("/api/session/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto))
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateSleepSessionExpectBadRequest() throws Exception {
        when(sessionService.updateSleepSession(any(), any())).thenReturn(sessionDto);
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenThrow(NullPointerException.class);

        mockMvc.perform(put("/api/session/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDto))
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteSleepSessionSuccess() throws Exception {
        when(sleepSessionRepository.findById(any())).thenReturn(java.util.Optional.of(session));
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));


        mockMvc.perform(post("/api/session/1/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteSleepSessionExpectInternalServerError() throws Exception {
        doThrow(SleepSessionNotFoundException.class).when(sessionService).deleteSleepSession(any());
        when(sleepSessionRepository.findById(any())).thenReturn(java.util.Optional.of(session));
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenReturn(java.util.Optional.of(user));


        mockMvc.perform(post("/api/session/1/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteSleepSessionExpectUnauthorized() throws Exception {
        when(sleepSessionRepository.findById(any())).thenReturn(java.util.Optional.of(session));
        when(jwtGenerator.getUsernameFromJWT(any())).thenReturn("testUser");
        when(userRepository.findByUsername(any())).thenThrow(NullPointerException.class);


        mockMvc.perform(post("/api/session/1/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isUnauthorized());
    }
}
