package com.example.covoiturage_vaadin.infrastructure.config;

import com.example.covoiturage_vaadin.application.services.AllowedStudentCodeService;
import com.example.covoiturage_vaadin.application.services.StudentService;
import com.example.covoiturage_vaadin.domain.model.Booking;
import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import com.example.covoiturage_vaadin.domain.model.Student;
import com.example.covoiturage_vaadin.domain.model.Trip;
import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.IBookingRepositoryPort;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initialise les données de test au démarrage de l'application.
 *
 * Crée automatiquement :
 * - 1 compte administrateur
 * - 60 étudiants avec des noms français réalistes
 * - 60 codes étudiants whitelistés
 * - 100+ trajets variés (destinations, dates, places, réguliers/ponctuels)
 * - Des réservations aléatoires
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final StudentService studentService;
    private final AllowedStudentCodeService codeService;
    private final PasswordEncoder passwordEncoder;
    private final ITripRepositoryPort tripRepository;
    private final IBookingRepositoryPort bookingRepository;
    private final Random random = new Random();

    // Listes de noms français pour générer des étudiants réalistes
    private static final String[] PRENOMS = {
        "Mehdi", "Salim", "Thomas", "Marie", "Lucas", "Emma", "Hugo", "Léa",
        "Nathan", "Chloé", "Louis", "Sarah", "Arthur", "Camille", "Gabriel",
        "Manon", "Jules", "Inès", "Raphaël", "Jade", "Adam", "Louise", "Tom",
        "Zoé", "Théo", "Lina", "Paul", "Clara", "Antoine", "Lisa", "Maxime",
        "Alice", "Alexandre", "Anaïs", "Victor", "Julie", "Pierre", "Laura",
        "Benjamin", "Mathilde", "Nicolas", "Charlotte", "Valentin", "Océane",
        "Clément", "Eva", "Romain", "Margaux", "Julien", "Sophie", "Quentin",
        "Pauline", "Alexis", "Justine", "Dylan", "Elise", "Kevin", "Marine",
        "Florian", "Audrey", "Adrien", "Morgane"
    };

    private static final String[] NOMS = {
        "Tazerouti", "Bouskine", "Martin", "Bernard", "Dubois", "Thomas", "Robert",
        "Richard", "Petit", "Durand", "Leroy", "Moreau", "Simon", "Laurent",
        "Lefebvre", "Michel", "Garcia", "David", "Bertrand", "Roux", "Vincent",
        "Fournier", "Morel", "Girard", "André", "Lefevre", "Mercier", "Dupont",
        "Lambert", "Bonnet", "François", "Martinez", "Legrand", "Garnier",
        "Faure", "Rousseau", "Blanc", "Guerin", "Muller", "Henry", "Roussel",
        "Nicolas", "Perrin", "Morin", "Mathieu", "Clement", "Gauthier", "Dumont",
        "Lopez", "Fontaine", "Chevalier", "Robin", "Masson", "Sanchez", "Gerard",
        "Nguyen", "Boyer", "Denis", "Lemaire", "Duval"
    };

    private static final String[] VILLES = {
        "Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Strasbourg",
        "Montpellier", "Bordeaux", "Lille", "Rennes", "Reims", "Le Havre",
        "Saint-Étienne", "Toulon", "Grenoble", "Dijon", "Angers", "Villeurbanne",
        "Le Mans", "Aix-en-Provence", "Brest", "Nîmes", "Limoges", "Tours",
        "Amiens", "Perpignan", "Metz", "Besançon", "Orléans", "Rouen", "Caen",
        "Argenteuil", "Mulhouse", "Nancy", "Montreuil", "La Défense"
    };

    public DataInitializer(StudentService studentService,
                          AllowedStudentCodeService codeService,
                          PasswordEncoder passwordEncoder,
                          ITripRepositoryPort tripRepository,
                          IBookingRepositoryPort bookingRepository) {
        this.studentService = studentService;
        this.codeService = codeService;
        this.passwordEncoder = passwordEncoder;
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // ⚠️ Vérification : Si des données existent déjà, on skip l'initialisation
        long studentCount = studentService.getAllStudents().stream()
            .filter(s -> !"ROLE_ADMIN".equals(s.getRole()))
            .count();

        if (studentCount > 0) {
            System.out.println("ℹ️  Base de données déjà peuplée (" + studentCount + " étudiants trouvés)");
            System.out.println("ℹ️  Initialisation des données de test IGNORÉE");

            // Créer uniquement le compte admin s'il n'existe pas
            if (!studentService.existsByUsername("admin")) {
                createAdmin();
            }

            return; // ⚠️ On arrête ici
        }

        System.out.println("🚀 Initialisation des données de test...");

        // 1. Créer le compte admin
        Student admin = createAdmin();
        System.out.println("✅ L'administrateur a été créé." + admin.getUsername());

        // 2. Créer 60 codes étudiants whitelistés
        createWhitelistedCodes();

        // 3. Créer 60 étudiants avec des noms français réalistes
        List<Student> students = createStudents(60);
        System.out.println("✅ " + students.size() + " étudiants créés");

        // 4. Créer 100+ trajets variés
        List<Trip> trips = createTrips(students, 120);
        System.out.println("✅ " + trips.size() + " trajets créés");

        // 5. Créer des réservations aléatoires
        int bookingsCount = createBookings(students, trips, 80);
        System.out.println("✅ " + bookingsCount + " réservations créées");

        System.out.println("✅ Initialisation terminée !");
        System.out.println("📊 Résumé : 1 admin + " + students.size() + " étudiants, "
                          + trips.size() + " trajets, " + bookingsCount + " réservations");
        System.out.println("🔑 Connexion admin : admin / admin123");
    }

    /**
     * Crée le compte administrateur par défaut.
     */
    private Student createAdmin() {
        if (!studentService.existsByUsername("admin")) {
            Student admin = new Student();
            admin.setName("Administrateur");
            admin.setEmail("admin@dauphine.eu");
            admin.setStudentCode("ADMIN001");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setApproved(true);
            admin.setAvatar("USER");
            admin.setCreatedAt(LocalDateTime.now());

            studentService.saveStudent(admin);
            System.out.println("✅ Compte admin créé : admin / admin123");
            return admin;
        }
        return studentService.getStudentByUsername("admin").orElse(null);
    }

    /**
     * Crée 60 codes étudiants whitelistés (22405100 à 22405159).
     */
    private void createWhitelistedCodes() {
        if (codeService.findAll().isEmpty()) {
            for (int i = 100; i < 160; i++) {
                String code = "224051" + String.format("%02d", i);
                try {
                    codeService.addAllowedCode(code, "SYSTEM");
                } catch (IllegalArgumentException e) {
                    // Code déjà existant, on ignore
                }
            }
            System.out.println("✅ 60 codes étudiants whitelistés (22405100-22405159)");
        }
    }

    /**
     * Crée N étudiants avec des noms français réalistes.
     */
    private List<Student> createStudents(int count) {
        List<Student> students = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String prenom = PRENOMS[i % PRENOMS.length];
            String nom = NOMS[i % NOMS.length];
            String fullName = prenom + " " + nom;
            String code = "224051" + String.format("%02d", i + 100);
            String email = prenom.toLowerCase() + "." + nom.toLowerCase() + "@dauphine.eu";
            String username = code;

            // Éviter les doublons d'email
            if (studentService.existsByEmail(email)) {
                email = prenom.toLowerCase() + "." + nom.toLowerCase() + i + "@dauphine.eu";
            }

            Student student = new Student();
            student.setName(fullName);
            student.setEmail(email);
            student.setStudentCode(code);
            student.setUsername(username);
            student.setPassword(passwordEncoder.encode("password123")); // Mot de passe de test
            student.setRole("ROLE_USER");
            student.setEnabled(true);
            student.setApproved(true);
            student.setAvatar(getRandomAvatar());
            student.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(180))); // Créés entre 0 et 6 mois

            Student saved = studentService.saveStudent(student);
            students.add(saved);

            // Marquer le code comme utilisé
            codeService.markCodeAsUsed(code, saved);
        }

        return students;
    }

    /**
     * Crée N trajets variés pour les étudiants.
     */
    private List<Trip> createTrips(List<Student> students, int count) {
        List<Trip> trips = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Student driver = students.get(random.nextInt(students.size()));
            String departure = VILLES[random.nextInt(VILLES.length)];
            String destination = VILLES[random.nextInt(VILLES.length)];

            // Éviter départ = destination
            while (departure.equals(destination)) {
                destination = VILLES[random.nextInt(VILLES.length)];
            }

            // Date entre maintenant et +30 jours
            LocalDateTime departureTime = LocalDateTime.now()
                .plusDays(random.nextInt(30))
                .plusHours(random.nextInt(24))
                .withMinute(random.nextInt(4) * 15) // Minutes : 0, 15, 30, 45
                .withSecond(0)
                .withNano(0);

            int totalSeats = 2 + random.nextInt(4); // 2 à 5 places
            boolean isRegular = random.nextDouble() < 0.3; // 30% de trajets réguliers

            Trip trip = new Trip();
            trip.setDepartureAddress(departure);
            trip.setDestinationAddress(destination);
            trip.setDepartureTime(departureTime);
            trip.setTotalSeats(totalSeats);
            trip.setAvailableSeats(totalSeats);
            trip.setDriver(driver);
            trip.setRegular(isRegular);

            Trip saved = tripRepository.save(trip);
            trips.add(saved);
        }

        return trips;
    }

    /**
     * Crée des réservations aléatoires.
     */
    private int createBookings(List<Student> students, List<Trip> trips, int count) {
        int created = 0;

        for (int i = 0; i < count; i++) {
            Trip trip = trips.get(random.nextInt(trips.size()));
            Student student = students.get(random.nextInt(students.size()));

            // Ne pas réserver son propre trajet
            if (trip.getDriver().getId().equals(student.getId())) {
                continue;
            }

            // Vérifier qu'il reste des places
            if (trip.getAvailableSeats() <= 0) {
                continue;
            }

            // Vérifier qu'il n'a pas déjà réservé ce trajet
            boolean alreadyBooked = bookingRepository.findAll().stream()
                .anyMatch(b -> b.getTrip().getId().equals(trip.getId())
                            && b.getStudent().getId().equals(student.getId())
                            && b.isActive());

            if (alreadyBooked) {
                continue;
            }

            // Créer la réservation
            Booking booking = new Booking(trip, student);
            booking.setBookedAt(LocalDateTime.now().minusDays(random.nextInt(10)));

            // 80% confirmées, 15% en attente, 5% annulées
            double rand = random.nextDouble();
            if (rand < 0.80) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else if (rand < 0.95) {
                booking.setStatus(BookingStatus.PENDING);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
            }

            // Décrémenter les places seulement si réservation active
            if (booking.isActive()) {
                trip.setAvailableSeats(trip.getAvailableSeats() - 1);
                tripRepository.save(trip);
            }

            bookingRepository.save(booking);
            created++;
        }

        return created;
    }

    /**
     * Retourne un avatar aléatoire parmi les 3 disponibles.
     */
    private String getRandomAvatar() {
        String[] avatars = {"USER", "MALE", "FEMALE"};
        return avatars[random.nextInt(avatars.length)];
    }
}
