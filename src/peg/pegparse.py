#!/usr/bin/env python
# -*- coding: utf-8 -*-

##################################################################
# pegparse.py:                                                   #
#     Python Script which parses the ADQL 2.1 standard using PEG #
##################################################################

import sys
from arpeggio.peg import ParserPEG
from xml.etree import ElementTree as etree

def assert_valid(parser, query, uuid):
    try:
        print query
	parser.parse(query)
    except Exception, msg:
        sys.stderr.write("%s doesn't parse but should: %s\n"%(uuid, msg))


def assert_invalid(parser, query, uuid):
    try:
 	parser.parse(query)
    except Exception, msg:
	sys.stderr.write("%s raises non-parse exception: %s\n"%(uuid, msg))
    else:
	sys.stderr.write("%s parses but shouldn't.\n"%uuid)

def run_test(query_el, parser):
    adql = query_el.find("adql")
    if adql.get("valid")=="true":
        assert_valid(parser, adql.text, query_el.get("uuid"))
    else:
        assert_invalid(parser, adql.text, query_el.get("uuid"))

def test_file(file_name, parser):
    with open(file_name) as f:
        for ev, el in etree.iterparse(f):
	    if el.tag=="query":
	        run_test(el, parser)

if __name__=="__main__":
    with open("adql21.peg", "r") as grammar_file:
        adql_grammar = grammar_file.read()
        print "CREATING GRAMMAR.."
        parser = ParserPEG(adql_grammar, 'with_query', debug=False, ignore_case=True, skipws=False)
        #parser = ParserPEG(adql_grammar, 'query_expression', debug=False)
        print "GRAMMAR CREATED"
        for f in ["geometry.xml", "regressionlike.xml", "setexpressions.xml", "simpleunit.xml", "subqueries.xml"]:
            test_file(f, parser)

