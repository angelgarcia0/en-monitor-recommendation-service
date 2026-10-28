package com.energymonitor.recommendation.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.energymonitor.recommendation.application.exception.RecommendationNotFoundException;
import com.energymonitor.recommendation.application.port.in.RecommendationInbox;
import com.energymonitor.recommendation.application.port.in.RecommendationView;
import com.energymonitor.recommendation.domain.model.RecommendationStatus;
import com.energymonitor.recommendation.domain.model.RecommendationType;
import com.energymonitor.recommendation.infrastructure.JwtKeyedTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Route protection and shape of the recommendation inbox. The requests carry an access token
 * signed with the test key pair, so the real filter chain runs and the caller is the token's
 * subject. Only members of a home see, read or delete its recommendations (the rule lives in the
 * use case, here mocked); a stranger gets a 404 and an unread recommendation cannot be deleted.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerWebTest extends JwtKeyedTest {

    private static final String USER_ID = "use0000001";
    private static final String HOME_ID = "hom0000001";
    private static final String RECOMMENDATION_ID = "rec0000001";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RecommendationInbox inbox;

    private static RecommendationView sample(RecommendationStatus status) {
        return new RecommendationView(RECOMMENDATION_ID, HOME_ID, "DEV0000001", "Fridge",
                RecommendationType.PEAK_HOURS, "recommendation.peak_hours", Instant.parse("2026-10-13T20:00:00Z"),
                status);
    }

    @Test
    @DisplayName("without a token the inbox answers 401")
    void withoutTokenIs401() throws Exception {
        mvc.perform(get("/api/v1/recommendations").param("homeId", HOME_ID))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(inbox);
    }

    @Test
    @DisplayName("list answers 200 with the recommendations of the home, for the token's subject")
    void listReturnsRecommendations() throws Exception {
        given(inbox.list(USER_ID, HOME_ID)).willReturn(List.of(sample(RecommendationStatus.UNREAD)));

        mvc.perform(get("/api/v1/recommendations").param("homeId", HOME_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRecommendation").value(RECOMMENDATION_ID))
                .andExpect(jsonPath("$[0].type").value("PEAK_HOURS"))
                .andExpect(jsonPath("$[0].status").value("UNREAD"));
    }

    @Test
    @DisplayName("list without homeId answers 400")
    void listRequiresHomeId() throws Exception {
        mvc.perform(get("/api/v1/recommendations").header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("list answers 404 when the caller does not belong to the home")
    void listRefusesStranger() throws Exception {
        given(inbox.list(USER_ID, HOME_ID)).willThrow(new RecommendationNotFoundException("home not found"));

        mvc.perform(get("/api/v1/recommendations").param("homeId", HOME_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("home not found"));
    }

    @Test
    @DisplayName("mark read answers 200 with the read recommendation")
    void markReadReturnsRecommendation() throws Exception {
        given(inbox.markRead(USER_ID, RECOMMENDATION_ID)).willReturn(sample(RecommendationStatus.READ));

        mvc.perform(put("/api/v1/recommendations/" + RECOMMENDATION_ID + "/read")
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }

    @Test
    @DisplayName("delete answers 204 for a read recommendation")
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/v1/recommendations/" + RECOMMENDATION_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isNoContent());

        verify(inbox).delete(USER_ID, RECOMMENDATION_ID);
    }

    @Test
    @DisplayName("delete answers 409 when the recommendation is still unread")
    void deleteRefusesUnread() throws Exception {
        willThrow(new IllegalStateException("only a read recommendation can be deleted"))
                .given(inbox).delete(USER_ID, RECOMMENDATION_ID);

        mvc.perform(delete("/api/v1/recommendations/" + RECOMMENDATION_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("delete read answers 200 with how many were deleted")
    void deleteReadReturnsCount() throws Exception {
        given(inbox.deleteRead(USER_ID, HOME_ID)).willReturn(3);

        mvc.perform(delete("/api/v1/recommendations").param("homeId", HOME_ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(3));
    }
}
