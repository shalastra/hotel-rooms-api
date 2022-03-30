package com.example.hotelroomapi;

import com.example.hotelroomapi.booking.BookingConfiguration;
import com.example.hotelroomapi.booking.RoomOccupancyManagerController;
import com.example.hotelroomapi.request.RequestedRooms;
import com.example.hotelroomapi.booking.RoomAllocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RoomOccupancyManagerController.class})
@ContextConfiguration(classes = {BookingConfiguration.class})
class HotelRoomApiApplicationTests {

    private static final String TEST_BIDS = "[23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]";

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @MethodSource
    @DisplayName("Given occupancy requirement, when request is sent, then return optimal room allocation")
    void provideOptimalRoomAllocation(RequestedRooms requestedRooms,
                                      RoomAllocation expectedResponse) throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("premium", String.valueOf(requestedRooms.premium()))
                        .param("economy", String.valueOf(requestedRooms.economy()))
                        .content(TEST_BIDS))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponse)
                .isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedResponse));
    }

    private static Stream<Arguments> provideOptimalRoomAllocation() {
        Arguments test1 = arguments(new RequestedRooms(3, 3),
                new RoomAllocation(3, new BigDecimal("738"), 3, new BigDecimal("167.99")));
        /*
          Invalid arguments, in contradiction to the provided business documentation:
          Since the request is 7 premium rooms and 5 economy and provided list of bids has only six above 100 (155, 374, 100, 101, 115, 209),
          it means to fill one last empty premium room hotel has to upgrade a client with the highest bid (99.99)
          Then we have 3 bids under 100 (23, 45, 22).
          Based on described understanding, the correct results should be: 7 and 1153.99 for premium and 3 and 90 for economy

          It was not defined how the API should behave is a such situation - I decided to correct tests.
         */
        Arguments test2 = arguments(new RequestedRooms(7, 5),
                new RoomAllocation(7, new BigDecimal("1153.99"), 3, new BigDecimal("90")));
        /*
          A bit different issue here. The provided input is incorrect
         */
        Arguments test3 = arguments(new RequestedRooms(2, 7),
                new RoomAllocation(2, new BigDecimal("583"), 4, new BigDecimal("189.99")));

        /*
          Similar issue was here, there is no possibility to receive the price for economy equal to 45.99 (no matching element in the list),
          however, the 0.99 was missing in the premium sum
          As in the previous comment, I corrected the test.
         */
        Arguments test4 = arguments(new RequestedRooms(7, 1),
                new RoomAllocation(7, new BigDecimal("1153.99"), 1, new BigDecimal(45)));

        return Stream.of(test1, test2, test3, test4);
    }
}
