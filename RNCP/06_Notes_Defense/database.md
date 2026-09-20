# Base de données — notes de soutenance

## Présentation courte

Music Wall utilise PostgreSQL 16. La base contient les comptes utilisateurs, les murs musicaux, leurs membres et leurs sections, ainsi qu'un catalogue local d'artistes, d'albums, de morceaux et de genres.

La base locale s'appelle `music_wall_rncp`. Elle est lancée avec Docker Compose. En production, l'application utilise une base PostgreSQL séparée sur Render.

## Les trois niveaux de modélisation

- Le MCD présente les entités métier et leurs relations, sans dépendre d'un logiciel précis.
- Le MLD transforme les relations plusieurs-à-plusieurs en tables d'association et ajoute les clés étrangères.
- Le MPD représente la structure PostgreSQL réellement utilisée : tables, colonnes, types et contraintes.

Les diagrammes se trouvent dans `RNCP/04_Diagrammes`.

## Tables principales

La base contient onze tables :

- `app_user` : comptes, rôles, biographies et avatars ;
- `music_wall` : murs et propriétaires ;
- `wall_members` : membres partageant un mur ;
- `music_section` : sections d'un mur ;
- `music_item` : albums ou morceaux enregistrés dans une section ;
- `artist`, `album`, `track`, `genre` : catalogue musical ;
- `album_genre` et `track_genre` : associations entre le catalogue et les genres.

## Relations importantes

- Un utilisateur peut posséder plusieurs murs.
- Un mur possède un seul propriétaire et peut avoir plusieurs membres.
- Un mur contient plusieurs sections.
- Une section contient plusieurs éléments musicaux.
- Un artiste peut avoir plusieurs albums et plusieurs morceaux.
- Un album peut contenir plusieurs morceaux.
- Un album ou un morceau peut appartenir à plusieurs genres.

Les relations plusieurs-à-plusieurs utilisent trois tables d'association :

- `wall_members` avec la clé primaire composée `(wall_id, user_id)` ;
- `album_genre` avec la clé primaire composée `(album_id, genre_id)` ;
- `track_genre` avec la clé primaire composée `(track_id, genre_id)`.

Ces clés composées empêchent d'enregistrer deux fois la même association.

## Contraintes utiles

- `app_user.username`, `artist.name` et `genre.name` sont uniques.
- La combinaison `(album.artist_id, album.title)` est unique.
- `track.album_id` peut être vide, car un morceau peut exister sans album.
- `music_item.catalog_track_id` et `music_item.catalog_album_id` sont individuellement facultatifs.
- Le service impose cependant une règle métier : un `music_item` référence exactement un morceau ou un album, jamais les deux et jamais aucun.

Cette dernière règle est contrôlée dans `MusicItemService`. Elle n'est pas actuellement représentée par une contrainte `CHECK` PostgreSQL.

## JPA et Hibernate

Les classes annotées avec `@Entity` représentent les tables. Les annotations `@ManyToOne`, `@ManyToMany`, `@JoinColumn` et `@JoinTable` décrivent les relations.

Hibernate met à jour la structure grâce à `spring.jpa.hibernate.ddl-auto=update`. Le fichier `schema.sql` active aussi l'extension `pg_trgm` et crée les index utiles pour la recherche.

Les valeurs Java `itemType`, `status` et `wallpaper` sont enregistrées comme du texte avec `EnumType.STRING`. La base contient donc des valeurs compréhensibles comme `TRACK`, `ALBUM`, `TO_LISTEN` ou `LISTENED`, au lieu de simples numéros.

## Recherche dans le catalogue

Le catalogue est stocké localement pour que le fonctionnement principal ne dépende pas d'une API musicale externe.

La recherche utilise PostgreSQL `pg_trgm` :

- `LIKE` trouve les textes contenant la recherche ;
- `similarity` trouve aussi des orthographes proches ;
- les index GIN accélèrent la recherche sur les noms et les titres.

Les requêtes natives utilisent des paramètres Spring Data. La saisie utilisateur n'est pas concaténée directement dans le SQL.

## Initialisation locale

Docker Compose démarre PostgreSQL, puis le backend. Le service `catalogue-seed` vérifie ensuite le nombre d'artistes.

- Si le catalogue est vide, il importe `database/catalogue_seed.sql`.
- S'il contient déjà des artistes, l'import est ignoré.

Ce mécanisme évite de réinsérer les mêmes données à chaque démarrage.

## Avatar

L'image de profil est conservée dans `app_user.avatar_image` avec le type PostgreSQL `BYTEA`. Le champ `avatar_content_type` mémorise le type du fichier, par exemple `image/png` ou `image/jpeg`, afin que le backend renvoie la bonne valeur HTTP `Content-Type`.

## Limites et amélioration possible

Le projet utilise actuellement `ddl-auto=update`, ce qui est simple pour une application étudiante. Pour un projet de production plus important, j'utiliserais Flyway ou Liquibase afin de versionner chaque modification du schéma et de pouvoir la reproduire de manière contrôlée.
