#!/bin/sh
C=../GWTUpload-website

[ ! -d "$C" ] && echo "$C project don't exist: $C" && exit

[ ! -f "build.xml" ] && echo "build.xml don't exist" && exit


V=`grep "property name=\"version" build.xml | sed -e 's#^.*value="##' -e 's#".*$##'`
[ -z "$V" ] && echo "Unable to get version from buld.xml" && exit

ant clean zip || exit

mkdir -p $C/javadoc
find $C/javadoc -name "*.html" -exec rm '{}' ';'

cmd="cp -r gwtupload-$V/doc/*   $C/javadoc/"
echo "Executing: $cmd" && $cmd

find $C -name "*html" | xargs svn propset svn:mime-type text/html

mkdir -p $C/mavenrepo/gwtupload/gwtupload/$V
cp gwtupload-$V.jar $C/mavenrepo/gwtupload/gwtupload/$V
md5sum gwtupload-$V.jar | awk '{print $1}' > $C/mavenrepo/gwtupload/gwtupload/$V/gwtupload-$V.jar.md5

cat <<EOF > $C/mavenrepo/gwtupload/gwtupload/$V/gwtupload-$V.pom
<?xml version="1.0" encoding="UTF-8"?><project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>gwtupload</groupId>
  <artifactId>gwtupload</artifactId>
  <version>$V</version>
  <description>GWTUpload Library, v$V</description>
</project>
EOF

