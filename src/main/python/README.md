
## Index documents into Search-API

Through POST requests to `/documents/{id}` new documents will be added to the cross-lingual search service.  

Below we detail how to do this from the dump created by TBFY, but it could be done from any other external source and using any other web client.

## Quick Start

1. Download the latest data dump, in JSON format, published by TBFY at Zenodo:     
    [https://doi.org/10.5281/zenodo.3783736](https://doi.org/10.5281/zenodo.3783736)
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

## Lastest Stable Release [![](https://jitpack.io/v/TBFY/search-API.svg)](https://jitpack.io/#TBFY/search-API)
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
Please take a look at our [contributing](https://github.com/TBFY/general/blob/master/guides/how-to-contribute.md) guidelines if you're interested in helping!
