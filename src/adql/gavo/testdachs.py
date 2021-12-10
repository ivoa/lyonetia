# run lyonetia tests against the DaCHS ADQL parser

import sys
from xml.etree import ElementTree as etree

from gavo import adql

def parse(statement, grammar=adql.getRawGrammar()[1]):
	return grammar.parseString(statement, parseAll=True)


def assert_valid(query, src_name, uuid):
	try:
		parse(query)
	except Exception as msg:
		sys.stderr.write("%s#%s doesn't parse but should: %s\n"%(
		    src_name, uuid, msg))


def assert_invalid(query, src_name, uuid):
	try:
		parse(query)
	except (adql.ParseException, adql.ParseSyntaxException):
		pass
	except Exception as msg:
		sys.stderr.write("%s#%s raises non-parse exception: %s\n"%(
		    src_name, uuid, msg))
	else:
		sys.stderr.write("%s#%s parses but shouldn't.\n"%(
		    src_name, uuid))


def run_test(query_el, file_name):
	adql = query_el.find("adql")
	if adql.get("valid")=="true":
		assert_valid(adql.text, file_name, query_el.get("uuid"))
	else:
		assert_invalid(adql.text, file_name, query_el.get("uuid"))

def test_file(file_name):
	with open(file_name) as f:
		for ev, el in etree.iterparse(f):
			if el.tag=="query":
				run_test(el, file_name)


if __name__=="__main__":
	for file_name in sys.argv[1:]:
		test_file(file_name)
