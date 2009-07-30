#!/bin/sh
C=../GWTUpload-website
M=../GWTUpload-mavenrepo
W=../GWTUpload-wiki

[ ! -d "$C" ] && echo "$C project don't exist" && exit
[ ! -d "$M" ] && echo "$M project don't exist" && exit
[ ! -d "$W" ] && echo "$W project don't exist" && exit

[ ! -f "build.xml" ] && echo "build.xml don't exist" && exit


V=`grep "property name=\"version" build.xml | sed -e 's#^.*value="##' -e 's#".*$##'`
[ -z "$V" ] && echo "Unable to get version from buld.xml" && exit

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


