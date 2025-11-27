# Guide Claude - Covoiturage Vaadin

## Architecture du projet

**Architecture hexagonale** (Clean Architecture) avec Spring Boot + Vaadin + Spring Security

### Couches

1. **Domain** (`domain/model/`)
   - `Student` : id, name, email, **studentCode**, **username**, **password** (BCrypt), **role** (USER/ADMIN), enabled, createdAt
   - `Trip` : id, departureAddress, destinationAddress, departureTime, totalSeats, availableSeats, isRegular, driver (ManyToOne → Student)
   - Méthode métier : `Trip.bookSeat()`

2. **Application** (`application/`)
   - **Ports** : `IStudentRepositoryPort`, `ITripRepositoryPort` (interfaces)
   - **Services** :
     - `StudentService` : Gestion étudiants
     - `TripService` : Gestion trajets (auto-assign driver via SecurityContext)
     - `SecurityContextService` : Abstraction du SecurityContext
   - Services annotés avec `@Transactional(readOnly = true)` pour lectures, `@Transactional` pour écritures

3. **Infrastructure** (`infrastructure/`)
   - **Adapters** : `StudentRepositoryAdapter`, `TripRepositoryAdapter` (implémentent les ports)
   - **JPA Repositories** : `StudentJpaRepository`, `TripJpaRepository` (Spring Data)
   - **Security** :
     - `VaadinSecurityConfiguration` : Configuration Spring Security pour Vaadin
     - `UserDetailsServiceImpl` : Authentification via Student
     - BCrypt pour le hashing des mots de passe
   - **Config** :
     - `DataInitializer` : Compte admin par défaut + codes étudiants initiaux

4. **UI** (`ui/`)
   - **Layout** : `MainLayout` (AppLayout avec sidebar + header + logout)
   - **Components** : `LogoutButton` (✅ corrigé : capture UI avant logout)
   - **Views** :
     - `LoginView` (`/login`) : Authentification [@AnonymousAllowed]
     - `StudentView` (`/`) : Annuaire étudiants [@PermitAll]
       - Colonne "Actions" (suppression) visible **uniquement pour ROLE_ADMIN**
       - Protection : impossible de se supprimer soi-même
       - Filtrage : n'affiche pas les comptes ADMIN
     - `TripCreationView` (`/proposer-trajet`) : Formulaire création trajet [@PermitAll]
       - ⚠️ Pas de sélection conducteur : **auto-assigné** depuis SecurityContext
     - `TripSearchView` (`/rechercher-trajet`) : Recherche trajets par destination [@PermitAll]

## Entités JPA

### Relation importante
```java
// Trip.java
@ManyToOne(fetch = FetchType.EAGER)
private Student driver;
```
**EAGER nécessaire** pour éviter `LazyInitializationException` dans les vues Vaadin

### Modèle d'authentification
- **Student** est le principal de sécurité (pas d'entité User séparée)
- `username` et `email` sont uniques
- `role` : "ROLE_USER" ou "ROLE_ADMIN"
- `password` : BCrypt avec force 10 (défaut)

## Configuration

### Base de données (application.properties)
- **MySQL** : `jdbc:mysql://localhost:3306/covoiturage_db`
- DDL : `update` (préserve les données)
- Username : `root` / Password : (vide)
- ⚠️ Console H2 désactivée (migration vers MySQL)

### Session management
- `spring.session.store-type=jdbc` : Sessions persistées en base MySQL
- Tables créées automatiquement par spring-session

### Logging SQL
- `spring.jpa.show-sql=true` : affiche les requêtes SQL
- `logging.level.org.hibernate.SQL=DEBUG` : logs détaillés
- Utile en dev, à désactiver en prod

## Règles de code

1. **Ne jamais** injecter directement les JPA repositories dans les services
   - ✅ Services → Ports (interfaces)
   - ✅ Adapters → JPA Repositories

2. **Transactions**
   - Lectures : `@Transactional(readOnly = true)`
   - Écritures : `@Transactional`

3. **Vues Vaadin**
   - Injectent les Services (pas les repositories)
   - Routes : `@Route("chemin")` et `@PageTitle("Titre")`
   - Annotations sécurité : `@PermitAll`, `@AnonymousAllowed`, `@RolesAllowed("ADMIN")`

4. **Sécurité**
   - Toujours utiliser `SecurityContextService` pour accéder au contexte
   - Ne jamais manipuler directement `SecurityContextHolder` dans les services métier
   - `TripService.proposeTrip()` récupère automatiquement le conducteur connecté

## Authentification & Autorisation (✅ Phase 1 Implémentée)

### Ce qui est implémenté
- ✅ Login/Logout fonctionnel (LoginView)
- ✅ BCrypt pour les mots de passe
- ✅ Rôles USER/ADMIN (via Student.role)
- ✅ Protection des routes par rôles (@PermitAll, @RolesAllowed)
- ✅ MainLayout avec navigation drawer
- ✅ LogoutButton corrigé (capture UI avant invalidation session)
- ✅ Compte admin par défaut (DataInitializer)

### Compte admin par défaut
```
Username: admin
Password: admin123
Email: admin@dauphine.eu
Code: ADMIN001
```

## À implémenter (TODO) - Prochaines étapes

### Phase 2 : Système de whitelist (selon plan.md)
- [ ] Entité `AllowedStudentCode` (whitelist codes étudiants)
- [ ] Port `IAllowedStudentCodeRepositoryPort`
- [ ] Service `AllowedStudentCodeService`
- [ ] DataInitializer : ajouter codes pré-autorisés

### Phase 3 : Interface admin whitelist
- [ ] Vue `AdminWhitelistView` (@RolesAllowed("ADMIN"))
- [ ] CRUD des codes autorisés
- [ ] Grid avec colonnes : code, utilisé, créé par, date

### Phase 4 : Inscription étudiants
- [ ] Service `AuthenticationService.registerStudent()`
- [ ] Vue `RegisterView` (formulaire inscription)
- [ ] Validation code étudiant via whitelist
- [ ] Lien inscription sur LoginView
- [ ] Modifier `TripService.proposeTrip()` pour auto-assign driver

### Phase 5 : Système de réservation
- [ ] Entité `Booking` (id, trip, student, bookedAt, status)
- [ ] Port + Service `BookingService`
- [ ] Méthode `TripService.bookTrip(tripId)` utilisant `Trip.bookSeat()`
- [ ] Bouton "Réserver" dans `TripSearchView`
- [ ] Vue "Mes réservations"

### Améliorations futures
- Exploitation du flag `isRegular` (trajets réguliers vs ponctuels)
- Filtres avancés de recherche (date, horaire, nombre de places)
- Profil utilisateur éditable
- Notifications entre étudiants
- Validation côté client (Binder)
- Tests unitaires (services)
- Migration SSO (optionnel)

## Technologies

- Spring Boot 3.1.0
- Vaadin 24.2.0
- Spring Security 6.1.0
- Hibernate/JPA
- MySQL 8.0 (XAMPP/local)
- Spring Session JDBC
- Maven

## Bugs corrigés récents

### LogoutButton NullPointerException (✅ 27/11/2024)
- **Problème** : `UI.getCurrent()` retournait `null` après `SecurityContextLogoutHandler.logout()`
- **Solution** : Capturer la référence UI **avant** l'invalidation de session
- **Fichier** : `ui/component/LogoutButton.java:22`
