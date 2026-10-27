package com.energymonitor.recommendation.domain.model;

/**
 * Whether a member already read a recommendation. Mirrors the {@code recommendation.status} column.
 */
public enum RecommendationStatus {
    READ,
    UNREAD
}
