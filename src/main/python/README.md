
# Index documents into Search-API

Through POST requests to `/documents/{id}` new documents will be added to the cross-lingual search service.  

Below we detail how to do this from the dump created by TBFY, but it could be done from any other external source and using any other web client.

## Quick Start

1. Download the latest data dump, in JSON format, published by TBFY at Zenodo:     
    [https://doi.org/10.5281/zenodo.3783736](https://doi.org/10.5281/zenodo.3783736)
1. Unzip it, for example in `/tmp`. A new folder is created for each month.         
1. Download the indexing script. It is implemented in Python, but is easily exportable to other languages:    
    [http://tbfy.librairy.linkeddata.es/search-api/src/main/python/index-tenders.py](https://github.com/TBFY/search-API/blob/master/src/main/python/index-tenders.py)
1. Edit it to set the root directory where the documents are. For example `/tmp`:
    ````
    main('/tmp/20*')
    ````
   As you can see, a filtering of directories to be indexed can be defined in the path itself by adding `*` characters.
1. Run it! That's it.

## Manual Indexing

Documents can also be added manually through the swagger interface on the [/documents/{id}](https://tbfy.librairy.linkeddata.es/search-api) resource.

A valid ID (e.g `ocds-0c46vo-0133-000516-2019_000516-2019_td`) and, at least, the text and name of the document must be provided.

```json
{
  "date": "2020-05-29T00:00:00Z",
  "source": "tender",
  "name": "Fourniture de lasers destines  la ralisation du projet de recherche Pasquans (Flagship Quantum Technology)",
  "text":"Voir cahier des charges (CCP Laser LKB 2018). Voir cahier des charges (CCP Laser LKB 2018)."
}
```

## Contributing
Please take a look at our [contributing](https://github.com/TBFY/general/blob/master/guides/how-to-contribute.md) guidelines if you're interested in helping!
