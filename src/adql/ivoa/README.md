# README

This directory lists all ADQL tests _for any generic ADQL service_.

On the contrary to the `adql/gavo/` and `adql/roe/` directories, it does not
apply to a specific ADQL service. In other words, all these tests do not
check the query execution itself. They aim to check the query parsing result.
In brief, only the parser is tested. No knowledge of the database or the
underlying service should be tested.

## Organisation

No sub-directory is expected. However, the ADQL validator testing this directory
should be able to validate all test files recursively.

## Test file

XML is the format used for all test files.

Each test file lists several test queries. They are more or less gathered by
category (e.g. `math_functions`) or position in an ADQL query (e.g. `select`).

In order to ensure the order of tests in the validation report, all test files
have a numerical prefix:

- just a number for tests on mandatory grammar or syntax elements
- `O` followed by a number for optional features
- `X` followed by a number for extra queries (e.g. interesting sample queries on
  Obscore at ESO), but still using standard ADQL features.

## Validation

Validation of such test file is performed with a validator based on the
[CDS' ADQL Library (VOLLT)](http://cdsportal.u-strasbg.fr/adqltuto/).

This parser has been chosen because of its standalone aspect: it does not need a
server or a database to parse ADQL queries. It is also possible to configure
exactly which version of ADQL to use and which optional features to enable. It
is supposed to be as independant as possible from any specific service. Finally,
it should follow as closely as possible the IVOA's ADQL specification.

The validator is written in Java 8 or more. Its compilation (and possibly its
execution) is done using Gradle.

Check out the [`adql-validator`](../../adql-validator/) directory for more
details on how to run tests.


## Continuous Integration (CI)

A CI workflow is configured in this GitHub repository. It runs each time a
commit is pushed on the `master` branch. All test files recursively found in the
`src/adql/` directory are validated. The resulting validation report (in
Markdown) is published in the
[Wiki of the GitHub repository](https://github.com/ivoa/lyonetia/wiki). The Wiki
page's name matches the branch name (i.e. `master` or `PR` followed by a
PullRequest number).
