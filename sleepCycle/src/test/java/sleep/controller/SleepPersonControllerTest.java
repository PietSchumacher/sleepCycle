package sleep.controller;

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
import sleep.dto.SleepPersonDto;
import sleep.models.SleepPerson;
import sleep.models.User;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.SleepPersonService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SleepPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SleepPersonService personService;

    @MockBean
    private JwtGenerator jwtGenerator;

    @MockBean
    private UserRepository userRepository;

    private int personId = 1;
    private String token = "validToken";
    private String username = "testUser";
    private SleepPersonDto updatedPersonDto;
    private SleepPerson person;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(jwtGenerator.getUsernameFromJWT(token)).thenReturn(username);

        updatedPersonDto = new SleepPersonDto();
        updatedPersonDto.setId(personId);
        updatedPersonDto.setName("Updated Name");

        person = new SleepPerson();
        person.setId(personId);

        user = new User();
        user.setPerson(person);
    }

    @Test
    void updateSleepPersonSuccess() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(personService.updateSleepPerson(any(SleepPersonDto.class), eq(personId))).thenReturn(updatedPersonDto);

        mockMvc.perform(put("/api/person/{id}/update", personId)
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId))
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(personService, times(1)).updateSleepPerson(any(SleepPersonDto.class), eq(personId));
    }

    @Test
    void updateSleepPersonUserNotFound() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/person/{id}/update", personId)
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSleepPersonExpectBadRequest() throws Exception {
        String token = null;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/person/{id}/update", personId)
                        .with(csrf())
                        .cookie(new Cookie("auth_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSleepPersonSuccess() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/person/{id}/delete", personId)
                        .with(csrf())
                        .with(user("testUser").roles("USER"))
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isOk())
                .andExpect(content().string("Person wurde erfolgreich gelöscht"));

        verify(personService, times(1)).deleteSleepPerson(personId);
    }

    @Test
    void deleteSleepPersonUserNotFound() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/person/{id}/delete", personId)
                        .with(csrf())
                        .with(user("testUser").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Der Benutzer konnte nicht gefunden werden"));
    }

    @Test
    void deleteSleepPersonException() throws Exception {
        when(userRepository.findByUsername(username)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/person/{id}/delete", personId)
                        .with(csrf())
                        .with(user("testUser").roles("USER"))
                        .cookie(new Cookie("auth_token", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Die Person konnte nicht gelöscht werden"));
    }
}
