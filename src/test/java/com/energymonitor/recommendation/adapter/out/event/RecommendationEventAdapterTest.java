package com.energymonitor.recommendation.adapter.out.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.energymonitor.recommendation.adapter.out.event.RecommendationEventAdapter.RecommendationCreatedMessage;
import com.energymonitor.recommendation.adapter.out.messaging.RecommendationOutboxWriter;
import com.energymonitor.recommendation.domain.model.RecommendationCreated;
import com.energymonitor.recommendation.domain.model.RecommendationType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("Recommendation events through the outbox")
class RecommendationEventAdapterTest {

    private static final Instant AT = Instant.parse("2026-10-23T15:00:00Z");
    private static final Instant NOW = Instant.parse("2026-10-23T15:00:02Z");

    private final RecommendationOutboxWriter outbox = mock(RecommendationOutboxWriter.class);
    private final RecommendationEventAdapter adapter =
            new RecommendationEventAdapter(outbox, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("a recommendation goes to the outbox as recommendation.created, with the body the notification service reads")
    void writesRecommendationCreated() {
        RecommendationCreated event = new RecommendationCreated("rec0000001", "hom0000001", null,
                RecommendationType.PEAK_HOURS, "recommendation.peakHours", AT);

        adapter.publish(event);

        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(outbox).write(eq("recommendation.created"), body.capture(), eq(NOW));
        assertThat(body.getValue()).isEqualTo(new RecommendationCreatedMessage("rec0000001",
                "hom0000001", null, "PEAK_HOURS", "recommendation.peakHours", AT));
    }
}
