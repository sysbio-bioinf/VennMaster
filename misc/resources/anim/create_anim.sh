#!/bin/sh
VENN=../../bin/
for ALG in swarm evo ; do
	echo "ALG=$ALG"
	java -jar $VENN/venn.jar --list ex-001.list --optstate out/state-$ALG.txt \
			--svg out/out-$ALG.svg --cfg config-$ALG.xml
done

