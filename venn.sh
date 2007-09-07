#!/bin/sh
#java -ea -Xms256m -Xmx256m -cp venn.jar:junit.jar:batik venn.VennMaster
java -Xms256m -Xmx256m -jar venn.jar $@

