## VIVO Widgets

This project is built to work with an instance of [VIVO](http://vivoweb.org/).  Vivo widgets
provides a method for easily embeding a script tag into your site that includes things like
publications, grants and courses.  It can provide data for both individuals in VIVO and on
organizations in VIVO.  A few examples of VIVO instances:

* [VIVO Cornell](http://vivo.cornell.edu/)
* [Scholars@Duke](https://scholars.duke.edu/)
* [VIVO Florida](http://vivo.ufl.edu/)

## Background

VIVO Widgets is made of up the following parts.

1. The web service which used indexed data from VIVO pulled via SPARQL.

1. Web page widget builder for creating the JavaScript snippets to be included in other sites.

1. Web Page Widgets: Embeddable JavaScript that allows content to be easily added to other website
  from VIVO.


## Building

1. Clone the VIVO Widgets project.

2. Change into the project directory and launch [sbt](http://www.scala-sbt.org/) and tell it to
  create an executable jar. This will create an executable jar located at
  ``target/vivo-widgets.jar``

    ```
    $ ./sbt assembly
    ```

3. To start Jetty, run the following command:

    ```shell
    $ PORT=8888 java -jar -Xmx500m \
      -Dproperties.location=/path/to/deploy.properties \
      -Dwidget.logging.dir=/var/log/vivo_widgets/ \
      target/vivo-widgets.jar
    ```

4. Browse to http://localhost:8888/.

## Developing Locally

1. Clone the project.

2. Put a copy of the deploy.properties with your local config into the following directory:

    ```
    src/main/resources/
    ```

3. To start the application follow the commands based on
   [Scalatra First Project](http://www.scalatra.org/2.2/getting-started/first-project.html):

    ```shell
    $ ./sbt
    > container:start 
    ```

If you want automatic code reloading, do the following:

    ```shell
    $ ./sbt
    > container:start
    > ~ ;copy-resources;aux-compile
    ```

4. Browse to http://localhost:8080/builder?uri=<some-uri>.

### Run the tests

    $ ./sbt
    > test

or to run only a few tests:

    > test-only edu.duke.oit.solr.test.GrantSpec edu.duke.oit.solr.test.CourseSpec

## Solr Index

Vivo widgets are powered by a Solr index.  You will need to copy the ``solr``
directory located here into a directory on the server where you have solr
running.  From there, you will need to set the WidgetsSolr.directory in the
``deploy.properties`` (see section below).

When you first start widgets and setup your database and solr location, you
will need to reindex.  To reindex, run:

    ```
    $ curl -s -u username:password -X POST  \
      http://127.0.0.1:8888/widgets/updates/rebuild/index
    ```

For the username and password, these are set in the ``deploy.properties`` as
WidgetUpdateSetup.username and WidgetUpdateSetup.password.

You can also reindex a specific person or organization. To reindex person, run:

    ```
    $ curl -s -u username:password -X POST \
      http://127.0.0.1:8888/widgets/updates/rebuild/person?uri=https://scholars.duke.edu/individual/per123456
    ```

substituting in the person's uri.

## Configuration

Widgets uses a very similar ``deploy.properties`` file that is used by VIVO.
It actually reuses some of the same database connection information, but it
also adds a few properties.

*WidgetsSolr.directory*

> Path to solr configuration files on your solr server.

*Widgets.theme*

> Name of the theme to use.

*Widgets.topLevelOrg*

> The URI for your top level organization

*WidgetUpdateSetup.username*

> Username for updating or rebuilding the index

*WidgetUpdateSetup.password*

> Password for updating or rebuilding the index

See ``deploy.properties.example`` in the docs directory.
  
## Known Issues

* There is an old version of a Jena listener in ``listener/src`` that will no
  longer work with this version of vivo widgets.  It needs to be updated to
support a new format of the json to update widgets.
