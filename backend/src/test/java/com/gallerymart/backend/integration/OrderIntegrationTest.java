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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

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
    void should_create_order_mark_payment_and_confirm_then_create_notification() throws Exception {
        String sellerToken = registerAndLogin("seller_order@gallerymart.test", "Password123", "Seller Order", "SELLER");
        String buyerToken = registerAndLogin("buyer_order@gallerymart.test", "Password123", "Buyer Order", "BUYER");

        MvcResult createdArtworkResult = mockMvc.perform(post("/api/artworks")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Morning Field",
                                "description", "Acrylic",
                                "price", new BigDecimal("220.00"),
                                "imageUrl", "https://img.test/morning.jpg",
                                "category", "Nature"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        Long artworkId = extractLong(createdArtworkResult, "data", "id");

        MvcResult createdOrderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "artworkId", artworkId,
                                "note", "Please pack carefully"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        Long orderId = extractLong(createdOrderResult, "data", "id");

        mockMvc.perform(patch("/api/orders/{id}/payment-sent", orderId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentSentAt").isNotEmpty());

        mockMvc.perform(patch("/api/orders/{id}/confirm", orderId)
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Order confirmed"));
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

    private Long extractLong(MvcResult result, String parent, String field) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path(parent).path(field).asLong();
    }
}
