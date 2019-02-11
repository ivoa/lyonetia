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

import os
import re
import sys
from xml.etree import ElementTree as etree

from arpeggio import NoMatch
from arpeggio.peg import ParserPEG


def get_parser(debug=False, root='query_specification'):
    """returns a PEG parser for the local ADQL grammar.

    This patches the grammar literal a bit to make it palatable to
    Arpeggio.

    NOTE: This is *not* a general PEG-to-arpeggio translator; it
    just happens to work on the ADQL grammar (and might do the
    trick of other simple cases).
    """
    with open("adql2.1.peg") as f:
        peg_rules = f.read()
    
    # insert semicolons between rules
    peg_rules = re.sub(r"(?m)^[^#](?!(=\<\-)).+\s+"
        r"((?!=(\<\-|)(?=\#)).+\n)+",
        lambda match: match.group().strip() + ";\n",
        peg_rules)

    # replace comments (TODO: make sure the # isn't in a literal)
    peg_rules = re.sub('#', '// ', peg_rules)

    # adapt character range syntax
    peg_rules = re.sub("'\\[", "r'[", peg_rules)

    return ParserPEG(peg_rules, 
       root,
       ignore_case=True, 
       skipws=False, 
       debug=False, 
       reduce_tree=True, 
       memoization=True)


class Tester(object):
    def __init__(self):
        self.parser = get_parser()
        self.successful, self.failed = 0, 0

    def assert_valid(self, query, uuid, f, desc):
        try:
            result = self.parser.parse(query)
            self.successful += 1

        except Exception, msg:
            sys.stderr.write(
                    "Query '{}' doesn't parse but should:\n"
                    "{}\nLocated at file {}, description: '{}'\n\n".format(
                        query.strip(), msg, f, desc.strip()))
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
        try:
            d = query_el.find("description").text
        except AttributeError:
            d = "<missing description>"
        query = adql.text
        if adql.get("valid")=="true":
            self.assert_valid(query, query_el.get("uuid"), f, d)
        else:
            self.assert_invalid(query, query_el.get("uuid"), f, d)


def test_file(tester, file_name):
    with open(file_name) as f:
        for ev, el in etree.iterparse(f):
            if el.tag=="query":
                tester.run_test(el, file_name)


def iter_example_files(root_dir):
    for dirpath, dirnames, filenames in os.walk(root_dir):
        for name in filenames:
            if name.endswith(".xml"):
                yield os.path.join(dirpath, name)


if __name__=="__main__":
    tester = Tester()
    for f_name in iter_example_files("../adql"):
        try:
           test_file(tester, f_name)
        except Exception as msg:
            print("[Skipping examples from {}: {}]".format(
                f_name, msg))
    print("{} ok, {} failed".format(tester.successful, tester.failed))

# vi:et:sta:sw=4
