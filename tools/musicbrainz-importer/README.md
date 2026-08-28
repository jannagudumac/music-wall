# MusicBrainz catalogue importer

This is an optional development tool used to prepare Music Wall's initial
catalogue. It is deliberately separate from the Spring Boot application.

```text
MusicBrainz -> this tool -> ../../database/catalogue_seed.sql -> PostgreSQL
```

The tool reads the selected artists in `artists.json`, retrieves a small set of
albums, tracks and genres, and writes only Music Wall catalogue fields. The
MusicBrainz identifiers are used transiently inside this directory and never
appear in the generated SQL.

Album covers use the direct Cover Art Archive release-group URL as the stored
`coverUrl`. There is no cover downloader, proxy, or redirect-processing layer.

## Run

Requirements: Java 17 and Maven.

Set an identifiable MusicBrainz user-agent, then run from this directory:

```powershell
$env:MUSICBRAINZ_USER_AGENT = 'MusicWallStudentProject/1.0 (your-contact@example.com)'
mvn test
mvn exec:java
```

Optional arguments select another artist file and output file:

```powershell
mvn exec:java -Dexec.args='artists.json ../../database/catalogue_seed.sql'
```

The generated seed is intended for an empty catalogue schema. It contains only
`artist`, `album`, `track`, `genre`, `album_genre`, and `track_genre` data. It
does not contain users, walls, sections, memberships, or listening states.

After the seed has been loaded, this entire tool directory can be removed and
Music Wall continues to operate normally from PostgreSQL.
