# 1. Use Apache Commons Text for fuzzy matching

## Context

Input titles contain typos and plurals ("Acountant", "Architects") that exact word matching
misses. Writing our own edit-distance algorithm would add code to test and maintain that isn't
specific to this problem. A quick web search turns up many mature libraries for fuzzy string
matching.

## Decision

Use `LevenshteinDistance` from Apache Commons Text. This is a time-boxed spike, and we did not
compare the alternatives. We chose Commons Text because it is maintained by the Apache Software
Foundation and widely used, so it is likely to stay supported and familiar to reviewers. It is
also small: one class covers what we need, and it brings in only `commons-lang3`. Levenshtein
distance is a standard measure whose results are easy to check by hand.

## Consequences

- One new runtime dependency (`commons-text`, plus `commons-lang3` transitively).
- The choice of library, and of edit distance over other similarity measures, has not been
  evaluated and should be revisited as the project grows or once real data is available.
