#!/usr/bin/env python
# -*- coding: utf-8 -*-

####################################################################
# adql2.1-peg.py                                                   #
#     Python script which supports the ADQL 2.1 standard in PEG    #
#     format (located at adql2.1.peg). This file should eventually #
#     be moved to another folder in order to not clash with the    #
#     project structure (preferably src/gavo) and be renamed,      #
#     e.g., testpeg.py                                             #                       
#     Requirements:                                                #
#         Arpeggio for PEG support.                                #
#         <http://www.igordejanovic.net/Arpeggio/stable>           #
####################################################################
# Authors:                                                         #
#     Jon Juaristi Campillo (ARI, ZAH, Universität Heidelberg)     #
#     <mailto:juaristi@uni-heidelberg.de>                          #
####################################################################

import sys
from arpeggio import NoMatch
from arpeggio.peg import ParserPEG
from xml.etree import ElementTree as etree

global parser

def assert_valid(query, uuid):
    try:
	parser.parse(query)
    except Exception, msg:
        sys.stderr.write("Doesn't parse but should:\n%s\n\n"%( msg))
    else:
        sys.stderr.write("Successfully analysed as a valid query\n")

def assert_invalid(query, uuid):
    try:
 	parser.parse(query)
    except NoMatch:
        sys.stderr.write("Can't parse invalid query\n")
        pass
    except Exception, msg:
	sys.stderr.write("Raises non-parse exception:\n%s\n\n"%( msg))
    else:
        sys.stderr.write("Parses but shouldn't\n")

def run_test(query_el):
    adql = query_el.find("adql")
    sys.stderr.write("%s" % (adql.text.upper()))
    if adql.get("valid")=="true":
        assert_valid(adql.text.upper(), query_el.get("uuid"))
    else:
        assert_invalid(adql.text.upper(), query_el.get("uuid"))

def test_file(file_name):
    with open(file_name) as f:
        for ev, el in etree.iterparse(f):
	    if el.tag=="query":
	        run_test(el)

if __name__=="__main__":
    with open("adql2.1.peg", "r") as grammar_file:
        adql_grammar = grammar_file.read()
        print "CREATING GRAMMAR..."
        parser = ParserPEG(adql_grammar, 'query', ignore_case=True, skipws=False, debug=True, reduce_tree=True)
        print "GRAMMAR CREATED"
        #for f in ["geometry.xml", "regressionlike.xml", "setexpressions.xml", "simpleunit.xml", "subqueries.xml"]:
        for f in ["simpleunit.xml"]:
            test_file(f)

