#!/bin/bash
sed -i '/GRAMMAR CREATED/p; /CREATING GRAMMAR/,/GRAMMAR CREATED/d' output.txt
grep "??\|++\|--" output.txt > filtered_output.txt
sed -i 's/    //g' filtered_output.txt
#Matching rule query=Sequence at position 0
