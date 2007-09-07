#!/bin/sh -e
#
# deploy.sh
# Creates a new VennMaster distribution
#
echo "Creating a VennMaster distribution"

macosx=0

version=$( cat "./VERSION" )
echo "Version $version"
target="VennMaster-$version"

wd=$PWD

echo "Creating venn.jar in ./src"
cd "$wd/src"
./pack.sh
cd "$wd"


### ASSEMBLE FILES IN TMP DIRECTORY ###
tmp="$wd/tmp"
if [ ! -d "$tmp" ] ; then
	mkdir "$tmp"
else
	rm -rf "$tmp/"*
fi

if [ ! -d "$wd/deploy" ] ; then
	mkdir "$wd/deploy"
fi

mkdir "$tmp/$target"
cp -r "$wd/bin/" "$tmp/$target/"
cp -r "$wd/doc" "$tmp/$target/"
mkdir "$tmp/$target/examples"
cp "$wd/data/deploy/"* "$tmp/$target/examples/"
cp "$wd/readme.txt" "$tmp/$target/"
cd "$wd/tmp"
zip -r "$wd/deploy/$target.zip" "$target"
cd "$wd"

############################# UPDATE .app #####################################
if [ $macosx -eq 1 ] ; then
	# copy template 
	cp -r $wd/VennMaster.app $wd/deploy/$target.app
	cp $wd/bin/venn.jar $wd/deploy/$target.app/Contents/Resources/Java/
	
	# create dmg
	hdiutil create -megabytes 32 -fs HFS+ -volname $target $tmp/image.dmg
	hdiutil mount $tmp/image.dmg
	
	# copy files into dmg
	cp -r $wd/deploy/$target.app /Volumes/$target/
	cp -r $wd/doc /Volumes/$target/
	mkdir /Volumes/$target/examples
	cp $wd/data/deploy/* /Volumes/$target/examples/
	cp $wd/readme.txt /Volumes/$target/
	hdiutil unmount /Volumes/$target/
	rm -f $wd/deploy/$target.dmg
	hdiutil convert -format UDZO $tmp/image.dmg -o $wd/deploy/$target.dmg	
fi

## clean tmp
rm -rf "$tmp" 

##
echo ""
echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo "FIND $target.zip and $target.app IN VennMaster/deploy"

##



