#!/bin/sh
date +"%H:%M:%S:%N"
java -ea -Xms100m -Xmx100m -jar venn.jar --cfg config.xml --filter filter4.xml --svg tmp/out.svg --gce examples/Bach_GO-Genelist.gce --se examples/Bach_GO-Categorylist.se 
# --sim tmp/sim.txt --prof tmp/prof.txt
date +"%H:%M:%S:%N"

