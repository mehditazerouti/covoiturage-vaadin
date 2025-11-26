# Guide Claude - Covoiturage Vaadin

## Architecture du projet

**Architecture hexagonale** (Clean Architecture) avec Spring Boot + Vaadin

### Couches

1. **Domain** (`domain/model/`)
   - `Student` : id, name, email
   - `Trip` : id, departure, destination, departureTime, totalSeats, availableSeats, isRegular, driver (ManyToOne → Student)
   - Méthode métier : `Trip.bookSeat()`

2. **Application** (`application/`)
   - **Ports** : `IStudentRepositoryPort`, `ITripRepositoryPort` (interfaces)
   - **Services** : `StudentService`, `TripService` (cas d'usage)
   - Services annotés avec `@Transactional(readOnly = true)` pour lectures, `@Transactional` pour écritures

3. **Infrastructure** (`infrastructure/adapter/`)
   - **Adapters** : `StudentRepositoryAdapter`, `TripRepositoryAdapter` (implémentent les ports)
   - **JPA Repositories** : `StudentJpaRepository`, `TripJpaRepository` (Spring Data)

4. **UI** (`ui/view/`)
   - `StudentView` (`/`) : CRUD étudiants
   - `TripCreationView` (`/proposer-trajet`) : Formulaire création trajet
   - `TripSearchView` (`/rechercher-trajet`) : Recherche trajets par destination

## Entités JPA

### Relation importante
```java
// Trip.java
@ManyToOne(fetch = FetchType.EAGER)
private Student driver;
```
**EAGER nécessaire** pour éviter `LazyInitializationException` dans les vues Vaadin

## Configuration

### Base de données (application.properties)
- H2 en mémoire : `jdbc:h2:mem:covoiturage`
- DDL : `create-drop` (recrée à chaque démarrage)
- Console H2 activée : `/h2-console`

### Logging SQL
- `spring.jpa.show-sql=true` : affiche les requêtes SQL
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

## À implémenter (TODO)

- Fonctionnalité réservation (bouton existe mais non implémenté)
- Authentification SSO
- Exploitation du flag `isRegular` (trajets réguliers vs ponctuels)

## Technologies

- Spring Boot 3.1.0
- Vaadin 24.2.0
- Hibernate/JPA
- H2 Database
- Maven
