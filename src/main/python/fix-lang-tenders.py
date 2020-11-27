#!/usr/bin/env python3
# docker run -d -p 6200:5000 librairy/bio-nlp:latest
import tarfile
import urllib.request
import json
import requests
import pysolr
import os
from langdetect import detect
from langdetect import detect_langs
import multiprocessing as mp
from datetime import datetime
import time

initial = 0

# Setup a Solr instance. The timeout is optional.
solr = pysolr.Solr('http://librairy.linkeddata.es/data/tbfy', timeout=2)


def get_language(text):
    try:
        lang = detect(text)
        return lang
    except:
        print("lang detect error!!")
        return "en"

def get_document(tender):
    if (not 'txt_t' in tender):
        return tender
    old_lang = tender['lang_s']
    new_lang = get_language(tender['txt_t'])
    if (old_lang != new_lang and len(detect_langs(tender['txt_t'])) == 1):
        print("["+old_lang+"->"+new_lang+"] for '" + tender['name_s'] + "'")
        tender['lang_s'] = new_lang
    return tender

pool = mp.Pool(4)

counter = 0
completed = False
window_size=1000
cursor = "*"
while (not completed):
    old_counter = counter
    solr_query = "source_s:tender"
    try:
        tenders = solr.search(q=solr_query,rows=window_size,cursorMark=cursor,sort="id asc")
        cursor = tenders.nextCursorMark
        counter += len(tenders)
        documents = pool.map(get_document, tenders)
        print("[",datetime.now(),"] solr indexing..")
        solr.add(documents)
        solr.commit()
        print("[",datetime.now(),"] solr index updated! ",counter)
        if (old_counter == counter):
            print("done!")
            break
    except Exception as e:
        print(repr(e))
        print("Solr query error. Wait for 5secs..")
        time.sleep(5.0)
        #solr.commit()

print(counter,"tenders successfully fixed")
pool.close()
