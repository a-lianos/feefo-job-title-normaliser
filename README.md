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

## Design

## Assumptions

## Decisions

## Limits and next steps
