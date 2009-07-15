


CONTENTS:

- Build file for ant: build.xml
- Java docs: doc
- Sample application: gwtupload-0.2.0.war
- Gwt version of the library: gwtupload-0.2.0.jar
- Javascrip version of the library and cgi-bin script: jsupload-0.2.0.zip
- External libraries needed for building the application: lib
- Java sources files and assets: src
- index.html and web.xml: war


BUILDING:

- Edit build.xml and set the property pointing to the gwt sdk:
  <property name="gwt.sdk" location="[...]/gwt-linux-1.6.4" />

- Create the application:
  ant war

- Create the gwt library:
  ant jar
  
- Create the js library:
  ant js
  
- Create the distribution file:
  ant zip

