
---

This is a fork of GWTUpload (http://code.google.com/p/gwtupload/) to remove the
dependency on the server-side session.

This means it can run in a clustered environment and share file upload
information via a user-provided FileRepository implementation (that could be
database-backed, cache-backed, etc.).

The servlet side for the fork is nearly a complete rewrite, with functionality
I didn't need taken out (e.g. multiple files, removing/clearing).

The client side is mostly untouched, saved for removing the `hasSession` check
and putting in an OnTokenRequestedHandler interface so that, when Send is clicked,
an event can fire that can go get the next token id (e.g. random or database id)
for use on the next upload.

---


CONTENTS:

- Build file for ant: build.xml
- Java docs: doc
- Sample application: gwtupload-x.x.x.war
- Gwt version of the library: gwtupload-x.x.x.jar
- Javascrip version of the library and cgi-bin script: jsupload-x.x.x.zip
- External libraries needed for building the application: lib
- Java sources files and assets: src
- index.html and web.xml: war


BUILDING:

- Edit build.xml and set the property pointing to the gwt sdk:
  <property name="gwt.sdk" location="[...]/gwt-linux/windows-x.x.x" />

- Create the application:
  ant war

- Create the gwt library:
  ant jar
  
- Create the js library:
  ant js
  
- Create the distribution file:
  ant zip

