# VIVO Widgets

A project build to work with an instance of [VIVO](http://vivoweb.org/).  You can also find documentation on the [VIVO Wiki](http://sourceforge.net/apps/mediawiki/vivo/index.php?title=VIVO_Widgets)

# Background

VIVO Widgets is made of up the following parts.

  1. The web service which used indexed data from VIVO pulled via SPARQL.
  1. Web page widget builder for creating the JavaScript snippets to be included in other sites.
  1. Template changes VIVO that allow for the "Add to my web site".
  1. Web Page Widgets: Embeddable JavaScript that allows content to be easily added to other website from VIVO.
  1. OpenSocial Widget: OpenSocial application that allows a user to search content in VIVO from their OpenSocial container.
  1. JSR 286 Portlet: provide an portlet that can be deployed to JSR compliant portal (like [Liferay](http://www.liferay.com/)).


# Building from Source

  1. Download and setup [sbt](http://code.google.com/p/simple-build-tool/).

  2. Clone the VIVO Widgets project (TODO).

  3. Change into the project directory and launch [sbt](http://code.google.com/p/simple-build-tool).

      $ sbt

  4. Fetch the dependencies.

      > update

  5. Start Jetty, enabling continuous compilation and reloading.

      > jetty-run
  
  6. Browse to http://localhost:8080/.
  


  