# AEM Data Exporter

The AEM Data Exporter has been designed to crawl a sites structure and determine all of the ```DAM``` assets which are referenced on that site and to bundle them together into content packages which can then be uploaded to other AEM environments. The program has been designed to perform the following operations:
1. Crawl the specified path and identify all of the other child/sibling pages (up to a specified depth).
2. Crawl each page and identify any referenced ```DAM``` content.
   1. Crawl each of the identified ```DAM``` content and find any additional references to other ```DAM``` content (up to a specified depth).
3. Create multiple package definitions (chunks of 100) containing the specified path, and all identified ```DAM``` content.
4. Upload the package definitions to the AEM instance.
5. Build the package definitions in the AEM instance.
6. Download the package definitions from the AEM instance.
7. Remove the package definitions from the AEM instance.

## Building Using Maven

##### Creating and Running a Self-Contained JAR

```mvn clean install```

The jar can be run using something like the following commands (see Usage for more details):

```java -jar target/data-exporter.jar-jar-with-dependencies.jar -host http://localhost:4502 -path /content/geometrixx -username admin -password admin -packageName content-geometrix -maxPageDepth 100 -maxDAMDepth 100```

A number of other jars are generated which can be used to run specific phases independently:

- ```data-exporter_installer.jar-jar-with-dependencies.jar``` - used to install bundles in the specified host.
- ```data-exporter_remover.jar-jar-with-dependencies.jar``` - used to remove uploaded bundles from a specified host. **NOTE:** This does not uninstall the bundles content, it simply removes the zip file from the JCR in order to save on space.
- ```data-exporter_uploader.jar-jar-with-dependencies.jar``` - used to upload bundles into the specified host.

## Usage

The AEM Data Exporter has been developed as a simple Java application which can either be run through the command line, or through an IDE. It is assumed that the environment has Maven and Java 7 available to it.

##### Program Arguments

The AEM Data Exporter expects the following program arguments:

- ```-host``` - Host location of the AEM instance to connect to.
- ```-path``` - Path to retrieve content from.
- ```-username``` - Username to authenticate with.
- ```-password``` - Password to authenticate with.
- ```-packageName``` - Name of the packages to create.
- ```-maxPageDepth``` (optional) - Maximum depth of child pages to identify.
- ```-maxDAMDepth``` (optional) - Maximum depth of DAM content to identify.

###### Examples

- ```-host http://localhost:4502 -path /content/geometrixx -username admin -password admin -packageName geometrixx```
  - Will crawl for pages starting at ```/content/geometrixx``` and find all child/sibling pages that exist.
  - Will crawl each page and find all ```/content/dam``` references and recursively find all ```/content/dam``` references for the identified ```DAM` content.
  - Will create numbered packages starting with the name ```geometrixx``` and upload to ```http//localhost:4502```.
  - Will build the packages.
  - Will download the packages.
  - Will remove the packages.
- ```-host http://localhost:4502 -path /content/geometrixx -username admin -password admin -packageName geometrixx -maxPageDepth 0```
  - Will crawl for pages starting at ```/content/geometrixx``` but NOT find any child/sibling pages that exist.
  - Will crawl each page and find all ```/content/dam``` references and recursively find all ```/content/dam``` references for the identified ```DAM` content.
  - Will create numbered packages starting with the name ```geometrixx``` and upload to ```http//localhost:4502```.
  - Will build the packages.
  - Will download the packages.
  - Will remove the packages.