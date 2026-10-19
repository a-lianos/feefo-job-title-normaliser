package io.github.alianos.normaliser.scoring;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class Tokeniser {

    private Tokeniser() {}

    static List<String> words(String title) {
        return Arrays.stream(title.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }
}
