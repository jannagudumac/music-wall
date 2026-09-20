# Sécurité — notes de soutenance

## Présentation courte

La sécurité de Music Wall repose sur Spring Security, BCrypt et les JWT. L'authentification vérifie l'identité de l'utilisateur. L'autorisation vérifie ensuite les actions qu'il a le droit d'effectuer.

## Inscription

Lors de l'inscription :

1. le backend vérifie que le nom d'utilisateur n'existe pas déjà ;
2. le mot de passe est haché avec BCrypt ;
3. le rôle `USER` est attribué par le serveur ;
4. l'utilisateur est enregistré ;
5. le backend retourne un JWT, le nom d'utilisateur et le rôle.

Le client ne peut donc pas choisir lui-même son rôle. Le mot de passe en clair n'est jamais enregistré dans PostgreSQL et n'est jamais renvoyé par l'API.

## Connexion et création du JWT

`AuthenticationManager` vérifie le nom d'utilisateur et le mot de passe. Après une connexion réussie, `JwtUtil` crée un token signé contenant :

- le nom d'utilisateur ;
- la date de création ;
- la date d'expiration.

La durée configurée est de 24 heures. La clé de signature vient de la variable d'environnement `JWT_SECRET` et n'est pas enregistrée dans le dépôt Git.

## Validation d'une requête protégée

1. Angular récupère le JWT dans `localStorage`.
2. `AuthInterceptor` ajoute `Authorization: Bearer <token>` à la requête.
3. `JwtFilter` lit cet en-tête avant le controller.
4. Il vérifie la signature, la date d'expiration et le nom de l'utilisateur.
5. Si le token est valide, Spring enregistre l'utilisateur dans le `SecurityContext`.
6. Le controller peut ensuite obtenir son identité avec `Authentication.getName()`.

L'API est stateless : le backend ne conserve pas de session de connexion en mémoire. Chaque requête protégée doit fournir son JWT.

## Routes publiques et protégées

Les routes d'inscription et de connexion sont publiques. Les lectures du catalogue et des profils sont également autorisées par le backend. Les autres routes nécessitent une authentification.

Le guard Angular empêche aussi l'accès aux pages principales lorsqu'aucun token n'est présent. Ce guard améliore la navigation, mais la véritable sécurité reste assurée par le backend.

## Autorisation sur les murs

Être connecté ne donne pas accès à tous les murs. `WallAccessService` centralise deux contrôles :

- `findAccessibleWall` autorise le propriétaire et les membres ;
- `findOwnedWall` autorise uniquement le propriétaire.

Le propriétaire peut modifier l'apparence, gérer les membres et supprimer le mur. Un membre peut consulter le mur et gérer ses sections et contenus. Un utilisateur extérieur reçoit une erreur HTTP 403.

Cette vérification est faite dans le service avec le nom fourni par Spring Security. Le backend ne fait pas confiance à un nom d'utilisateur envoyé par le frontend.

## Codes HTTP importants

- `400 Bad Request` : données invalides ou règle métier refusée ;
- `401 Unauthorized` : utilisateur non authentifié ou JWT rejeté ;
- `403 Forbidden` : utilisateur authentifié mais sans permission ;
- `404 Not Found` : ressource inexistante.

L'intercepteur Angular déconnecte l'utilisateur après une réponse 401. Il conserve la session après une réponse 403, car le token est toujours valide.

## Autres protections

### Validation

Les DTO utilisent les annotations Jakarta Validation comme `@NotBlank`, `@Size` et `@Pattern`. Le frontend valide aussi les formulaires, mais la validation backend reste obligatoire car une requête peut être envoyée sans passer par l'interface Angular.

### Injection SQL

L'accès aux données passe par Spring Data JPA et par des requêtes paramétrées. Les entrées utilisateur ne sont pas concaténées directement dans une chaîne SQL.

### XSS

Angular échappe par défaut les valeurs affichées dans les templates. Le projet n'insère pas directement du HTML fourni par un utilisateur.

### CORS

Le backend accepte uniquement l'origine définie par `CORS_ORIGIN`. En production, cette valeur correspond au frontend Netlify.

### CSRF

La protection CSRF est désactivée car l'API est stateless et reçoit le JWT dans l'en-tête `Authorization`, pas dans un cookie de session automatiquement envoyé par le navigateur.

### Secrets

Les identifiants PostgreSQL, la clé JWT et l'origine CORS sont fournis par des variables d'environnement. Le dépôt contient seulement des exemples sans secrets réels.

## Limite à expliquer honnêtement

Le JWT est enregistré dans `localStorage`. C'est simple pour ce projet, mais un script malveillant exécuté dans la page pourrait tenter de le lire. Angular réduit le risque XSS en échappant les valeurs des templates. Pour une application plus sensible, j'étudierais un cookie `HttpOnly`, `Secure` et `SameSite`, avec une stratégie CSRF adaptée.
