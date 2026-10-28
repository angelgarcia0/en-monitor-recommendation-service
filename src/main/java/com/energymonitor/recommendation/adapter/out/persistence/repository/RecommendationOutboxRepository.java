package com.energymonitor.recommendation.adapter.out.persistence.repository;

import com.energymonitor.recommendation.adapter.out.persistence.entity.RecommendationOutboxEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository over {@code recommendation_outbox}.
 */
@Repository
public interface RecommendationOutboxRepository extends JpaRepository<RecommendationOutboxEntity, String> {

    /**
     * The messages still waiting for the broker, oldest first, so the order in which the
     * recommendations were created is the order in which the notification service hears about
     * them.
     */
    @Query("""
            select m from RecommendationOutboxEntity m
            where m.publishedAt is null and m.discardedAt is null
            order by m.occurredAt asc, m.idMessage asc
            """)
    List<RecommendationOutboxEntity> findPending(Pageable page);
}
