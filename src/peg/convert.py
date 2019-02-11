#!/usr/bin/env python
# -*- coding: utf-8 -*-

####################################################################
# convert.py                                                       #
#     Converts standard PEG grammar to Arpeggio grammar            #
####################################################################
# Authors:                                                         #
#     Jon Juaristi Campillo (ARI, ZAH, Universität Heidelberg)     #
#     <mailto:juaristi@uni-heidelberg.de>                          #
####################################################################

import re

pound_sign = re.compile(r"# ")
rule = re.compile(r"^[^#](?!(=\<\-)).+\s+((?!=(\<\-|)(?=\#)).+\n)+", re.I | re.M)
with open("adql2.1.peg", "r+") as adql_peg_file:
    adql_peg_text = adql_peg_file.read()
    rules = re.findall(rule, adql_peg_text)
    adql_peg_text = rule.sub(lambda t: t.group().strip() + ";\n\n", adql_peg_text)
    adql_peg_text = re.sub(pound_sign, '// ', adql_peg_text)
    adql_arp_text = re.sub("'\\[", "r'[", adql_peg_text)
    adql_arp_file = open("adql2.1.arp.peg", "w")
    adql_arp_file.write(adql_arp_text)

# vi:et:sta:sw=4
