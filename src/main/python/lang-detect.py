#!/usr/bin/env python3
from langdetect import detect

text="[u'Ausschreibung geht \\xfcber die Lieferung von Gefahrenmeldeanlagen f\\xfcr die Verwendung in der \\xfcberbetrieblichen Ausbildung und Meisterausbildung Elektrotechnik. Die kompletten Gefahrenmeldeanlagen sollen inklusive Zubeh\\xf6r, wie zum Beispiel Taubleau, Smart Key, Bewegungsmeldern und Gateway zu KNX geliefert werden. Details siehe Leistungsverzeichnis.']"
lang = detect(text)
print(lang)
