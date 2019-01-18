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
#         <http://textx.github.io/Arpeggio/stable>                 #
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

def assert_valid(query, uuid, f, desc):
    try:
        result = parser.parse(query)
    except Exception, msg:
        sys.stderr.write(
                "Query '{}' doesn't parse but should:\n{}\nLocated at file {}, description: '{}'\n\n".format(
                query, msg, f, desc))

def assert_invalid(query, uuid, f, desc):
    try:
        result = parser.parse(query)
        sys.stderr.write("Query '{}' parses but shouldn't.\nLocated at file {}, description: '{}'\n\n".format(query, f, desc))
    except NoMatch:
        pass
    except Exception, msg:
        sys.stderr.write(
            "Query '{}' Raises non-parse exception:\n{}\n\n".format(query, msg))


def run_test(query_el, f):
    adql = query_el.find("adql")
    d = query_el.find("description").text
    if adql.get("valid")=="true":
        assert_valid(adql.text.upper(), query_el.get("uuid"), f, d)
    else:
        assert_invalid(adql.text.upper(), query_el.get("uuid"), f, d)


def test_file(file_name):
    with open(file_name) as f:
        for ev, el in etree.iterparse(f):
            if el.tag=="query":
                run_test(el, file_name)

if __name__=="__main__":
    with open("adql2.1.peg", "r") as adql_peg_file:
        adql_peg = adql_peg_file.read()
        parser = ParserPEG(adql_peg, 'query', ignore_case=True, skipws=False, debug=False, reduce_tree=True, memoization=True)
        for f in [
                "../adql/gavo/whitespace.xml",
                "../adql/gavo/geometry.xml",
                "../adql/gavo/regressionlike.xml",
                "../adql/gavo/setexpressions.xml",
                "../adql/gavo/simpleunit.xml",
                "../adql/gavo/subqueries.xml"
                ]:
            test_file(f)

# vi:et:sta:sw=4
