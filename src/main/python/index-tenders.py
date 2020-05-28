#!/usr/bin/env python3
import json
import requests
import asyncio
from concurrent.futures import ThreadPoolExecutor
from timeit import default_timer
import os
import time
import glob
import sys

START_TIME = default_timer()

def PrintException():
    exc_type, exc_obj, tb = sys.exc_info()
    f = tb.tb_frame
    lineno = tb.tb_lineno
    filename = f.f_code.co_filename
    linecache.checkcache(filename)
    line = linecache.getline(filename, lineno, f.f_globals)
    print('EXCEPTION IN ({}, LINE {} "{}"): {}'.format(filename, lineno, line.strip(), exc_obj))


def fetch(session,file_url):
    if (not os.path.exists(file_url)):
        print(file_url, "not exists")
        return
    try:
        with open(file_url) as f:
          data = json.load(f)
          if ('releases' in data):
              for release in data['releases']:
                if ('tender' in release):
                    tender_data = release['tender']
                    document = {}
                    if ('description' in tender_data):
                        id=tender_data['id']
                        base_url = 'http://tbfy.librairy.linkeddata.es/search-api/documents/'
                        document['name']=tender_data['title']
                        if ('status' in tender_data):
                            document['tags']=tender_data['status']
                        document['text']=tender_data['description']
                        document['source']="tender"
                        document['date']=data['publishedDate']
                        with session.post(base_url + id, json=document) as response:
                            elapsed = default_timer() - START_TIME
                            time_completed_at = "{:5.2f}s".format(elapsed)
                            print("{0:<30} {1:>20}".format(file_url, response.status_code))
                            
    except Exception as e:
        PrintException()
    

async def index_documents(directory):
        
    print("{0:<30} {1:>20}".format("Files from " + directory, "Status"))
    
    with ThreadPoolExecutor(max_workers=8) as executor:
        with requests.Session() as session:
                        
            # Set any session parameters here before calling `fetch`
            loop = asyncio.get_event_loop()
            START_TIME = default_timer()
            tasks = [
                loop.run_in_executor(
                    executor,
                    fetch,
                    *(session, file) # Allows us to pass in multiple arguments to `fetch`
                )
                for file in glob.glob(directory+"/*.json")
            ]
            for response in await asyncio.gather(*tasks):
                pass


def main(path):
    # Articles
    directories = glob.glob(path)            

    for directory in directories:
        if (os.path.isfile(directory)):
            continue
        
        loop = asyncio.get_event_loop()
        future = asyncio.ensure_future(index_documents(directory))
        loop.run_until_complete(future)

                
main('/Users/cbadenes/Downloads/20*')
