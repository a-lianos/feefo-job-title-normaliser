package io.github.alianos.normaliser;

import java.util.List;

/** Prints the normalised title for a few example input titles. */
public class Main {

    private static final List<String> EXAMPLE_INPUT_TITLES = List.of(
            "Java engineer",
            "C# engineer",
            "Chief Accountant",
            "Acountant",
            "Engineer, Software",
            "Architects",
            "quantity surveyor",
            "Gardener",
            "Software Architect",
            "");

    public static void main(String[] args) {
        Normaliser normaliser = new Normaliser();
        for (String inputTitle : EXAMPLE_INPUT_TITLES) {
            String normalisedTitle = normaliser.normalise(inputTitle);
            System.out.printf("%-22s -> %s%n", "\"" + inputTitle + "\"",
                    normalisedTitle.isEmpty() ? "(no match)" : normalisedTitle);
        }
    }
}
