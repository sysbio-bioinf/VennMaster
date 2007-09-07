#!python
#
# Converts a gene ontology OBO file into an easy readable text file
#

import re

file = open('gene_ontology_edit.obo','r')
inTerm = False
curId = None
curParents = []
for line in file:
    L = line.strip()
    if re.match('\[.*\]',L):
        if inTerm:
            # end current term
            buf = curId + '\t'
            if len(curParents)>0:
                buf += '\t'.join(curParents)
            print buf
            inTerm = False 
        
    if L=='[Term]':
        # a new term starts
        if inTerm:
            # end current term
            buf = curId + '\t'
            if len(curParents)>0:
                buf += '\t'.join(curParents)
            print buf
            inTerm = False
        inTerm = True
        curId = None
        curParents = []
    elif inTerm:
        idx = L.find('!')
        if idx>=0:
            L = L[0:idx] # cut off description
        idx = L.find(':')
        if idx>=0:
            key = L[0:idx].strip()
            value = L[(idx+1):].strip()
            if key == 'id':
                curId = value
            elif key == 'is_a':
                curParents.append(value)
            elif key =='is_obsolete' and value=='true':
                curId = None
                curParents = []
                inTerm = False
                
if inTerm:
    # end current term
    buf = curId + '\t'
    if len(curParents)>0:
        buf += '\t'.join(curParents)
        print buf
            
file.close()

