[![Validator test](https://github.com/gmantele/lyonetia/actions/workflows/test-validator.yml/badge.svg)](https://github.com/gmantele/lyonetia/actions/workflows/test-validator.yml)
[![ADQL Validation](https://github.com/gmantele/lyonetia/actions/workflows/validate-queries.yml/badge.svg)](https://github.com/gmantele/lyonetia/wiki)

# lyonetia
A project for working on the BNF specification of ADQL, named after the <a href='http://ukmoths.org.uk/species/lyonetia-clerkella'>Lyonetiinae</a> family of moths.

At the [October 2016 interop in Trieste](http://www.adass2016.inaf.it/index.php/13-ivoa-interop) the DAL group decided to [delay putting the ADQL 2.1 working draft forward for the next stage of review](http://wiki.ivoa.net/internal/IVOA/InteropOct2016DAL/adql-20161022-002.pdf#5) because there were questions about the validity of the [Backus–Naur form](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form) (BNF) definition of the language grammar included in the specification.

Prior to this, the BNF definition of the language grammar has been treated as a human readable resource that was interpreted manually by people developing ADQL parser implementations.
 
The October 2016 decision added a new requirement that the BNF definition of the language grammar needed to be machine readable and had to be validated against a set of standard queries.

## Initial import
As a starting point, the BNF grammar definition from the ADQL-2.1 working draft dated [2nd May 2016](http://www.ivoa.net/documents/ADQL/20160502/index.html) was imported into the *Lyonetia* project on the [15th June 2016](https://github.com/ivoa/lyonetia/commit/0a30d7fa4ee306bf49c0aefbbf50b845918fbe16).

## Project plan 

The following wiki pages explore some ideas on how develop the tools and methods we need to create a machine readable version of the grammar.

* [BNF grammar and validation](../../wiki/BNF-grammar-and-validation)

## Test queries

One of the aims of this project is collect a set of test queries that can be used both to validate the BNF grammar produced by this project, and to validate ADQL parser implementations and eventually TAP serices.

The collection of is ADQL queries is stored as part of the [project source code](../../tree/master/src/adql).

The project team welcomes contributions from anyone who has examples of ADQL queries that can be used to test aspects of the BNF grammar. We are particularly interested in examples of ADQL queries that demomstrate specific science use cases.

To contribute to the collection clone the GitHub project, choose an appropriate name to represent your contribution (e.g. your institute acronym), create a sub-directory and add your queries.

At the moment the format for contributing queries is based on an [example XML file](../../blob/master/src/adql/roe/example.xml). As the project evolves we may develop a stricter XML schema for contributions.

## Project funding 

This project has received funding from the following sources :
* The European Community's Seventh Framework Programme (FP7-SPACE-2013-1) under grant agreement n°606740, Gaia [GENIUS](https://cordis.europa.eu/project/id/606740) 
* The European Commission Framework Programme Horizon 2020 Research and Innovation action under grant agreement n°653477, [ASTERICS](https://cordis.europa.eu/project/id/653477)
* The UK Science and Technology Facilities Council under grant numbers [ST/M001989/1](https://gtr.ukri.org/projects?ref=ST%2FM001989%2F1), [ST/M007812/1](https://gtr.ukri.org/projects?ref=ST%2FM007812%2F1), and [ST/N005813/1](https://gtr.ukri.org/projects?ref=ST%2FN005813%2F1)

