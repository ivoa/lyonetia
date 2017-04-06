# run lyonetia tests against the DaCHS ADQL parser

import sys
from xml.etree import ElementTree as etree

from gavo import adql

def parse(statement, grammar=adql.getRawGrammar()[1]):
	return grammar.parseString(statement, parseAll=True)


def assert_valid(query, uuid):
	try:
		parse(query)
	except Exception, msg:
		sys.stderr.write("%s doesn't parse but should: %s\n"%(uuid, msg))


def assert_invalid(query, uuid):
	try:
		parse(query)
	except (adql.ParseException, adql.ParseSyntaxException):
		pass
	except Exception, msg:
		sys.stderr.write("%s raises non-parse exception: %s\n"%(uuid, msg))
	else:
		sys.stderr.write("%s parses but shouldn't.\n"%uuid)


def run_test(query_el):
	adql = query_el.find("adql")
	if adql.get("valid")=="true":
		assert_valid(adql.text, query_el.get("uuid"))
	else:
		assert_invalid(adql.text, query_el.get("uuid"))

def test_file(file_name):
	with open(file_name) as f:
		for ev, el in etree.iterparse(f):
			if el.tag=="query":
				run_test(el)


if __name__=="__main__":
	for file_name in sys.argv[1:]:
		test_file(file_name)
