# Questions du jury — réponses courtes

Les réponses ci-dessous sont formulées pour pouvoir être dites directement pendant la soutenance.

## Projet et choix fonctionnels

### Quel problème résout Music Wall ?

Les recommandations musicales sont souvent dispersées entre des playlists, des messages et des notes. Music Wall permet de les organiser dans des murs partagés, avec des sections et un statut d'écoute.

### Pourquoi un mur plutôt qu'une playlist ?

Une playlist reste principalement une liste linéaire. Un mur permet de créer plusieurs sections thématiques, d'ajouter des albums ou des morceaux et de collaborer avec d'autres utilisateurs.

### Pourquoi avoir limité le MVP ?

Le périmètre initial était trop large. J'ai reporté les amis, invitations, favoris et statistiques afin de terminer une version cohérente, testée et déployée.

### Pourquoi utiliser un catalogue local ?

Je ne voulais pas que la fonction principale dépende de la disponibilité ou des limites d'une API externe. Le catalogue PostgreSQL garantit un fonctionnement stable et reproductible.

## Architecture

### Pourquoi une architecture en couches ?

Chaque couche a une responsabilité précise : le controller gère HTTP, le service contient la logique métier et le repository accède aux données. Cette séparation facilite les tests, la maintenance et l'explication du code.

### Quel est le parcours d'une requête ?

Angular envoie une requête JSON. Le controller la reçoit, le service applique les règles métier, le repository dialogue avec PostgreSQL par JPA et le backend retourne un DTO au frontend.

### Pourquoi utiliser des DTO ?

Les DTO contrôlent les données échangées avec l'API. Ils évitent d'exposer directement les entités JPA, les mots de passe et les relations internes.

### Pourquoi ne pas avoir créé de DAO séparés ?

Les repositories Spring Data JPA remplissent déjà ce rôle. Une couche DAO supplémentaire aurait seulement répété les mêmes appels dans ce projet.

### À quoi sert le dossier `projection` ?

Une projection récupère seulement les colonnes nécessaires à une requête. Elle est utilisée pour les suggestions du catalogue afin de retourner un résultat léger sans charger toutes les données d'une entité.

## Angular et TypeScript

### À quoi sert un composant Angular ?

Un composant gère une partie de l'interface. Il associe une classe TypeScript, un template HTML et des styles CSS.

### À quoi sert un service Angular ?

Le service centralise une logique réutilisable, notamment les appels HTTP vers le backend. Les composants restent ainsi concentrés sur l'affichage et les interactions.

### Qu'est-ce qu'un Observable ?

Un Observable représente une valeur qui arrivera plus tard, par exemple une réponse HTTP. Le composant s'abonne avec `subscribe` pour recevoir le résultat ou une erreur.

### Pourquoi utiliser RxJS dans la recherche ?

`debounceTime` attend une pause dans la saisie, `distinctUntilChanged` évite les recherches identiques et `switchMap` conserve uniquement la recherche la plus récente. Cela réduit les requêtes inutiles.

### À quoi sert le guard ?

Le guard empêche l'accès aux pages privées lorsqu'aucun token n'est enregistré. Il améliore la navigation, mais le backend vérifie toujours réellement le JWT et les permissions.

### À quoi sert l'intercepteur ?

Il ajoute automatiquement le JWT aux requêtes HTTP. Il distingue aussi une erreur 401, qui invalide la session, d'une erreur 403, qui indique seulement un refus d'accès.

### Comment communiquent un composant enfant et son parent ?

`@Input` transmet des données du parent vers l'enfant. `@Output` et `EventEmitter` permettent à l'enfant d'envoyer un événement au parent.

## Spring Boot et API

### Quelle différence entre controller et service ?

Le controller gère la requête et la réponse HTTP. Le service contient les règles métier et coordonne les repositories.

### À quoi sert `@Valid` ?

`@Valid` déclenche les règles présentes dans le DTO, par exemple `@NotBlank`, `@Size` ou `@Pattern`. Une entrée incorrecte est refusée avant l'exécution de la logique métier.

### À quoi sert `@Transactional` ?

Elle garantit qu'un ensemble de modifications réussit entièrement ou est annulé en cas d'erreur. `readOnly = true` indique qu'une méthode effectue uniquement une lecture.

### Pourquoi utiliser JPA et Hibernate ?

JPA permet de décrire les tables avec des objets Java. Hibernate traduit les opérations sur ces objets en requêtes SQL PostgreSQL.

### Comment les erreurs sont-elles gérées ?

Les services lancent des exceptions métier précises. `GlobalExceptionHandler` les transforme en réponses JSON avec les codes HTTP 400, 401, 403 ou 404.

## Base de données

### Pourquoi PostgreSQL ?

PostgreSQL est une base relationnelle robuste, adaptée aux relations du projet. Elle fournit aussi `pg_trgm`, utilisé pour la recherche approximative dans le catalogue.

### Pourquoi des tables d'association ?

Une relation plusieurs-à-plusieurs ne peut pas être stockée dans une seule clé étrangère. `wall_members`, `album_genre` et `track_genre` relient donc deux tables avec une clé primaire composée.

### Pourquoi `track.album_id` peut-il être nul ?

Certains morceaux peuvent exister indépendamment d'un album. Le morceau doit toujours avoir un artiste, mais son album reste facultatif.

### Pourquoi les deux références de `music_item` sont-elles nullables ?

Elles sont facultatives séparément parce que l'élément peut représenter soit un morceau, soit un album. Le service vérifie qu'une seule des deux références est renseignée.

### Pourquoi enregistrer le titre et l'artiste dans `music_item` ?

Ce sont des instantanés pratiques pour afficher rapidement le contenu enregistré. L'élément conserve aussi une clé étrangère vers l'album ou le morceau réel du catalogue.

### Comment le catalogue est-il initialisé ?

Après le démarrage du backend, Docker Compose vérifie si la table `artist` est vide. Si elle l'est, le service `catalogue-seed` importe le fichier SQL dans une transaction.

## Sécurité

### Quelle différence entre authentification et autorisation ?

L'authentification vérifie qui est l'utilisateur. L'autorisation vérifie ensuite ce qu'il a le droit de consulter ou de modifier.

### Pourquoi BCrypt ?

BCrypt produit un hash lent et salé, conçu pour les mots de passe. Le backend peut vérifier le mot de passe sans jamais enregistrer sa valeur en clair.

### Comment fonctionne le JWT ?

Après la connexion, le backend crée un token signé contenant le nom d'utilisateur et une date d'expiration. Angular l'envoie dans l'en-tête `Authorization`. Le backend vérifie sa signature avant d'authentifier la requête.

### Où le JWT est-il stocké ?

Il est stocké dans `localStorage` pour conserver la session après un rechargement. C'est une solution simple pour ce projet. Pour une application plus sensible, j'étudierais un cookie `HttpOnly` avec une protection CSRF adaptée.

### Pourquoi désactiver CSRF ?

L'API ne repose pas sur un cookie de session envoyé automatiquement par le navigateur. Elle est stateless et reçoit le JWT explicitement dans l'en-tête `Authorization`.

### À quoi sert CORS ?

CORS indique au navigateur quelle origine frontend peut appeler le backend. L'origine autorisée est fournie par la variable d'environnement `CORS_ORIGIN`.

### Quelle différence entre 401 et 403 ?

401 signifie que l'utilisateur n'est pas correctement authentifié. 403 signifie qu'il est authentifié, mais qu'il n'a pas la permission demandée.

### Comment empêchez-vous un membre de supprimer un mur ?

Le service récupère le nom de l'utilisateur depuis Spring Security puis appelle `findOwnedWall`. Si l'utilisateur n'est pas le propriétaire, le backend retourne une erreur 403.

## Tests et déploiement

### Quelle différence entre test unitaire et test d'intégration ?

Un test unitaire vérifie une classe isolée avec des mocks. Un test d'intégration vérifie plusieurs composants ensemble, par exemple une requête HTTP traitée par Spring Security et le controller.

### Que testent les tests Angular ?

Ils vérifient l'intercepteur HTTP. Une réponse 403 doit conserver la session, tandis qu'une réponse 401 doit déconnecter l'utilisateur et le rediriger vers la page de connexion.

### Pourquoi Docker Compose ?

Docker Compose démarre PostgreSQL, le backend, le catalogue et le frontend avec une configuration reproductible. Il évite de configurer chaque service manuellement.

### Pourquoi Netlify et Render ?

Netlify est adapté au build statique Angular. Render exécute le backend Spring Boot dans un conteneur Docker et fournit une base PostgreSQL séparée pour la production.

### Pourquoi la base de production est-elle séparée ?

La base locale sert au développement et aux tests manuels. La base Render contient les données de l'application déployée. Cette séparation évite de mélanger les environnements.

## Limites et évolutions

### Quelle est la principale limite actuelle ?

Le catalogue est local et ne se met pas à jour automatiquement. Les invitations, les favoris et les statistiques ne font pas non plus partie du MVP.

### Que feriez-vous ensuite ?

J'ajouterais des migrations Flyway, davantage de tests frontend, une vérification d'accessibilité, puis un système d'invitations avec acceptation ou refus.

### Quelle difficulté vous a le plus appris ?

Le contrôle des accès m'a appris que l'authentification ne suffit pas. J'ai centralisé les règles propriétaire et membre dans `WallAccessService` afin d'éviter des vérifications différentes dans chaque service.
