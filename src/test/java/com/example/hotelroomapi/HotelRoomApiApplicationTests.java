package com.example.hotelroomapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class HotelRoomApiApplicationTests {

    private static final List<BigDecimal> someInput = Stream
            .of(23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209)
            .map(v -> new BigDecimal(String.valueOf(v))).toList();

    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    record RequestedRooms(int premium, int economy){};

    @ParameterizedTest
    @MethodSource
    @DisplayName("Given occupancy requirement, when request is sent, then return optimal room allocation")
    void provideOptimalRoomAllocation(RequestedRooms requestedRooms,
                                      Map<String, Map<Integer, Integer>> response) throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("premium", String.valueOf(requestedRooms.premium()))
                        .param("economy", String.valueOf(requestedRooms.economy()))
                        .content(objectMapper.writeValueAsString(someInput)))
                .andExpect(status().isOk())
                .andReturn();

        String actualResult = mvcResult.getResponse().getContentAsString();

        assertThat(actualResult)
                .isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(response));
    }

    private static Stream<Arguments> provideOptimalRoomAllocation() {
        return Stream.of(
                arguments(new RequestedRooms(3,3),
                        Map.of("premium", Map.of(3, 738),
                                "economy", Map.of(3, 167.99))),
                arguments(new RequestedRooms(7,5),
                        Map.of("premium", Map.of(6, 1054),
                                "economy", Map.of(4, 189.99))),
                arguments(new RequestedRooms(2,7),
                        Map.of("premium", Map.of(2, 583),
                                "economy", Map.of(4, 189.99))),
                arguments(new RequestedRooms(7,1),
                        Map.of("premium", Map.of(7, 1153),
                                "economy", Map.of(1, 45))));
    }
}
