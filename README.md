# Job Title Normaliser

A small Java library that maps free-text job titles to the closest of a fixed set of target titles
("Architect", "Software engineer", "Quantity surveyor", "Accountant"). For example, "Java engineer"
becomes "Software engineer" and "Chief Accountant" becomes "Accountant". Internally, each target is
given a quality score q between 0.0 and 1.0, and the best-scoring target wins.

## Build and test

Requires JDK 25. The Maven wrapper downloads Maven itself.

```sh
./mvnw test           # run the unit tests
./mvnw clean package  # build the jar into target/
./mvnw -q compile exec:java  # print the normalised title for a few example inputs
./mvnw -q compile exec:java -Dexec.args="'Civil engineer' Barista"  # or for your own inputs
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Usage

```java
Normaliser n = new Normaliser();
String input = "Java engineer";
String normalisedTitle = n.normalise(input);
```

## Design

`Normaliser` owns selection: it asks a `ScoringStrategy` for q against each target title, picks the
highest, and applies the tie and no-match rules. The strategy owns scoring and nothing else. A new
matching algorithm is a new `ScoringStrategy` implementation passed to
`Normaliser(List<String> targetTitles, ScoringStrategy strategy)`. This split has a few benefits.
First, it makes the code testable: `Normaliser`'s selection rules are pinned with stub strategies,
and each strategy's scoring is tested on its own. Second, strategies can be composed and decorated.
Third, strategies can be compared against each other while `Normaliser` itself doesn't change.

Strategies chain rather than grow features: a strategy can wrap another and change the input before
passing it on. The default is `new WordMappingStrategy(new LevenshteinWordStrategy())`.
`WordMappingStrategy` adds the target words that specialisms, synonyms and abbreviations stand for
("Python" adds "software", "QS" adds "quantity surveyor"), and `LevenshteinWordStrategy` does the
scoring.

## Testing

- `NormaliserTest` covers the brief's examples and other typical titles end to end, and pins the
  selection rules (highest q wins, ties, no match, null and blank input, configuration errors) with
  stub strategies.
- `LevenshteinWordStrategyTest` covers word scoring: case, word order, separators, typos and
  plurals, and where the similarity floor cuts off.
- `WordMappingStrategyTest` covers how mapped words are added to the input, constructor validation,
  and the default mapping's specialisms, synonyms and abbreviations.

## Scope

The brief gives four target titles and four examples. The solution aims to be a clean, extensible
structure that handles those and the obvious variations of them: case, word order, punctuation,
extra words such as seniority, typos and plurals, and a small set of specialisms, synonyms and
abbreviations.

Matching is lexical: it compares words, not meanings. A title that shares a generic word with a
target matches that target, so "Civil engineer" and "Property developer" both become "Software
engineer", and "Quality manager" is close enough in spelling to become "Quantity surveyor". Telling
these apart needs domain knowledge, such as a curated dictionary of job titles, a stemmer or semantic
matching. Each of these should be chosen and tuned against a labelled sample of real titles, since a
rule that fixes one title can break others. They are left out until such data exists, and each would
be a new `ScoringStrategy` or mapping, with no change to `Normaliser`.

## Assumptions

- "Best match" means the target with the highest q. If no target scores above 0.0, `normalise`
  returns "" rather than null or an exception, so the brief's `String` signature and sample code
  work unchanged.
- Target list order sets the priority on ties: when several targets share the highest q, the first
  in the list wins, so the result is deterministic.
- Null or blank input returns "", the same as no match, rather than throwing: free-text fields are
  often empty, and normalising one shouldn't crash. `Normaliser` checks this before scoring, so
  strategies never see null or blank input.
- An empty target list or a blank target title is a configuration error, so the `Normaliser`
  constructor rejects it straight away rather than returning "" for every input later.
- A word within roughly one or two typos of a target word counts as that word, so "Acountant" and
  "Architects" both match. Precisely, word similarity is 1 - edit distance / longer word length,
  and anything below 0.75 counts as no match. This catches some related words but not others:
  "Architecture" matches Architect and "Accounting" matches Accountant, but "Engineering" doesn't
  match engineer.
- The default word mapping (for example, Python, Frontend and SWE to software engineer, QS to
  quantity surveyor, ACCA to accountant) shows the mechanism rather than covering the domain.
  Input words must match a mapping key exactly; typos in them aren't tolerated.
- "Developer" alone maps to "engineer" rather than "software engineer", because it can also mean a
  property developer. "Python developer" and "Software developer" score a full match, while
  "Developer" alone scores 0.5, the same as "Engineer" alone.
- Text handling is English-only: lowercasing uses `Locale.ROOT`, and accents aren't folded.

## Decisions

- [ADR 0001](docs/adr/0001-use-commons-text-for-fuzzy-matching.md): use Apache Commons Text's
  Levenshtein distance for fuzzy matching.

## Next steps

- Collect a labelled sample of real titles. Then, build an evaluation harness that can test
  how changes affect the whole scope. Use the harness to to tune the similarity floor and to choose
  between richer strategies such as a title dictionary, a stemmer or semantic matching.
- The no-match threshold is fixed in `Normaliser` (q > 0.0), so each strategy must return exactly
  0.0 for no match. A shared, configurable threshold may be worth adding if more strategies need
  one. A QualityScore should also be returned instead of a double; that would prevent having to 
  throw an error from the normaliser, as errors would have to be handled inside the ScoringStrategy.
