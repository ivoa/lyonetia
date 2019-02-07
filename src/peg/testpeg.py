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


class Tester(object):
    def __init__(self):
        with open("adql2.1.py.peg", "r") as adql_peg_file:
            adql_peg = adql_peg_file.read()
        self.parser = ParserPEG(adql_peg, 
            'query_specification', 
            ignore_case=True, 
            skipws=False, 
            debug=False, 
            reduce_tree=True, 
            memoization=True)
        self.successful, self.failed = 0, 0


    def assert_valid(self, query, uuid, f, desc):
        try:
            result = self.parser.parse(query)
            self.successful += 1

        except Exception, msg:
            sys.stderr.write(
                    "Query '{}' doesn't parse but should:\n"
                    "{}\nLocated at file {}, description: '{}'\n\n".format(
                        query, msg, f, desc))
            self.failed += 1

    def assert_invalid(self, query, uuid, f, desc):
        try:
            result = self.parser.parse(query)
            sys.stderr.write("Query '{}' parses but shouldn't.\n"
                "Located at file {}, description: '{}'\n\n".format(
                    query, f, desc))
            self.failed += 1

        except NoMatch:
            self.successful +=1

        except Exception, msg:
            sys.stderr.write(
                "Query '{}' Raises non-parse exception:\n{}\n\n".format(query, msg))
            self.failed += 1


    def run_test(self, query_el, f):
        adql = query_el.find("adql")
        d = query_el.find("description").text
        text = adql.text.upper()
        if adql.get("valid")=="true":
            self.assert_valid(text, query_el.get("uuid"), f, d)
        else:
            self.assert_invalid(text, query_el.get("uuid"), f, d)


def test_file(tester, file_name):
    with open(file_name) as f:
        for ev, el in etree.iterparse(f):
            if el.tag=="query":
                tester.run_test(el, file_name)

if __name__=="__main__":
    tester = Tester()
    for f in [
           "../adql/gavo/whitespace.xml",
           "../adql/gavo/geometry.xml",
           "../adql/gavo/regressionlike.xml",
           "../adql/gavo/setexpressions.xml",
           "../adql/gavo/simpleunit.xml",
           "../adql/gavo/subqueries.xml",
           "../adql/gavo/additionaltests.xml",
           "../adql/roe/common-queries.xml",
           "../adql/roe/example.xml",
           "../adql/roe/geometric.xml",
           "../adql/roe/offset.xml"
           ]:
       test_file(tester, f)
    print("{} ok, {} failed".format(tester.successful, tester.failed))

# vi:et:sta:sw=4
