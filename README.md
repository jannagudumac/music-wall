# Music Wall — RNCP defense version

Music Wall turns the idea of a physical listening wall into a collaborative web application. A registered user creates a wall, organises albums or tracks into coloured sections, marks them `TO_LISTEN` or `LISTENED`, and directly adds other registered users as collaborators.

This repository is the deliberately simplified RNCP version. It favours a conventional Angular → Spring Boot → PostgreSQL architecture that can be explained and diagrammed clearly.

## MVP

- Register, log in and log out with a unique username and password.
- Authenticate protected requests with a JWT; store passwords as BCrypt hashes.
- View and edit a simple profile with bio and avatar.
- Create, view, rename, style and delete owned music walls.
- Create, edit and delete coloured sections.
- Search the local Artist/Album/Track catalogue and view details.
- Add exactly one catalogue album or track to a section.
- Change an item's status between `TO_LISTEN` and `LISTENED`.
- Search registered usernames and directly add or remove wall members.
- Let owners and members manage sections and items; reserve wall settings and membership management for the owner.
- Return from a catalogue detail to the original wall section through query parameters and a fragment.

There is intentionally no email identity, pending collaboration state or complex wall role. Possible V2 work is listed at the end.

## Technologies

| Area | Technology |
|---|---|
| Frontend | Angular 19, TypeScript 5.7, RxJS 7.8, HTML/CSS |
| Backend | Java 17, Spring Boot 3.5, Spring Web, Validation, Security, Data JPA |
| Security | JWT (JJWT), BCrypt, stateless Spring Security |
| Database | PostgreSQL 16, Hibernate |
| Initial catalogue source | MusicBrainz, through a separate development tool |
| Tests | JUnit 5, Mockito, MockMvc, H2 in PostgreSQL compatibility mode, Jasmine/Karma |
| Packaging | Maven Wrapper, npm, Docker, Docker Compose |

## Project structure

```text
music-wall/
├── backend/
│   ├── src/main/java/com/musicwall/
│   │   ├── controller/   HTTP entry points
│   │   ├── service/      business rules
│   │   ├── repository/   Spring Data database access
│   │   ├── entity/       JPA persistence model
│   │   ├── dto/          request and response contracts
│   │   ├── security/     JWT and Spring Security
│   │   └── exception/    shared HTTP error handling
│   ├── src/test/         focused unit and integration tests
│   └── Dockerfile
├── frontend/src/app/
│   ├── components/       pages and wall subcomponents
│   ├── services/         API calls and shared behaviour
│   ├── models/           TypeScript API shapes
│   ├── guards/           protected-route check
│   └── interceptors/     Bearer-token attachment
├── docs/                 RNCP diagrams and educational guide
├── create-database.sql
└── docker-compose.yml
```

## Prerequisites

For local development:

- Java 17 or newer
- Node.js 20+ and npm
- PostgreSQL 16 (another supported recent PostgreSQL version should also work)

For the container route, Docker Desktop is enough for the backend and database. Angular remains local by design.

## Database isolation and local setup

The backend defaults to the dedicated database name `music_wall_rncp`. It must never be pointed at the database of the preserved full application.

Create the clean database:

```powershell
psql -U postgres -f create-database.sql
```

Then configure the backend:

```powershell
Copy-Item backend/.env.example backend/.env
```

Required values in `backend/.env`:

```properties
DB_URL=jdbc:postgresql://localhost:5432/music_wall_rncp
DB_USERNAME=postgres
DB_PASSWORD=replace_me
JWT_SECRET=replace_with_a_random_secret_of_at_least_32_characters
```

`JWT_SECRET`, `DB_USERNAME` and `DB_PASSWORD` have no committed production fallback. Hibernate creates/updates the RNCP schema; `schema.sql` adds the PostgreSQL trigram extension and catalogue search indexes after the tables exist.

Populate a fresh catalogue after the backend has created its tables:

```powershell
psql -U postgres -d music_wall_rncp -f database/catalogue_seed.sql
```

The seed contains catalogue reference data only. It never creates users, walls,
members, sections or listening states.

## Catalogue architecture

Normal application use is deliberately local:

```text
Angular -> Spring Boot -> PostgreSQL
```

MusicBrainz is not a runtime dependency. It is used only through the optional
standalone utility in `tools/musicbrainz-importer/` to prepare the initial SQL
seed:

```text
MusicBrainz -> external importer -> catalogue_seed.sql -> PostgreSQL
```

The backend has no MusicBrainz service, startup runner, provider configuration,
or provider identifiers in its entities and REST DTOs. Search and detail pages
therefore continue to work when the internet or MusicBrainz is unavailable. The
importer directory can be removed after database preparation without affecting
the application.

## Wall detail component styles

`WallDetailComponent` owns the complete page layout, wallpaper and section-grid
positioning. `WallHeaderComponent`, `WallMembersComponent`,
`WallSectionComponent`, `MusicItemComponent` and `CatalogueSearchComponent`
each own the CSS for their own markup. Normal Angular style encapsulation is
used; there is no wall-wide `ViewEncapsulation.None` or styling framework.

## Start locally

Backend, from `backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Frontend, from `frontend/`:

```powershell
npm install
npm start
```

Open `http://localhost:4200`. The frontend calls `http://localhost:8080/api` by default.

## Tests and builds

Backend unit and integration tests:

```powershell
cd backend
.\mvnw.cmd test
```

The integration test uses an in-memory H2 database only under the `test` profile. It covers registration through Controller → Service → Repository, checks persistence and verifies that BCrypt—not plain text—is stored.

Frontend test and production build:

```powershell
cd frontend
npm test
npm run build
```

The normal backend tests cover the local catalogue and focused wall/access/item
behaviour without internet access. Provider transformation tests live with the
optional external importer and use local JSON fixtures only.

## Docker

Copy the root environment example and replace all secrets:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Compose starts two containers:

- `postgres`: PostgreSQL with database `music_wall_rncp`, exposed on host port `55432` by default;
- `backend`: the Spring Boot image, exposed on host port `8080`, connecting to PostgreSQL through the Compose service name `postgres`.

The named volume `music_wall_rncp_data` preserves only this RNCP database. Stop containers with `docker compose down`; add `-v` only when you intentionally want to erase that RNCP volume.

An image is the packaged template built from a Dockerfile. A container is a running instance of an image. Compose describes how the two containers, environment variables, ports, health check and private network fit together.

## Main API endpoints

All endpoints except registration/login and public catalogue/profile reads require `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register and receive a JWT |
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| GET/PUT | `/api/profiles/{username}`, `/api/profiles/me` | Read/update a simple profile |
| POST/GET | `/api/profiles/me/avatar`, `/api/profiles/{username}/avatar` | Upload/read avatar bytes |
| GET | `/api/catalog/search?query=...` | Search local catalogue |
| GET | `/api/catalog/suggestions?query=...` | Autocomplete suggestions |
| GET | `/api/catalog/{artists|albums|tracks}/{id}` | Catalogue detail |
| POST/GET | `/api/walls` | Create/list accessible walls |
| GET/PUT/DELETE | `/api/walls/{wallId}` | Read or owner-update/delete wall |
| PUT | `/api/walls/{wallId}/appearance` | Owner changes colour/wallpaper |
| GET | `/api/walls/{wallId}/members` | Owner reads direct members |
| GET | `/api/walls/{wallId}/members/search?query=...` | Owner searches candidates |
| POST | `/api/walls/{wallId}/members` | Owner directly adds a username |
| DELETE | `/api/walls/{wallId}/members/{username}` | Owner removes a member |
| POST/PUT/DELETE | `/api/walls/{wallId}/sections[/{sectionId}]` | Owner/member manages sections |
| POST/PUT/DELETE | `/api/walls/{wallId}/sections/{sectionId}/items[/{itemId}]` | Owner/member manages items |

Controllers return DTOs directly for normal JSON and use `@ResponseStatus` for fixed `201`/`204` responses. `ResponseEntity<byte[]>` remains only for avatars because their media type is dynamic. `GlobalExceptionHandler` turns service exceptions and validation failures into consistent HTTP errors.

## Security overview

1. Registration hashes the password with BCrypt. Login asks Spring Security to verify the submitted password.
2. `JwtUtil` signs a token whose subject is the unique username; the global `ROLE_USER` remains because it fits Spring authorities without adding complexity.
3. Angular stores the token after authentication. `authInterceptor` adds it to backend requests.
4. `JwtFilter` validates the Bearer token, loads the user and places an authenticated object in `SecurityContext`.
5. `SecurityConfig` applies stateless access rules and CORS. `authGuard` prevents unauthenticated navigation in the browser, but backend security remains authoritative.
6. DTO validation limits incoming data. JPA parameters avoid handwritten SQL concatenation, Angular escapes interpolation by default, and stateless Bearer authentication avoids cookie-based CSRF exposure.

## Architecture and data model

The backend follows one readable path:

```text
Angular component → Angular service/HttpClient → Controller → Service
                  → Repository → JPA/Hibernate → PostgreSQL
```

The collaboration rule is equally direct:

```text
MusicWall.owner   = exactly one User
MusicWall.members = zero or more distinct Users (owner excluded)
```

`WallAccessService` is the only small authorization helper. It answers owner/access questions; it is not a general permission framework. Wall member operations intentionally remain in `MusicWallController` and `MusicWallService` because membership is part of the wall resource.

The detailed MCD, MLD/MPD, UML, use cases, sequences, three-tier diagram, class responsibilities and junior-friendly concept guide are in [docs/architecture-and-defense.md](docs/architecture-and-defense.md).

## Manual acceptance flows

Before a defense/release, verify:

1. Register/login and receive a JWT.
2. Create a wall; confirm the creator is owner but is not duplicated in members.
3. Update wall and appearance; create/edit/delete a section.
4. Search catalogue; add one album or track; toggle listening status; delete it.
5. Search a registered username; add it directly; open and edit the wall as that member.
6. Remove the member and confirm access is denied.
7. Update bio and upload a JPEG/PNG/WebP avatar.
8. From a wall section open catalogue/detail and return to that wall and fragment.

## V2 ideas

Future versions could add a friend system, friend requests, invitation approval, richer member roles, favourites, public-profile controls, genre statistics or concert discovery. They are not partially implemented in this RNCP codebase.
