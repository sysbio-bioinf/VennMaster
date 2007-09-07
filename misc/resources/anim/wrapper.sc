.flash bbox=500x500 fps=20  filename="out.swf"
	# .box background width=500 height=500 fill=white color=white line=0 
	
	
	.swf movie  filename="tmp.swf"
	.swf lastframe filename="tmp-last.swf"
	.frame 1
		.put movie
	.frame 500
		.del movie
		.put lastframe
.end

