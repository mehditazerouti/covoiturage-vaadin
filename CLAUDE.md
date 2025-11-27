# Guide Claude - Covoiturage Vaadin

## Architecture du projet

**Architecture hexagonale** (Clean Architecture) avec Spring Boot + Vaadin + Spring Security

### Couches

1. **Domain** (`domain/model/`)
   - `Student` : id, name, email, **studentCode**, **username**, **password** (BCrypt), **role** (USER/ADMIN), enabled, **approved**, createdAt
   - `Trip` : id, departureAddress, destinationAddress, departureTime, totalSeats, availableSeats, isRegular, driver (ManyToOne → Student)
   - `AllowedStudentCode` : id, studentCode (unique), used, createdAt, createdBy, usedBy (ManyToOne → Student)
   - Méthodes métier : `Trip.bookSeat()`, `AllowedStudentCode.markAsUsed(Student)`

2. **Application** (`application/`)
   - **Ports** : `IStudentRepositoryPort`, `ITripRepositoryPort`, `IAllowedStudentCodeRepositoryPort` (interfaces)
   - **Services** :
     - `StudentService` : Gestion étudiants
     - `TripService` : Gestion trajets (auto-assign driver via SecurityContext)
     - `SecurityContextService` : Abstraction du SecurityContext
     - `AllowedStudentCodeService` : Gestion de la whitelist des codes étudiants
     - `AuthenticationService` : Gestion de l'inscription et approbation des étudiants
   - Services annotés avec `@Transactional(readOnly = true)` pour lectures, `@Transactional` pour écritures

3. **Infrastructure** (`infrastructure/`)
   - **Adapters** : `StudentRepositoryAdapter`, `TripRepositoryAdapter`, `AllowedStudentCodeRepositoryAdapter` (implémentent les ports)
   - **JPA Repositories** : `StudentJpaRepository`, `TripJpaRepository`, `AllowedStudentCodeJpaRepository` (Spring Data)
   - **Security** :
     - `VaadinSecurityConfiguration` : Configuration Spring Security pour Vaadin
     - `UserDetailsServiceImpl` : Authentification via Student
     - BCrypt pour le hashing des mots de passe
   - **Config** :
     - `DataInitializer` : Compte admin par défaut + codes étudiants whitelistés (22405100, 22405101, 22405102)

4. **UI** (`ui/`)
   - **Layout** : `MainLayout` (AppLayout avec sidebar + header + logout)
     - Section navigation principale (tous utilisateurs)
     - Section administration (visible uniquement pour ROLE_ADMIN)
   - **Components** : `LogoutButton` (✅ corrigé : capture UI avant logout)
   - **Views publiques** :
     - `LoginView` (`/login`) : Authentification [@AnonymousAllowed]
       - Lien vers RegisterView
       - Traduction française du formulaire
     - `RegisterView` (`/register`) : Inscription publique [@AnonymousAllowed]
       - Si code whitelisté → compte activé immédiatement
       - Si code non whitelisté → compte en attente de validation admin
   - **Views utilisateur** [@PermitAll] :
     - `StudentView` (`/`) : Annuaire étudiants
       - Colonne "Actions" (suppression) visible **uniquement pour ROLE_ADMIN**
       - Protection : impossible de se supprimer soi-même
       - Filtrage : n'affiche pas les comptes ADMIN
     - `TripCreationView` (`/proposer-trajet`) : Formulaire création trajet
       - ⚠️ Pas de sélection conducteur : **auto-assigné** depuis SecurityContext
     - `TripSearchView` (`/rechercher-trajet`) : Recherche trajets par destination
   - **Views admin** [@RolesAllowed("ADMIN")] :
     - `AdminStudentCreationView` (`/admin/create-student`) : Création manuelle d'étudiant par admin
     - `AdminWhitelistView` (`/admin/whitelist`) : Gestion CRUD de la whitelist
       - Grid : code, utilisé, utilisé par, créé par, date, actions
       - Protection : impossible de supprimer un code utilisé
     - `PendingStudentsView` (`/admin/pending-students`) : Validation des étudiants en attente
       - Affiche les étudiants avec approved=false
       - Actions : Approuver (whitelist + activer) ou Rejeter (supprimer)

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
- `approved` : true = compte validé, false = en attente de validation admin
- `enabled` : true = peut se connecter, false = compte désactivé

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

## Authentification & Autorisation

### ✅ Phases 1 à 4 : Système complet d'inscription et whitelist (IMPLÉMENTÉ)

#### Phase 1 : Authentification de base
- ✅ Login/Logout fonctionnel (LoginView)
- ✅ BCrypt pour les mots de passe
- ✅ Rôles USER/ADMIN (via Student.role)
- ✅ Protection des routes par rôles (@PermitAll, @RolesAllowed)
- ✅ MainLayout avec navigation drawer
- ✅ LogoutButton corrigé (capture UI avant invalidation session)
- ✅ Compte admin par défaut (DataInitializer)

#### Phase 2 : Système de whitelist
- ✅ Entité `AllowedStudentCode` (whitelist codes étudiants)
- ✅ Port `IAllowedStudentCodeRepositoryPort` + Adapter JPA
- ✅ Service `AllowedStudentCodeService`
- ✅ DataInitializer : codes pré-autorisés (22405100, 22405101, 22405102)

#### Phase 3 : Interface admin whitelist
- ✅ Vue `AdminWhitelistView` (@RolesAllowed("ADMIN"))
- ✅ CRUD complet des codes autorisés
- ✅ Grid avec colonnes : code, utilisé, utilisé par, créé par, date
- ✅ Protection : impossible de supprimer un code utilisé

#### Phase 4 : Inscription étudiants
- ✅ Service `AuthenticationService.registerStudent()` et `approveStudent()`
- ✅ Vue `RegisterView` (formulaire inscription public)
- ✅ Validation code étudiant via whitelist :
  - Code whitelisté → approved=true, enabled=true (accès immédiat)
  - Code non whitelisté → approved=false, enabled=false (en attente)
- ✅ Lien inscription sur LoginView
- ✅ Vue `PendingStudentsView` pour valider/rejeter les étudiants en attente
- ✅ Champ `approved` ajouté à l'entité Student

### Compte admin par défaut
```
Username: admin
Password: admin123
Email: admin@dauphine.eu
Code: ADMIN001
```

### Codes étudiants whitelistés par défaut
```
22405100, 22405101, 22405102
```

## À implémenter (TODO) - Prochaines étapes

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

## Historique des développements

### Correction suppression étudiant avec code whitelist (✅ 27/11/2025 20:00)
- **Problème** : Erreur de contrainte de clé étrangère lors de la suppression d'un étudiant ayant utilisé un code whitelist
  - `SQLIntegrityConstraintViolationException`: Cannot delete or update a parent row
  - Le code restait marqué comme "utilisé" après suppression
- **Solution** :
  1. Ajout `@OnDelete(action = OnDeleteAction.SET_NULL)` sur `AllowedStudentCode.usedBy` (ligne 37)
  2. Modification `StudentService.deleteStudent()` pour libérer automatiquement le code (ligne 45-60)
  3. Ajout méthode `AllowedStudentCodeService.saveCode()` (ligne 95)
- **Migration SQL requise** :
  ```sql
  ALTER TABLE allowed_student_code DROP FOREIGN KEY FKb6y4t1fmdirvxv4ny3otlku8k;
  ALTER TABLE allowed_student_code ADD CONSTRAINT FKb6y4t1fmdirvxv4ny3otlku8k
  FOREIGN KEY (used_by_id) REFERENCES student(id) ON DELETE SET NULL;
  ```
- **Fichiers modifiés** :
  - `domain/model/AllowedStudentCode.java:37` : Annotation OnDelete
  - `application/services/StudentService.java:45-60` : Logique de libération du code
  - `application/services/AllowedStudentCodeService.java:95` : Méthode saveCode

### Système d'inscription et whitelist (✅ 27/11/2025 18:00)
- **Implémenté** : Phases 2, 3, et 4 complètes
- **Fichiers ajoutés** :
  - `domain/model/AllowedStudentCode.java` : Entité whitelist
  - `application/services/AuthenticationService.java` : Service inscription
  - `application/services/AllowedStudentCodeService.java` : Service whitelist
  - `ui/view/RegisterView.java` : Formulaire inscription public
  - `ui/view/AdminWhitelistView.java` : Gestion admin de la whitelist
  - `ui/view/PendingStudentsView.java` : Validation des étudiants en attente
  - Ports et adapters correspondants
- **Fichiers modifiés** :
  - `domain/model/Student.java` : Ajout champ `approved`
  - `ui/component/MainLayout.java` : Section admin avec 3 nouveaux liens
  - `ui/view/LoginView.java` : Lien vers RegisterView
  - `infrastructure/config/DataInitializer.java` : Ajout codes whitelistés

### LogoutButton NullPointerException (✅ 27/11/2025 16:00)
- **Problème** : `UI.getCurrent()` retournait `null` après `SecurityContextLogoutHandler.logout()`
- **Solution** : Capturer la référence UI **avant** l'invalidation de session
- **Fichier** : `ui/component/LogoutButton.java:22`
