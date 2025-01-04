package sleep.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import sleep.dto.SleepSessionResponse;
import sleep.models.SleepPerson;
import sleep.models.User;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.SleepPersonService;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SleepPersonService personService;

    @MockBean
    private JwtGenerator jwtGenerator;

    private Cookie authCookie = new Cookie("auth_token", "validToken");
    private User mockUser = new User();
    private SleepPerson mockPerson = new SleepPerson();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser.setPerson(mockPerson);
        mockUser.setUsername("testUser");
    }

    @Test
    public void testDisplayHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    public void testDisplayLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("loginPage"));
    }

    @Test
    public void testDisplayRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    public void testDisplayGatherSessionsPageAuthenticated() throws Exception {
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/gatherSleepSessions").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("sessionForm"));
    }

    @Test
    public void testDisplayPersonalOverviewAuthenticated() throws Exception {
        when(personService.getAllSessionsByPersonId(any(Integer.class),any(Integer.class),any(Integer.class))).thenReturn(new SleepSessionResponse());
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        SleepSessionResponse response = new SleepSessionResponse();
        response.setContent(new ArrayList<>());

        mockMvc.perform(get("/personalOverview").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("personalOverview"));
    }

    @Test
    public void testDisplayProfilePageAuthenticated() throws Exception {
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/profile").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    public void testDisplayOptimizationPageAuthenticated() throws Exception {
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/optimization").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("optimization"));
    }

    @Test
    public void testRedirectToLoginPage() throws Exception {
        when(jwtGenerator.getUsernameFromJWT("validToken")).thenThrow(NullPointerException.class);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/optimization").cookie(authCookie))
                .andExpect(view().name("loginPage"));

        mockMvc.perform(get("/personalOverview").cookie(authCookie))
                .andExpect(view().name("loginPage"));

        mockMvc.perform(get("/gatherSleepSessions").cookie(authCookie))
                .andExpect(view().name("loginPage"));

        mockMvc.perform(get("/profile").cookie(authCookie))
                .andExpect(view().name("loginPage"));
    }
}
