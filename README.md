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
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Usage

```java
Normaliser n = new Normaliser();
String input = "Java engineer";
String normalisedTitle = n.normalise(input);
// output normalisedTitle
```

## Design

## Assumptions

- "Best match" means the target with the highest q. If no target scores above 0.0, `normalise`
  returns "" rather than null or an exception, so the brief's `String` signature and sample code
  work unchanged.
- When several targets share the highest q, the first in target-title order wins, so the result is
  deterministic.

## Decisions

## Limits and next steps
