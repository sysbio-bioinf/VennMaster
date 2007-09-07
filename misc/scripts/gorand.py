#!/usr/bin/python

"""
gorand.py

Randomized finding of Gene Ontology terms.

$Id$
"""

import os, string, sys, time, random

def loadGenes(fname):
	L = open(fname,'r').readlines()
	L = map(string.strip,L)
	L = filter((lambda x: len(x) > 0),L)
	L = map((lambda x: x.split('\t')[0]),L)
	return(L)
	

random.seed(173)
wd = os.getcwd()+'/..'
resultDir = 'results/gorand'
totalGeneFile = wd + '/data/barry_total.txt'
changedGeneFile = wd + '/data/barry_changed.txt'


## random generate subsets of the given length
allGenes = loadGenes(totalGeneFile)
changedGenes = loadGenes(changedGeneFile)
numChanged = len(changedGenes)
numRuns = 100

changedGeneList = resultDir+'/'+'list.txt'
fd = open(changedGeneList,'w+')
fd.write(changedGeneFile+'\n')
for i in range(numRuns):
	#G = random.sample(allGenes,numChanged)   # randomly choose genes
	G = random.sample(changedGenes,40)   # randomly choose genes
	name = resultDir+'/'+'changed-%.4d' % i
	fd.write(name+'\n')	
	f = open(name,'w+')
	for L in G:
		f.write(L+'\n')
	f.close()

	
fd.close()


## start gominer

javaPath = 'java'
#dbConnection = 'jdbc:mysql://mac1.innere2.klinik.uni-ulm.de/godb'
#dbUser = 'godb'
#dbPassword = 'godb'
#dbConnection = 'jdbc:mysql://discover.nci.nih.gov/GEEVS'
#dbUser = 'deploy'
#dbPassword = 'selectonly'
#dbDriver = 'com.mysql.jdbc.Driver'
dbConnection = 'jdbc:mysql://localhost/gominer'
dbUser = 'jk13'
dbPassword = 'blubb'
dbDriver = 'com.mysql.jdbc.Driver'

## java -cp gominer.jar gov.nih.nci.lmp.gominer.GOCommand -t /usr/home/gominer/total.gene.txt -c /usr/home/gominer/changed1 -d jdbc:mysql://discover.nci.nih.gov/GEEVS -j com.mysql.jdbc.Driver -u deploy -p selectonly -r /usr/home/gominer/dumptest -s UniProt\;MGI -o all -e gce\;svge -a all -v all -n true -x false -y false
# common java parameters
P = [javaPath,'-Xms256M','-Xmx256M']
P += ['-cp',wd+'/bin/gominer.jar','gov.nih.nci.lmp.gominer.GOCommand',
	  '-t',totalGeneFile,
	  #'-c',changedGeneFile,
	  '-h',changedGeneList,
	  '-d',dbConnection,
	  '-j',dbDriver,
	  '-u',dbUser,
	  '-p',dbPassword,
	  '-r',resultDir,
	  '-s','all', # data source
	  '-o', 'all',    # organism NCBI taxonomy ids
	  '-e', 'gce\;se', # export type(s)
	  '-a','all',     # root category
	  '-v','all',  # evidence code(s)
	  '-n','false',   # use enhanced name column
	  '-x','true',   # use crossreference table
	  '-y','true' ]  # use synonym table

cmd = string.join(P,' ')

def start():
	# call gominer
	ret = 0
	time_start = time.time()
	try:
		# ret = os.spawnv(os.P_WAIT,javaPath,P)
		print(cmd)
		ret = os.system(cmd)
	except OSError,data:
		print "OSError : "+str(data)+"\n"+cmd
		sys.exit(-1)

	time_stop = time.time()
	if  ret != 0:
		print "ERROR : return value = " + str(ret)+"\n"+cmd
		sys.exit(-1)

	# report timing
	diff = time_stop - time_start
	#append(outPath+'/time.log','\t'.join(map(str,[i,param,run,diff])))

start()
