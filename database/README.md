# Initial catalogue seed

`catalogue_seed.sql` contains only provider-independent reference data:

- artists;
- albums;
- tracks;
- genres;
- album/genre relationships;
- track/genre relationships.

It contains no user-generated or authentication data. Load it only after the
Spring backend has created an empty schema:

```powershell
psql -U postgres -d music_wall_rncp -f database/catalogue_seed.sql
```

The committed seed preserves the numeric catalogue IDs from the audited local
catalogue so existing Music Wall item references remain valid during migration.
It was restored into a disposable provider-free schema and verified before the
former provider columns were removed.

The optional generator is documented in `tools/musicbrainz-importer/README.md`.
