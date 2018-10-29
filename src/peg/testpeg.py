#!/usr/bin/env python
# -*- coding: utf-8 -*-

####################################################################
# testpeg.py                                                       #
#     Python script which supports the ADQL 2.1 standard in PEG    #
#     format (located at adql2.1.peg). This file should eventually #
#     be moved to another folder in order to not clash with the    #
#     project structure (preferably src/gavo)                      #
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
        sys.stderr.write("Query '%s' doesn't parse but should:\n%s\n\n"%(query, msg))
    else:
        print "Successfully analysed as a valid query"

def assert_invalid(query, uuid):
    try:
 	parser.parse(query)
    except NoMatch:
        print "Can't parse invalid query\n"
        pass
    except Exception, msg:
	sys.stderr.write("Query '%s' Raises non-parse exception:\n%s\n\n"%(query, msg))
#    else:
#        sys.stderr.write("Query '%s' parses but shouldn't\n" % (query))

def run_test(query_el):
    adql = query_el.find("adql")
    print adql.text
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
    with open("adql2.1.peg", "r") as adql_peg_file:
        adql_peg = adql_peg_file.read()
        print "CREATING GRAMMAR..."
        parser = ParserPEG(adql_peg, 'query', ignore_case=True, skipws=False, debug=False, reduce_tree=True, memoization=True)
        print "GRAMMAR CREATED\n"
        #for f in ["geometry.xml", "regressionlike.xml", "setexpressions.xml", "simpleunit.xml", "subqueries.xml"]:
        for f in [
                "../adql/gavo/geometry.xml",
                "../adql/gavo/regressionlike.xml", 
                #"../adql/gavo/setexpressions.xml",
                "../adql/gavo/simpleunit.xml", 
                "../adql/gavo/subqueries.xml"
                ]:
            test_file(f)

