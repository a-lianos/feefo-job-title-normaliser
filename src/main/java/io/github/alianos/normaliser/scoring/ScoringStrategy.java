package io.github.alianos.normaliser.scoring;

/**
 * Scores how well a free-text input title matches a target title.
 */
@FunctionalInterface
public interface ScoringStrategy {

    /**
     * Returns the quality score q, from 0.0 (no match) to 1.0 (perfect match).
     */
    double score(String inputTitle, String targetTitle);
}
