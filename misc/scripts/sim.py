#!/usr/bin/python
"""
sim.py

VennMaster simulation automation.
Version 2006/03/22

Requires libxml2 for python.
(see http://uche.ogbuji.net/tech/akara/nodes/2003-01-01/pyxml-akara)

"""

import sys, string, os, libxml2, time


##############################################################################
#
#                            HELPER FUNCTIONS
#
##############################################################################
def seq(A,B,n):
	"Creates a sequence from A to B with n steps."
	result = list()
	f = (B-A)/float(n-1)
	for k in range(n):
		result.append(float(k)*f+A)
	return result
        

def setValue(doc,name,value):
	"Sets the value of a node with name <name>"
	L = doc.xpathEval('//'+name)
	assert len(L) == 1
	L[0].setContent(str(value))


def rmcomments(x):
	"Removes an optional comment from a character string."
	x = x.strip()
	i = x.find('#')
	if i>=0:
		x = x[0:i]
	return x
	
def append(path, x, sep='\n'):
	"Appends a string x to a file 'path'"
	f = open(path,'a+')
	f.write(x + sep)
	f.close()

##############################################################################

data_file = 'data-20060209.txt'
#data_file = 'data.txt'
output_path = '/results/tmp.40'		# << CHANGE THIS PATH TO A NEW OUTPUT DIRECTORY

cwd = sys.path[0]   # script working directory

# Load config file templates
cfg = libxml2.parseFile(cwd+'/config2.xml')
flt = libxml2.parseFile(cwd+'/filter.xml')

# Load seed values (=number of runs)
seedFile = cwd+'/seed.txt'
f = open(seedFile,'r')
seeds = map((lambda x: int(x)),f.readlines())
f.close()

seeds = seeds[0:10]	# reduce number of runs (comment out!)

# Path to the Java executable file
#javaPath = 'c:/Programme/Java/j2re1.4.2_09/bin/java'
javaPath = 'c:/Programme/Java/jre1.5.0_05/bin/java'

# load data set descriptions
f = open(data_file,'r')
X = f.readlines()
f.close()
fileList = map((lambda x: rmcomments(x).split('\t')),X)
#n = len(X[0])
fileList = filter((lambda x: len(x)>=1 and len(x[0])>1),fileList)

fileList = fileList[0:3]

print(fileList)

#
outPath = cwd+output_path	
if os.path.exists(outPath):
	print "ERROR: path "+outPath+" already exists"
	sys.exit(-1)
else:
	os.mkdir(outPath)


# write out parameters
params = seq(0.0,1000.0,10)
#params = seq(0.01,1,10)
#params = [0,2]

f = open(outPath+'/params.txt','w+')
f.write('\n'.join(map(str,params)))
f.close()

# set venn options
setValue(cfg,'sizeFactor',0.4)
setValue(cfg,'optimizer',0)
setValue(cfg,'delta',400)
#setValue(cfg,'optSwarm/maxIterations',200)
#setValue(cfg,'optSwarm/maxConstIterations',25)
#setValue(cfg,'optSwarm/numParticles',30)

for i in range(0,len(fileList)):
	entry = fileList[i]
	st = str(i)+'\t'+'\t'.join(map(str,entry))
	print st
	append(outPath+'/data.txt',st)
	
	if len(entry) == 5:
		# GoMiner file
		gceFile = cwd+'/data/'+entry[0]
		seFile = cwd+'/data/'+entry[1]
		dataFile = []
		setValue(flt,"minTotal",int(entry[2]))
		setValue(flt,"maxTotal",int(entry[3]))
		setValue(flt,"maxPValue",float(entry[4]))
		
		# write filter settings
		fltFile = outPath+'/filter'+str(i)+'.xml'
		of = open(fltFile,'w+')
		print >> of, flt
		of.close()
		
	elif len(entry) == 1:
		# .list file
		gceFile = []
		seFile = []
		dataFile = cwd+'/'+entry[0]
	else:
		raise "Illegal format of data file"
		
	
	for param in range(len(params)):
		
		p = params[param]
		setValue(cfg,'delta',p)
		#setValue(cfg,'maxV',p)
		#setValue(cfg,'optimizer',p)
		#setValue(cfg,'cGlobal',2*p)
		#setValue(cfg,'cLocal',p)
		
		for run in range(len(seeds)):
			
			print output_path+'data '+str(i)+' param '+str(param)+' run '+str(run)
			setValue(cfg,'randomSeed',seeds[run])
			
			# generate configuration files
			cfgFile = outPath+'/config-'+str(i)+'-'+str(param)+'-'+str(run)+'.xml'
			of = open(cfgFile,'w+')
			print >> of, cfg
			of.close()
	
			outSim  = outPath+'/sim-'+str(i)+'-' + str(param)+'-'+str(run)+'.txt'
			outProf = outPath+'/prof-'+str(i)+'-'+ str(param)+'-'+str(run)+'.txt'
			outSvg  = outPath+'/svg-'+str(i)+'-'+ str(param)+'-'+str(run)+'.svg'
	
			# common java parameters
			P = [javaPath,'-Xms256M','-Xmx256M']
			P += ['-jar','venn.jar','--cfg',cfgFile,'--sim',outSim,'--prof',outProf,'--svg',outSvg]
			if len(dataFile) > 0:
				# list file
				P += ['--list',dataFile]
			else:
				# GoMiner file
				P += ['--gce','"'+gceFile+'"','--se','"'+seFile+'"','--filter',fltFile]
			
			cmd = string.join(P,' ')
			
			# call VennMaster
			ret = 0
			time_start = time.time()
			try:
				# ret = os.spawnv(os.P_WAIT,javaPath,P)
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
			append(outPath+'/time.log','\t'.join(map(str,[i,param,run,diff])))
			

#cfg.freeDoc()
#flt.freeDoc()

