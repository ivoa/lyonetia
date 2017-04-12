# lyonetia
A project for working on the BNF specification of ADQL, named after the <a href='http://ukmoths.org.uk/systematic-list/#Lyonetiinae'>Lyonetiinae</a> family of moths.

At the [October 2016 interop in Trieste](http://www.adass2016.inaf.it/index.php/13-ivoa-interop) the DAL group decided to [delay putting the ADQL 2.1 working draft forward for the next stage of review](http://wiki.ivoa.net/internal/IVOA/InteropOct2016DAL/adql-20161022-002.pdf#5) because there were questions about the validity of the [Backus–Naur form](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form) (BNF) definition of the language grammar included in the specification.

Prior to this, the BNF definition of the language grammar has been treated as a human readable resource that was interpreted manually by people developing ADQL parser implementations.
 
The October 2016 decision added a new requirement that the BNF definition of the language grammar needed to be machine readable and had to be validated against a set of standard queries.

In order to accomplish this, this project was set up to develop the tools and test data needed to create a machine readable version of the grammar. 

## Initial import
As a starting point, the BNF grammar definition from the ADQL-2.1 working draft dated [2nd May 2016](http://www.ivoa.net/documents/ADQL/20160502/index.html) was imported into the *Lyonetia* project on the [15th June 2016](https://github.com/ivoa/lyonetia/commit/0a30d7fa4ee306bf49c0aefbbf50b845918fbe16).

## Project plan 

The following wiki pages explore some ideas on how develop the tools and methods we need to create a machine readable version of the grammar.

* [BNF grammar and validation](../../wiki/BNF-grammar-and-validation)

## Test queries

The 




## Project funding 

This project has received funding from the following sources :
* The European Community's Seventh Framework Programme (FP7-SPACE-2013-1) under grant agreement n°606740, 
* The European Commission Framework Programme Horizon 2020 Research and Innovation action under grant agreement n°653477
* The UK Science and Technology Facilities Council under grant numbers ST/M001989/1, ST/M007812/1, and ST/N005813/1

