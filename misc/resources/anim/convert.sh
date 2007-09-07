#!/bin/sh

export SWFTOOLS=/cygdrive/c/swftools
cd out
for FILE in evo swarm ; do
	echo $FILE
	$SWFTOOLS/pdf2swf -o tmp.swf $FILE.pdf > /dev/null
	$SWFTOOLS/pdf2swf -o tmp-last.swf $FILE-last.pdf > /dev/null
	$SWFTOOLS/swfc ../wrapper.sc
	mv out.swf $FILE.swf
	rm tmp.swf tmp-last.swf
done
cd ..

