---
parent: Requirements
---
# Fetchers

## Respect provider request limits
`req~fetchers.rate-limiting~1`

Fetchers with a documented request limit throttle requests across all fetcher instances. Limits expressed as requests per time interval are converted consistently to requests per second.

Needs: impl

## Reject external entities in XML responses
`req~fetchers.xml-xxe-prevention~1`

MODS, Medline, EndNote XML, Citavi, and MS Office (MSBib) XML imports as well as PICA, MARC, ISIDORE, arXiv, and Medline XML fetcher responses disable DTD processing so that external entities cannot be resolved.
Citation style (CSL) files are read with DTD processing disabled, too.
BibDesk group comments inside `.bib` files are Apple plists and thus carry a DOCTYPE declaration; there, external entity resolution is disabled and entity expansion is limited instead.

<!-- markdownlint-disable-file MD022 -->
