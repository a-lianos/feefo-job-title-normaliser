package io.github.alianos.normaliser.scoring;

import static io.github.alianos.normaliser.scoring.Tokeniser.words;

import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Scores q as the average, over the target's words, of each word's best similarity to an input
 * word, ignoring case. Word similarity is 1 - Levenshtein distance / longer word length, so typos
 * and plurals still score highly; a similarity below 0.75 counts as 0.
 * Anything other than a letter or digit separates words.
 */
public class LevenshteinWordStrategy implements ScoringStrategy {

    // Without a floor every unrelated word scores a little, so no input would ever be "no match".
    private static final double SIMILARITY_FLOOR = 0.75; //TODO: arbitrarily chosen. Can't be tuned with a proper annotated sample.

    private static final LevenshteinDistance DISTANCE = LevenshteinDistance.getDefaultInstance();

    @Override
    public double score(String inputTitle, String targetTitle) {
        List<String> inputWords = words(inputTitle);
        List<String> targetWords = words(targetTitle);
        if (targetWords.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (String targetWord : targetWords) {
            total += bestSimilarity(targetWord, inputWords);
        }
        return total / targetWords.size();
    }

    private static double bestSimilarity(String targetWord, List<String> inputWords) {
        double best = 0.0;
        for (String inputWord : inputWords) {
            double similarity = similarity(targetWord, inputWord);
            if (similarity >= SIMILARITY_FLOOR && similarity > best) {
                best = similarity;
            }
        }
        return best;
    }

    private static double similarity(String a, String b) {
        int distance = DISTANCE.apply(a, b);
        return 1.0 - (double) distance / Math.max(a.length(), b.length());
    }
}
