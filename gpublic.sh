#!/bin/sh
C=../GWTUpload-website
M=../GWTUpload-mavenrepo
W=../GWTUpload-wiki
SVN_REPO=https://gwtupload.googlecode.com/svn

[ ! -d "$C" ] && echo "$C project don't exist" && exit
[ ! -d "$M" ] && echo "$M project don't exist" && exit
[ ! -d "$W" ] && echo "$W project don't exist" && exit

[ ! -f "build.xml" ] && echo "build.xml don't exist" && exit

svn status build.xml *.sh src | grep "..."
if [ $? = 0 ]
then
    echo -e "Changes detected, do you want to continue? (y/n) [y]  \c"
    read t
    [ -n "$t" -a "$t" != y ] && exit
    EDITOR=vi svn commit
fi




V=`grep "property name=\"version" build.xml | sed -e 's#^.*value="##' -e 's#".*$##'`
[ -z "$V" ] && echo "Unable to get version from buld.xml" && exit

svn copy $SVN_REPO/trunk/GWTUpload $SVN_REPO/tags/GWTUpload-$V -m "Tag for release $V" 2>&1

echo "Creating jsUpload wiki documentation"
perl gjslib.pl > $W/JsUpload_Documentation.wiki

ant clean zip || exit

mkdir -p $C/javadoc
find $C/javadoc -name "*.html" -exec rm '{}' ';'

cmd="cp -r gwtupload-$V/doc/*   $C/javadoc/"
echo "Executing: $cmd" && $cmd

find $C -name "*html" | xargs svn add
find $C -name "*html" | xargs svn propset svn:mime-type text/html

mkdir -p $M/gwtupload/gwtupload/$V
cp gwtupload-$V.jar $M/gwtupload/gwtupload/$V
md5sum gwtupload-$V.jar | awk '{print $1}' > $M/gwtupload/gwtupload/$V/gwtupload-$V.jar.md5

cat <<EOF > $M/gwtupload/gwtupload/$V/gwtupload-$V.pom
<?xml version="1.0" encoding="UTF-8"?><project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>gwtupload</groupId>
  <artifactId>gwtupload</artifactId>
  <version>$V</version>
  <description>GWTUpload Library, v$V</description>
</project>
EOF


