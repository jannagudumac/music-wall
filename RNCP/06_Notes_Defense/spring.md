# Spring Boot — notes de soutenance

## Présentation courte

Le backend utilise Java 17 et Spring Boot 3.5. Il expose une API REST au format JSON et suit une architecture en couches. Chaque couche a une responsabilité précise, ce qui facilite la lecture, les tests et la maintenance.

## Organisation des couches

### Controller

Le controller reçoit la requête HTTP, lit les paramètres ou le JSON, puis délègue le traitement au service. Il retourne ensuite un DTO et le code HTTP adapté.

Exemple : `MusicWallController` reçoit la demande de création d'un mur et transmet le nom de l'utilisateur authentifié à `MusicWallService`.

### DTO

Un DTO définit les données échangées avec l'API. Il évite d'exposer directement les entités JPA et permet d'ajouter la validation des entrées.

Les champs contrôlés par le serveur, comme l'identifiant et le propriétaire, ne sont pas copiés depuis la requête lors d'une création ou d'une modification.

### Service

Le service contient la logique métier : création des murs, contrôle des accès, gestion des membres, validation d'un élément musical et conversion entre entités et DTO.

### Repository

Le repository lit et enregistre les données avec Spring Data JPA. `JpaRepository` fournit déjà les opérations CRUD principales. Les noms de certaines méthodes permettent à Spring de générer automatiquement la requête.

Exemple : `findDistinctByOwnerUsernameOrMembersUsernameOrderByIdDesc` récupère les murs dont l'utilisateur est propriétaire ou membre, supprime les doublons et trie les résultats du plus récent au plus ancien.

### Projection

Une projection est une petite interface qui reçoit seulement certaines colonnes d'une requête. Dans Music Wall, `CatalogSuggestionProjection` récupère uniquement l'identifiant, le type, le titre, le sous-titre et le score nécessaires à l'autocomplétion. Cela évite de charger une entité complète pour un résultat très court.

### Entity

Une entité représente une table PostgreSQL. Les annotations JPA décrivent les colonnes, les identifiants et les relations.

### Exception

`GlobalExceptionHandler` transforme les erreurs connues en réponses JSON cohérentes. Le frontend reçoit ainsi un code HTTP et un message qu'il peut afficher.

## Exemple de parcours d'une requête

Pour ajouter un morceau à une section :

1. Angular envoie une requête HTTP avec le JWT.
2. `JwtFilter` authentifie l'utilisateur.
3. `MusicItemController` reçoit la requête.
4. `MusicItemService` vérifie la règle métier.
5. `WallAccessService` contrôle l'accès au mur.
6. `MusicItemRepository` enregistre l'entité.
7. Hibernate produit la requête SQL pour PostgreSQL.
8. Le backend retourne un `MusicItemDTO` au frontend.

## Injection de dépendances

Spring crée les objets annotés avec `@Controller`, `@Service`, `@Repository` ou `@Component`, puis fournit leurs dépendances par constructeur.

Cette approche évite de créer manuellement les services et repositories. Elle facilite aussi les tests, car une dépendance réelle peut être remplacée par un mock.

## Transactions

`@Transactional` regroupe plusieurs opérations dans une transaction. Si une erreur se produit, les modifications peuvent être annulées ensemble.

- `@Transactional(readOnly = true)` est utilisé pour les lectures.
- `@Transactional` est utilisé pour les créations, modifications et suppressions.

Par exemple, la suppression d'un mur supprime d'abord ses éléments, puis ses sections, puis le mur dans la même transaction.

## Validation des requêtes

Le controller utilise `@Valid` avec les annotations présentes dans les DTO :

- `@NotBlank` refuse une chaîne vide ;
- `@Size` limite la longueur ;
- `@Pattern` vérifie un format, par exemple une couleur hexadécimale.

Si la validation échoue, `GlobalExceptionHandler` retourne une réponse HTTP 400 avec un message lisible.

## JPA et Hibernate

JPA est la spécification Java utilisée pour décrire les entités et leurs relations. Hibernate est l'implémentation qui transforme ces opérations Java en requêtes SQL PostgreSQL.

Exemples de relations utilisées :

- `@ManyToOne` entre un mur et son propriétaire ;
- `@ManyToMany` entre un mur et ses membres ;
- `@ManyToOne` entre une section et son mur ;
- `@ManyToMany` entre les albums, les morceaux et les genres.

## Pourquoi ne pas créer de classes DAO séparées ?

Dans ce projet, les repositories Spring Data JPA remplissent déjà le rôle de DAO. Ajouter une deuxième couche DAO qui appelle seulement les repositories ajouterait du code sans apporter de comportement utile.

## Pourquoi utiliser des DTO ?

- Ils empêchent d'exposer le mot de passe ou des relations JPA internes.
- Ils contrôlent la forme du JSON.
- Ils portent les règles de validation de l'API.
- Ils permettent au service de décider quels champs sont modifiables.

## Tests

Les services sont testés avec JUnit et Mockito. Les repositories sont remplacés par des mocks afin de tester uniquement la logique métier.

Les tests d'intégration utilisent Spring Boot, MockMvc et H2 pour vérifier plusieurs composants ensemble, depuis la requête HTTP jusqu'au traitement backend.

Le projet contient actuellement 17 tests backend. Deux tests Angular vérifient aussi le comportement de l'intercepteur après les réponses HTTP 401 et 403.
