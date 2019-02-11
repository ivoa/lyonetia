import sys
from arpeggio.peg import ParserPEG

import testpeg

def main():
    if len(sys.argv)!=3:
        sys.exit("Usage: {} <nonterminal> <expression>".format(
            sys.argv[0]))

    nonterminal, expression = sys.argv[1:]
    
    parser = testpeg.get_parser(debug=True, root=nonterminal)
    parser.parse(expression)


if __name__=="__main__":
    main()

# vi:et:sta:sw=4

