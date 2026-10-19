package io.github.alianos.normaliser.scoring;

import static io.github.alianos.normaliser.scoring.Tokeniser.words;

import java.util.List;
import java.util.Set;

/**
 * Scores q as the fraction of the target's words that appear in the input, ignoring case.
 * Anything other than a letter or digit separates words.
 */
public class WordMatchStrategy implements ScoringStrategy {

    @Override
    public double score(String inputTitle, String targetTitle) {
        Set<String> inputWords = Set.copyOf(words(inputTitle));
        List<String> targetWords = words(targetTitle);
        if (targetWords.isEmpty()) {
            return 0.0;
        }
        long matched = targetWords.stream().filter(inputWords::contains).count();
        return (double) matched / targetWords.size();
    }
}
