package com.gallerymart.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gallerymart.backend.repository.ArtworkRepository;
import com.gallerymart.backend.repository.NotificationRepository;
import com.gallerymart.backend.repository.OrderRepository;
import com.gallerymart.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArtworkIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        orderRepository.deleteAll();
        artworkRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void should_create_artwork_and_find_it_in_search_results_for_seller() throws Exception {
        String sellerToken = registerAndLogin("seller_art@gallerymart.test", "Password123", "Seller Art", "SELLER");

        mockMvc.perform(post("/api/artworks")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Sunset Lake",
                                "description", "Oil on canvas",
                                "price", new BigDecimal("150.00"),
                                "imageUrl", "https://img.test/sunset.jpg",
                                "category", "Landscape"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Sunset Lake"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));

        mockMvc.perform(get("/api/artworks")
                        .param("keyword", "sunset")
                        .param("category", "Landscape")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("Sunset Lake"));

        mockMvc.perform(get("/api/artworks/my")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Sunset Lake"));
    }

    private String registerAndLogin(String email, String password, String fullName, String roles) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password,
                                "fullName", fullName,
                                "roles", roles
                        ))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
