import sys
from arpeggio.peg import ParserPEG

def main():
	if len(sys.argv)!=3:
		sys.exit("Usage: {} <nonterminal> <expression>".format(
			sys.argv[0]))
	
	nonterminal, expression = sys.argv[1:]

	with open("adql2.1.peg") as f:
		parser = ParserPEG(f.read(), nonterminal, 
			skipws=False, ignore_case=False, memoization=True, debug="True")
	
	parser.parse(expression)


if __name__=="__main__":
	main()
