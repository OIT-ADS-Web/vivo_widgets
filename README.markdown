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


3. Change into the project directory and launch [sbt](http://www.scala-sbt.org/) and tell it to
   create an executable jar. 

    ```
    $ bin/sbt assembly

    ```

This will create an executable jar located at  

    ```
    target/scala-2.10/vivo-widgets.jar

    ```

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

See ``deploy.properties.example`` in the root directory.


## Running

Vivo widgets needs a running instance of [SOLR](http://lucene.apache.org/solr).  It has been tested with solr 4.7. 


You will need to copy the ``solr/main``
directory located here into the directory on the server where you have solr
running.  That is determined by WidgetsSolr.directory property in the
``deploy.properties`` file (see above).


If you are starting from scratch (without an already existing SOLR instance) you can follow steps
such as the following:

1. Download SOLR from http://lucene.apache.org/solr/

2. This will likely be a file such as solr_4.7_examples.tar.gz.  Try downloading it.  You can put that 
   in a tmp directory (for instance). 

    ```
    cd tmp
    tar xvzf solr_4.7_examples.tar.gz.
    cd .. 
    ```

3. Copy the solr configuration files in ```solr/main`` `into the configuration area of the local solr server.  For instance:

   ```
   mkdir widgetsolr
   cp -R solr/main tmp/solr_4.7_examples/solr/widgetsolr
   ```

4. Start the server with a command much like this.  The directories may be different depending on where you put the solr_examples
   files etc... following the example above:
    
   ```
   java -Dsolr.home=tmp/solr_4.7_examples -Djetty.home=tmp/solr_4.7_examples -server \
    -DSTOP.PORT=8079 -DSTOP.KEY=pleasestop -jar tmp/solr_4.7_examples/start.jar 2> tmp/solr.log &
  ```
If you go somwhere like here (depending on the port) you should be able to verify it's running:

  ```
    http://localhost:8983/solr/
  ```
And when you are done, remember to make it stop, by running something like this:
    
    ```
    java -Dsolr.solr.home=. -server -DSTOP.PORT=8079 -DSTOP.KEY=pleasestop -jar stmp/solr_4.7_examples/start.jar --stop
    ```

3. Finally, to start vivo_widgets using the jar file run something like the following command:

    ```shell

    $ PORT=8888 java -jar -Xmx500m \
      -Dproperties.location=/path/to/deploy.properties \
      -Dwidget.logging.dir=/var/log/vivo_widgets/ \
      target/scala-2.10/vivo-widgets.jar
    ```

4. Browse to `http://localhost:8888/widgets/builder?uri=.`

5. This will likely say "No Content" - because the Solr index is much like a database, and the database is empty at this point.  But
   it means it is running.  In order to give it content, see the Solr Index section below.

## Solr Index

1. When you first start widgets and setup your database and solr location, you
will need to reindex.  To reindex, run (assuming you are running from the jar file on port 8888):

    ```
    $ curl -s -u username:password -X POST  \
      http://127.0.0.1:8888/widgets/updates/rebuild/index
    ```

For the username and password, these are set in the ``deploy.properties`` as
WidgetUpdateSetup.username and WidgetUpdateSetup.password.

2. You can also reindex a specific person or organization. To reindex person, run:

    ```
    $ curl -s -u username:password -X POST \
      http://127.0.0.1:8888/widgets/updates/rebuild/person?uri=https://vivo.duke.edu/individual/per000001
    ```

substituting in the person's uri.

3. Once you have data - you can see a page via id by sending the uri paramater. This is just an example:, 

   ```
   http://localhost:8888/widgets/builder?uri=https://scholars.duke.edu/individual/org50001204
   ```

You can query for uri ids via solr itself:

  ```  
  http://localhost:8983/solr/#/vivowidgetcore/query
  ```
  
## Developing Locally

1. Clone the project.

2. Change into the project directory.  Put a copy of your deploy.properties with your local config into the following directory
   (see Configuration section for details about this file).

    ```
    src/main/resources/
    ```
 
3. To start the application follow the commands based on
   [Scalatra First Project](http://www.scalatra.org/2.2/getting-started/first-project.html):

    ```shell
    $ bin/sbt
    > container:start 
    ```

If you want automatic code reloading, do the following:

    ```shell
    $ bin/sbt
    > container:start
    > ~ ;copy-resources;aux-compile
    ```

4. Browse to http://localhost:8080/builder?uri=.

5. NOTE: Building the solr index will be a slightly different command in this case, because running via sbt does NOT add /widgets to 
   the base url, and the default port is 8080:

   ``` 
    $ curl -s -u username:password -X POST  \
      http://127.0.0.1:8080/updates/rebuild/index
   ```

### Run the tests

   ```
    $ bin/sbt
    > test
   ```

or to run only a few tests:

  ```
    > test-only edu.duke.oit.solr.test.GrantSpec edu.duke.oit.solr.test.CourseSpec
  ```

 
## Known Issues

* There is an old version of a Jena listener in ``listener/src`` that will no
  longer work with this version of vivo widgets.  It needs to be updated to
support a new format of the json to update widgets.
