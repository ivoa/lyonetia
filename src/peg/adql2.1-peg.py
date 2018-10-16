#!/usr/bin/env python
# -*- coding: utf-8 -*-

##################################################################
# adql2.1-peg.py:                                                #
#     Python Script which parses the ADQL 2.1 standard using PEG #
##################################################################

import sys
from arpeggio.peg import ParserPEG
from xml.etree import ElementTree as etree

def assert_valid(parser, query, uuid):
    try:
	parser.parse(query)
    except Exception, msg:
        sys.stderr.write("'%s' doesn't parse but should:\n%s\n\n"%(query, msg))


def assert_invalid(parser, query, uuid):
    try:
 	parser.parse(query)
    except Exception, msg:
	sys.stderr.write("'%s' raises non-parse exception:\n%s\n\n"%(query, msg))

def run_test(query_el, parser):
    adql = query_el.find("adql")
    if adql.get("valid")=="true":
        assert_valid(parser, adql.text.upper(), query_el.get("uuid"))
    else:
        assert_invalid(parser, adql.text.upper(), query_el.get("uuid"))

def test_file(file_name, parser):
    with open(file_name) as f:
        for ev, el in etree.iterparse(f):
	    if el.tag=="query":
	        run_test(el, parser)

if __name__=="__main__":
    with open("adql2.1.peg", "r") as grammar_file:
        adql_grammar = grammar_file.read()
        print "CREATING GRAMMAR.."
        parser = ParserPEG(adql_grammar, 'query', ignore_case=True, skipws=False, debug=False, reduce_tree=True)
        print "GRAMMAR CREATED"
        #for f in ["geometry.xml", "regressionlike.xml", "setexpressions.xml", "simpleunit.xml", "subqueries.xml"]:
        for f in ["simpleunit.xml"]:
            test_file(f, parser)

