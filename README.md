<p align="center"><img width=15% src="https://github.com/TBFY/general/blob/master/figures/tbfy-logo.png"></p>
<p align="center"><img width=40% src="https://github.com/TBFY/search-API/blob/master/logo.png"></p>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
![Java](https://img.shields.io/badge/java-v1.8+-blue.svg)
![Maven](https://img.shields.io/badge/maven-v3.0+-blue.svg)
[![Build Status](https://travis-ci.org/TBFY/search-API.svg?branch=master)](https://travis-ci.org/TBFY/search-API)
[![Release Status](https://jitci.com/gh/TBFY/search-API/svg)](https://jitci.com/gh/TBFY/search-API)
[![GitHub Issues](https://img.shields.io/github/issues/TBFY/search-API.svg)](https://github.com/TBFY/search-API/issues)
[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)



## Basic Overview

Explore collections of multilingual public procurement data through a Restful API:
- `/documents` : list of existing documents 
- `/documents/{id}` : details of a document
- `/documents/{id}/items` : similar documents

Or search for a similar document given a text:
- `/items` : similar documents

## Quick Start

1. A Swagger-based API is available online at:     
    [http://tbfy.librairy.linkeddata.es/search-api](http://tbfy.librairy.linkeddata.es/search-api)
1. Get the list of available documents, and filter by language or source, using `/documents`:     
    [http://tbfy.librairy.linkeddata.es/search-api/documents](http://tbfy.librairy.linkeddata.es/search-api/documents?size=10)
1. Get the content, and additional information, of a document through `/documents/{id}`:    
    [http://tbfy.librairy.linkeddata.es/search-api/documents/jrc32002D0996-en](http://tbfy.librairy.linkeddata.es/search-api/documents/jrc32002D0996-en?text=true)
1. Obtain similar documents, regardless of language, through `/documents/{id}/items`:
    [http://tbfy.librairy.linkeddata.es/search-api/documents/jrc32002D0996-en/items](http://tbfy.librairy.linkeddata.es/search-api/documents/jrc32002D0996-en/items?size=10&source=jrc)
1. To obtain only documents in Spanish, just add `lang=es` to the query:    
    [http://tbfy.librairy.linkeddata.es/search-api/documents/jrc32002D0996-en/items?lang=es](http://tbfy.librairy.linkeddata.es/search-api/documents/jrc32002D0996-en/items?size=10&source=jrc&lang=es)

Similar documents to a free text can also be searched. All you have to do is make a HTTP-POST request with a json like this at :
```json
{
  "size": 10,
  "source": "jrc",
  "text": "Council Directive 9343EEC on the hygiene of foodstuffs as regards the transport of bulk liquid oils and fats by seaText with EEA relevance."
}
```
In order to obtain only documents in Spanish, just add `lang=es` to the json:
```json
{
  "size": 10,
  "source": "jrc",
  "text": "Council Directive 9343EEC on the hygiene of foodstuffs as regards the transport of bulk liquid oils and fats by seaText with EEA relevance.",
  "lang":"es"
}
```

## Last Stable Release [![](https://jitpack.io/v/TBFY/search-API.svg)](https://jitpack.io/#TBFY/search-API)
This tool is part of the [librAIry](http://librairy.linkeddata.es) ecosystem, and needs [librAIry-API](https://github.com/librairy/api) for deployment.

* It can start as a service via [docker-compose.yml](https://github.com/TBFY/search-API/blob/master/docker-compose.yml):
* Or through Maven dependencies:
    1. Add the JitPack repository to your build file   
    ```xml
        <repositories>
		      <repository>
		        <id>jitpack.io</id>
		        <url>https://jitpack.io</url>
		      </repository>
	      </repositories>
   ```
   2. Add the dependency
    ```xml
        <dependency>
	         <groupId>com.github.TBFY</groupId>
	         <artifactId>search-API</artifactId>
	         <version>last-stable-release-version</version>
      </dependency>
     ```

## Contributing
Please take a look at our [contributing](https://github.com/TBFY/harvester/blob/master/CONTRIBUTING.md) guidelines if you're interested in helping!
