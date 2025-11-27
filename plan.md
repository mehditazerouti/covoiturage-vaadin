Plan d'implémentation : Système d'authentification Spring Security

 Vue d'ensemble

 Implémenter un système d'authentification complet pour l'application de covoiturage avec :
 - Login/password via Spring Security
 - Whitelist de codes étudiants (liste pré-définie)
 - Auto-inscription pour codes autorisés
 - Interface admin pour gérer la whitelist
 - Navigation principale avec menu drawer
 - Protection des routes par rôles (USER, ADMIN)

 Approche : Implémentation progressive en 4 phases testables indépendamment

 Architecture retenue

 Modèle de domaine

 - Extension de Student : Ajout de champs d'authentification (username, password, studentCode, role, enabled)
 - Nouvelle entité AllowedStudentCode : Whitelist des codes autorisés
 - Pas d'entité User séparée : Student est le principal de sécurité

 Couches impactées (architecture hexagonale)

 Domain        → Student (étendu), AllowedStudentCode (nouveau)
 Application   → Nouveaux services (Auth, SecurityContext, AllowedCode)
 Infrastructure → Security config, UserDetailsService, nouveaux adapters
 UI            → LoginView, RegisterView, AdminView, MainLayout

 Phase 1 : Authentification basique + Login

 Objectif

 Login/logout fonctionnel avec compte admin par défaut

 1.1 Domain Model

 Modifier Student.java
 Ajouter les champs :
 @Column(unique = true, nullable = false)
 private String studentCode;

 @Column(unique = true, nullable = false)
 private String username;

 @Column(nullable = false)
 private String password;  // BCrypt hashed

 private String role = "ROLE_USER";  // ROLE_USER ou ROLE_ADMIN

 private boolean enabled = true;

 private LocalDateTime createdAt;

 Créer StudentRole.java (optionnel - enum)
 public enum StudentRole {
     ROLE_USER,
     ROLE_ADMIN
 }

 1.2 Application Layer

 Étendre IStudentRepositoryPort.java
 Optional<Student> findByUsername(String username);
 Optional<Student> findByStudentCode(String studentCode);
 boolean existsByUsername(String username);
 boolean existsByEmail(String email);

 Créer SecurityContextService.java
 Service pour abstraire SecurityContextHolder :
 @Service
 public class SecurityContextService {
     public Optional<String> getCurrentUsername() {
         // Récupère username depuis SecurityContext
     }

     public boolean hasRole(String role) {
         // Vérifie si user a le rôle
     }
 }

 Modifier StudentService.java
 Ajouter méthodes :
 public Optional<Student> getStudentByUsername(String username);
 public Optional<Student> getStudentByStudentCode(String code);

 1.3 Infrastructure Layer

 Étendre StudentJpaRepository.java
 Optional<Student> findByUsername(String username);
 Optional<Student> findByStudentCode(String studentCode);
 boolean existsByUsername(String username);
 boolean existsByEmail(String email);

 Modifier StudentRepositoryAdapter.java
 Implémenter les nouvelles méthodes du port

 Créer infrastructure/security/UserDetailsServiceImpl.java
 @Service
 public class UserDetailsServiceImpl implements UserDetailsService {
     private final StudentService studentService;

     @Override
     public UserDetails loadUserByUsername(String username) {
         Student student = studentService.getStudentByUsername(username)
             .orElseThrow(() -> new UsernameNotFoundException("User not found"));

         return User.builder()
             .username(student.getUsername())
             .password(student.getPassword())
             .roles(student.getRole().replace("ROLE_", ""))
             .disabled(!student.isEnabled())
             .build();
     }
 }

 Créer infrastructure/config/SecurityConfiguration.java
 @Configuration
 @EnableWebSecurity
 public class SecurityConfiguration {

     @Bean
     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http
             .authorizeHttpRequests(auth -> auth
                 .requestMatchers("/login", "/register").permitAll()
                 .requestMatchers("/admin/**").hasRole("ADMIN")
                 .anyRequest().authenticated()
             )
             .formLogin(form -> form
                 .loginPage("/login")
                 .defaultSuccessUrl("/", true)
             )
             .logout(logout -> logout
                 .logoutSuccessUrl("/login")
             );

         return http.build();
     }

     @Bean
     public PasswordEncoder passwordEncoder() {
         return new BCryptPasswordEncoder();
     }
 }

 Créer infrastructure/config/VaadinSecurityConfiguration.java
 @Configuration
 public class VaadinSecurityConfiguration extends VaadinWebSecurity {

     @Override
     protected void configure(HttpSecurity http) throws Exception {
         super.configure(http);
         setLoginView(http, LoginView.class);
     }
 }

 Créer infrastructure/config/DataInitializer.java
 @Component
 public class DataInitializer implements ApplicationRunner {

     private final StudentService studentService;
     private final PasswordEncoder passwordEncoder;

     @Override
     @Transactional
     public void run(ApplicationArguments args) {
         // Créer admin par défaut si n'existe pas
         if (!studentService.existsByUsername("admin")) {
             Student admin = new Student();
             admin.setName("Administrateur");
             admin.setEmail("admin@ensah.ma");
             admin.setStudentCode("ADMIN001");
             admin.setUsername("admin");
             admin.setPassword(passwordEncoder.encode("admin123"));
             admin.setRole("ROLE_ADMIN");
             admin.setEnabled(true);
             admin.setCreatedAt(LocalDateTime.now());
             studentService.saveStudent(admin);
         }
     }
 }

 1.4 UI Layer

 Créer ui/view/LoginView.java
 @Route("login")
 @PageTitle("Connexion - Covoiturage")
 @AnonymousAllowed
 public class LoginView extends VerticalLayout implements BeforeEnterObserver {

     private final LoginForm loginForm = new LoginForm();

     public LoginView() {
         setSizeFull();
         setAlignItems(Alignment.CENTER);
         setJustifyContentMode(JustifyContentMode.CENTER);

         loginForm.setAction("login");
         loginForm.setForgotPasswordButtonVisible(false);

         H1 title = new H1("Covoiturage ENSAH");
         Paragraph subtitle = new Paragraph("Connectez-vous pour continuer");

         add(title, subtitle, loginForm);
     }

     @Override
     public void beforeEnter(BeforeEnterEvent event) {
         if (event.getLocation()
             .getQueryParameters()
             .getParameters()
             .containsKey("error")) {
             loginForm.setError(true);
         }
     }
 }

 1.5 Configuration

 Modifier pom.xml
 Ajouter dépendances :
 <!-- Spring Security -->
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-security</artifactId>
 </dependency>

 <!-- Spring Session JDBC -->
 <dependency>
     <groupId>org.springframework.session</groupId>
     <artifactId>spring-session-jdbc</artifactId>
 </dependency>

 Modifier application.properties
 # Session management
 spring.session.store-type=jdbc
 spring.session.jdbc.initialize-schema=always

 1.6 Tests Phase 1

 - Lancer l'app → redirection vers /login
 - Login avec admin/admin123 → accès à /
 - Logout → retour à /login
 - Vérifier hash du password en base

 ---
 Phase 2 : Système de whitelist

 Objectif

 Créer l'entité AllowedStudentCode et le service de gestion

 2.1 Domain Model

 Créer domain/model/AllowedStudentCode.java
 @Entity
 public class AllowedStudentCode {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(unique = true, nullable = false)
     private String studentCode;

     private boolean used = false;

     private LocalDateTime createdAt;

     private String createdBy;  // Username de l'admin

     // getters/setters, constructeurs
 }

 2.2 Application Layer

 Créer application/ports/IAllowedStudentCodeRepositoryPort.java
 public interface IAllowedStudentCodeRepositoryPort {
     AllowedStudentCode save(AllowedStudentCode code);
     Optional<AllowedStudentCode> findByStudentCode(String studentCode);
     List<AllowedStudentCode> findAll();
     void delete(AllowedStudentCode code);
     boolean existsByStudentCode(String studentCode);
 }

 Créer application/services/AllowedStudentCodeService.java
 @Service
 @Transactional(readOnly = true)
 public class AllowedStudentCodeService {

     private final IAllowedStudentCodeRepositoryPort repository;

     @Transactional
     public AllowedStudentCode addAllowedCode(String studentCode, String createdBy) {
         if (repository.existsByStudentCode(studentCode)) {
             throw new IllegalArgumentException("Code déjà existant");
         }

         AllowedStudentCode code = new AllowedStudentCode();
         code.setStudentCode(studentCode);
         code.setCreatedBy(createdBy);
         code.setCreatedAt(LocalDateTime.now());
         return repository.save(code);
     }

     public boolean isCodeAvailable(String studentCode) {
         return repository.findByStudentCode(studentCode)
             .map(code -> !code.isUsed())
             .orElse(false);
     }

     @Transactional
     public void markCodeAsUsed(String studentCode) {
         AllowedStudentCode code = repository.findByStudentCode(studentCode)
             .orElseThrow(() -> new IllegalArgumentException("Code non trouvé"));
         code.setUsed(true);
         repository.save(code);
     }

     public List<AllowedStudentCode> findAll() {
         return repository.findAll();
     }

     @Transactional
     public void deleteCode(AllowedStudentCode code) {
         repository.delete(code);
     }
 }

 2.3 Infrastructure Layer

 Créer infrastructure/adapter/AllowedStudentCodeJpaRepository.java
 public interface AllowedStudentCodeJpaRepository
     extends JpaRepository<AllowedStudentCode, Long> {
     Optional<AllowedStudentCode> findByStudentCode(String studentCode);
     boolean existsByStudentCode(String studentCode);
 }

 Créer infrastructure/adapter/AllowedStudentCodeRepositoryAdapter.java
 @Component
 public class AllowedStudentCodeRepositoryAdapter
     implements IAllowedStudentCodeRepositoryPort {

     private final AllowedStudentCodeJpaRepository jpaRepository;

     // Implémentation de tous les méthodes du port
 }

 Modifier DataInitializer.java
 Ajouter initialisation de codes par défaut :
 @Autowired
 private AllowedStudentCodeService codeService;

 @Override
 public void run(ApplicationArguments args) {
     // ... création admin ...

     // Ajouter codes par défaut
     if (codeService.findAll().isEmpty()) {
         codeService.addAllowedCode("20240001", "SYSTEM");
         codeService.addAllowedCode("20240002", "SYSTEM");
         codeService.addAllowedCode("20240003", "SYSTEM");
     }
 }

 2.4 Tests Phase 2

 - Vérifier table allowed_student_code créée
 - Vérifier 3 codes initialisés
 - Tester isCodeAvailable() via console ou test unitaire

 ---
 Phase 3 : Interface admin de gestion whitelist

 Objectif

 Vue Vaadin pour admins gérant les codes autorisés

 3.1 UI Layer

 Créer ui/view/AdminWhitelistView.java
 @Route(value = "admin/whitelist", layout = MainLayout.class)
 @PageTitle("Gestion Whitelist - Admin")
 @RolesAllowed("ADMIN")
 public class AdminWhitelistView extends VerticalLayout {

     private final AllowedStudentCodeService codeService;
     private final SecurityContextService securityContext;

     private final Grid<AllowedStudentCode> grid = new Grid<>(AllowedStudentCode.class);
     private final TextField newCodeField = new TextField("Nouveau code étudiant");
     private final Button addButton = new Button("Ajouter", VaadinIcon.PLUS.create());

     public AdminWhitelistView(AllowedStudentCodeService codeService,
                              SecurityContextService securityContext) {
         this.codeService = codeService;
         this.securityContext = securityContext;

         setSpacing(true);
         setPadding(true);

         H2 title = new H2("Gestion des codes étudiants autorisés");

         configureGrid();

         addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
         addButton.addClickListener(e -> handleAddCode());

         HorizontalLayout toolbar = new HorizontalLayout(newCodeField, addButton);
         toolbar.setAlignItems(Alignment.END);

         add(title, toolbar, grid);

         refreshGrid();
     }

     private void configureGrid() {
         grid.removeAllColumns();
         grid.addColumn(AllowedStudentCode::getStudentCode)
             .setHeader("Code étudiant")
             .setSortable(true);

         grid.addColumn(code -> code.isUsed() ? "Oui" : "Non")
             .setHeader("Utilisé");

         grid.addColumn(AllowedStudentCode::getCreatedBy)
             .setHeader("Ajouté par");

         grid.addColumn(code -> {
             if (code.getCreatedAt() != null) {
                 return code.getCreatedAt().format(
                     DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                 );
             }
             return "";
         }).setHeader("Date création");

         grid.addComponentColumn(code -> {
             Button deleteBtn = new Button(VaadinIcon.TRASH.create());
             deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
             deleteBtn.addClickListener(e -> handleDelete(code));
             deleteBtn.setEnabled(!code.isUsed());  // Désactivé si déjà utilisé
             return deleteBtn;
         }).setHeader("Actions");

         grid.getColumns().forEach(col -> col.setAutoWidth(true));
     }

     private void handleAddCode() {
         String code = newCodeField.getValue();

         if (code == null || code.trim().isEmpty()) {
             Notification.show("Veuillez entrer un code", 3000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_ERROR);
             return;
         }

         try {
             String username = securityContext.getCurrentUsername().orElse("UNKNOWN");
             codeService.addAllowedCode(code.trim(), username);

             Notification.show("Code ajouté avec succès", 3000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

             newCodeField.clear();
             refreshGrid();
         } catch (IllegalArgumentException ex) {
             Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                 .addThemeVariants(NotificationVariant.LUMO_ERROR);
         }
     }

     private void handleDelete(AllowedStudentCode code) {
         codeService.deleteCode(code);
         Notification.show("Code supprimé", 3000, Notification.Position.MIDDLE);
         refreshGrid();
     }

     private void refreshGrid() {
         grid.setItems(codeService.findAll());
     }
 }

 3.2 Tests Phase 3

 - Login en tant qu'admin
 - Accéder à /admin/whitelist
 - Ajouter un code → vérifier dans la grille
 - Vérifier qu'un code utilisé ne peut pas être supprimé
 - Essayer d'accéder avec compte USER → 403 Forbidden

 ---
 Phase 4 : Inscription + Navigation + Mise à jour vues

 Objectif

 Inscription étudiants, layout principal, protection routes existantes

 4.1 Application Layer

 Créer application/services/AuthenticationService.java
 @Service
 @Transactional(readOnly = true)
 public class AuthenticationService {

     private final StudentService studentService;
     private final AllowedStudentCodeService codeService;
     private final PasswordEncoder passwordEncoder;

     @Transactional
     public Student registerStudent(String studentCode, String username,
                                    String email, String name, String password) {
         // Validation
         if (studentService.existsByUsername(username)) {
             throw new IllegalArgumentException("Nom d'utilisateur déjà pris");
         }

         if (studentService.existsByEmail(email)) {
             throw new IllegalArgumentException("Email déjà utilisé");
         }

         if (!codeService.isCodeAvailable(studentCode)) {
             throw new IllegalArgumentException(
                 "Code étudiant invalide ou déjà utilisé"
             );
         }

         // Créer l'étudiant
         Student student = new Student();
         student.setStudentCode(studentCode);
         student.setUsername(username);
         student.setEmail(email);
         student.setName(name);
         student.setPassword(passwordEncoder.encode(password));
         student.setRole("ROLE_USER");
         student.setEnabled(true);
         student.setCreatedAt(LocalDateTime.now());

         Student saved = studentService.saveStudent(student);

         // Marquer le code comme utilisé
         codeService.markCodeAsUsed(studentCode);

         return saved;
     }

     public Student getCurrentLoggedInStudent() {
         return SecurityContextHolder.getContext().getAuthentication()
             .map(auth -> studentService.getStudentByUsername(auth.getName()))
             .flatMap(opt -> opt)
             .orElse(null);
     }
 }

 4.2 UI Layer - Inscription

 Créer ui/view/RegisterView.java
 @Route("register")
 @PageTitle("Inscription - Covoiturage")
 @AnonymousAllowed
 public class RegisterView extends VerticalLayout {

     private final AuthenticationService authService;

     private final TextField studentCodeField = new TextField("Code étudiant");
     private final TextField usernameField = new TextField("Nom d'utilisateur");
     private final TextField nameField = new TextField("Nom complet");
     private final EmailField emailField = new EmailField("Email");
     private final PasswordField passwordField = new PasswordField("Mot de passe");
     private final PasswordField confirmPasswordField =
         new PasswordField("Confirmer le mot de passe");
     private final Button registerButton = new Button("S'inscrire");

     public RegisterView(AuthenticationService authService) {
         this.authService = authService;

         setSizeFull();
         setAlignItems(Alignment.CENTER);
         setJustifyContentMode(JustifyContentMode.CENTER);

         H1 title = new H1("Créer un compte");
         Paragraph info = new Paragraph(
             "Utilisez votre code étudiant pour vous inscrire"
         );

         FormLayout formLayout = new FormLayout();
         formLayout.add(
             studentCodeField,
             usernameField,
             nameField,
             emailField,
             passwordField,
             confirmPasswordField
         );
         formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

         studentCodeField.setRequired(true);
         studentCodeField.setPlaceholder("Ex: 20240001");

         usernameField.setRequired(true);
         usernameField.setPlaceholder("Nom d'utilisateur unique");

         emailField.setRequired(true);
         emailField.setPlaceholder("votreemail@ensah.ma");

         passwordField.setRequired(true);
         passwordField.setMinLength(6);

         registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
         registerButton.addClickListener(e -> handleRegistration());

         RouterLink loginLink = new RouterLink(
             "Déjà un compte ? Se connecter",
             LoginView.class
         );

         VerticalLayout form = new VerticalLayout(
             title,
             info,
             formLayout,
             registerButton,
             loginLink
         );
         form.setMaxWidth("500px");
         form.setAlignItems(Alignment.STRETCH);

         add(form);
     }

     private void handleRegistration() {
         // Validation
         if (!validateFields()) {
             return;
         }

         try {
             authService.registerStudent(
                 studentCodeField.getValue().trim(),
                 usernameField.getValue().trim(),
                 emailField.getValue().trim(),
                 nameField.getValue().trim(),
                 passwordField.getValue()
             );

             Notification.show(
                 "Inscription réussie ! Vous pouvez maintenant vous connecter.",
                 5000,
                 Notification.Position.MIDDLE
             ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

             // Redirection vers login
             getUI().ifPresent(ui -> ui.navigate(LoginView.class));

         } catch (IllegalArgumentException ex) {
             Notification.show(
                 "Erreur : " + ex.getMessage(),
                 5000,
                 Notification.Position.MIDDLE
             ).addThemeVariants(NotificationVariant.LUMO_ERROR);
         }
     }

     private boolean validateFields() {
         if (studentCodeField.isEmpty() || usernameField.isEmpty() ||
             nameField.isEmpty() || emailField.isEmpty() ||
             passwordField.isEmpty() || confirmPasswordField.isEmpty()) {

             Notification.show(
                 "Tous les champs sont obligatoires",
                 3000,
                 Notification.Position.MIDDLE
             ).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return false;
         }

         if (passwordField.getValue().length() < 6) {
             Notification.show(
                 "Le mot de passe doit contenir au moins 6 caractères",
                 3000,
                 Notification.Position.MIDDLE
             ).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return false;
         }

         if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
             Notification.show(
                 "Les mots de passe ne correspondent pas",
                 3000,
                 Notification.Position.MIDDLE
             ).addThemeVariants(NotificationVariant.LUMO_ERROR);
             return false;
         }

         return true;
     }
 }

 Modifier LoginView.java
 Ajouter lien vers inscription :
 RouterLink registerLink = new RouterLink(
     "Pas encore de compte ? S'inscrire",
     RegisterView.class
 );
 add(title, subtitle, loginForm, registerLink);

 4.3 UI Layer - Navigation principale

 Créer ui/component/MainLayout.java
 @CssImport("./styles/main-layout.css")
 public class MainLayout extends AppLayout {

     private final SecurityContextService securityContext;

     public MainLayout(SecurityContextService securityContext) {
         this.securityContext = securityContext;
         createHeader();
         createDrawer();
     }

     private void createHeader() {
         H1 logo = new H1("Covoiturage ENSAH");
         logo.getStyle()
             .set("font-size", "var(--lumo-font-size-l)")
             .set("margin", "0");

         String username = securityContext.getCurrentUsername().orElse("User");
         Button logout = new Button(
             "Déconnexion (" + username + ")",
             VaadinIcon.SIGN_OUT.create()
         );
         logout.addClickListener(e -> {
             SecurityContextHolder.clearContext();
             getUI().ifPresent(ui -> {
                 ui.getSession().close();
                 ui.navigate("login");
             });
         });

         HorizontalLayout header = new HorizontalLayout(logo, logout);
         header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
         header.setWidthFull();
         header.expand(logo);
         header.setPadding(true);

         addToNavbar(header);
     }

     private void createDrawer() {
         VerticalLayout nav = new VerticalLayout();
         nav.setSpacing(false);
         nav.setPadding(true);

         RouterLink searchLink = new RouterLink(
             "Rechercher un trajet",
             TripSearchView.class
         );
         searchLink.setRoute(TripSearchView.class);
         searchLink.getStyle().set("padding", "var(--lumo-space-s)");

         RouterLink createTripLink = new RouterLink(
             "Proposer un trajet",
             TripCreationView.class
         );
         createTripLink.getStyle().set("padding", "var(--lumo-space-s)");

         nav.add(searchLink, createTripLink);

         // Menu admin (conditionnel)
         if (securityContext.hasRole("ADMIN")) {
             Span divider = new Span("Administration");
             divider.getStyle()
                 .set("font-weight", "bold")
                 .set("font-size", "var(--lumo-font-size-s)")
                 .set("color", "var(--lumo-secondary-text-color)")
                 .set("padding", "var(--lumo-space-s)")
                 .set("padding-top", "var(--lumo-space-m)");

             RouterLink studentsLink = new RouterLink(
                 "Gestion étudiants",
                 StudentView.class
             );
             studentsLink.getStyle().set("padding", "var(--lumo-space-s)");

             RouterLink whitelistLink = new RouterLink(
                 "Codes autorisés",
                 AdminWhitelistView.class
             );
             whitelistLink.getStyle().set("padding", "var(--lumo-space-s)");

             nav.add(divider, studentsLink, whitelistLink);
         }

         addToDrawer(nav);
     }
 }

 Créer src/main/resources/META-INF/resources/styles/main-layout.css (optionnel) :
 vaadin-app-layout::part(navbar) {
     background-color: var(--lumo-primary-color);
     color: var(--lumo-primary-contrast-color);
 }

 vaadin-app-layout h1 {
     color: var(--lumo-primary-contrast-color);
 }

 4.4 UI Layer - Mise à jour des vues existantes

 Modifier StudentView.java
 @Route(value = "admin/students", layout = MainLayout.class)
 @PageTitle("Gestion Étudiants")
 @RolesAllowed("ADMIN")  // Réservé aux admins
 public class StudentView extends VerticalLayout {
     // Code existant inchangé
     // Juste ajouter les annotations ci-dessus
 }

 Modifier TripCreationView.java
 @Route(value = "proposer-trajet", layout = MainLayout.class)
 @PageTitle("Proposer un trajet")
 @PermitAll
 public class TripCreationView extends VerticalLayout {

     private final TripService tripService;
     // SUPPRIMER : private final StudentService studentService;
     // SUPPRIMER : private final ComboBox<Student> driverSelect;

     // ... autres champs ...

     public TripCreationView(TripService tripService /* Retirer StudentService */) {
         this.tripService = tripService;

         // Configurer le formulaire
         configureForm();

         add(/* ... */);
     }

     private void configureForm() {
         // NE PLUS CRÉER le ComboBox driver

         proposeButton.addClickListener(e -> {
             try {
                 // NOUVELLE SIGNATURE sans driverId
                 tripService.proposeTrip(
                     departureField.getValue(),
                     destinationField.getValue(),
                     departureTime,
                     totalSeatsField.getValue(),
                     regularCheckbox.getValue()
                 );

                 Notification.show(
                     "Trajet proposé avec succès !",
                     3000,
                     Notification.Position.MIDDLE
                 ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                 clearForm();

             } catch (IllegalStateException ex) {
                 Notification.show(
                     "Erreur : Vous devez être connecté",
                     5000,
                     Notification.Position.MIDDLE
                 ).addThemeVariants(NotificationVariant.LUMO_ERROR);
             }
         });
     }
 }

 Modifier TripSearchView.java
 @Route(value = "rechercher-trajet", layout = MainLayout.class)
 @PageTitle("Rechercher un trajet")
 @PermitAll
 public class TripSearchView extends VerticalLayout {
     // Code existant quasi-inchangé
     // Juste ajouter les annotations ci-dessus
 }

 Modifier TripService.java
 @Service
 @Transactional(readOnly = true)
 public class TripService {

     private final ITripRepositoryPort tripRepository;
     private final SecurityContextService securityContext;  // NOUVEAU
     private final StudentService studentService;  // NOUVEAU

     // NOUVELLE SIGNATURE sans driverId
     @Transactional
     public Trip proposeTrip(String departureAddress,
                            String destinationAddress,
                            LocalDateTime departureTime,
                            int totalSeats,
                            boolean isRegular) {

         // Récupérer le conducteur depuis SecurityContext
         String username = securityContext.getCurrentUsername()
             .orElseThrow(() -> new IllegalStateException(
                 "Aucun utilisateur authentifié"
             ));

         Student driver = studentService.getStudentByUsername(username)
             .orElseThrow(() -> new IllegalStateException(
                 "Étudiant non trouvé"
             ));

         // Créer le trajet
         Trip trip = new Trip();
         trip.setDriver(driver);
         trip.setDepartureAddress(departureAddress);
         trip.setDestinationAddress(destinationAddress);
         trip.setDepartureTime(departureTime);
         trip.setTotalSeats(totalSeats);
         trip.setAvailableSeats(totalSeats);
         trip.setRegular(isRegular);

         return tripRepository.save(trip);
     }

     // ... autres méthodes existantes inchangées
 }

 4.5 Tests Phase 4

 - S'inscrire avec code valide (ex: 20240001)
 - Login avec le nouveau compte
 - Vérifier navigation drawer
 - Proposer un trajet → vérifier driver auto-assigné
 - Rechercher le trajet → vérifier affichage
 - Logout
 - Login en admin → vérifier menu admin visible

 ---
 Fichiers critiques à lire/modifier

 À créer (16 fichiers)

 1. domain/model/AllowedStudentCode.java
 2. domain/model/StudentRole.java (optionnel)
 3. application/ports/IAllowedStudentCodeRepositoryPort.java
 4. application/services/AllowedStudentCodeService.java
 5. application/services/AuthenticationService.java
 6. application/services/SecurityContextService.java
 7. infrastructure/adapter/AllowedStudentCodeJpaRepository.java
 8. infrastructure/adapter/AllowedStudentCodeRepositoryAdapter.java
 9. infrastructure/security/UserDetailsServiceImpl.java
 10. infrastructure/config/SecurityConfiguration.java
 11. infrastructure/config/VaadinSecurityConfiguration.java
 12. infrastructure/config/DataInitializer.java
 13. ui/view/LoginView.java
 14. ui/view/RegisterView.java
 15. ui/view/AdminWhitelistView.java
 16. ui/component/MainLayout.java

 À modifier (10 fichiers)

 1. domain/model/Student.java - Ajouter champs auth
 2. application/ports/IStudentRepositoryPort.java - Ajouter méthodes
 3. application/services/StudentService.java - Implémenter méthodes
 4. application/services/TripService.java - Auto-assign driver
 5. infrastructure/adapter/StudentJpaRepository.java - Queries
 6. infrastructure/adapter/StudentRepositoryAdapter.java - Implémentation
 7. ui/view/StudentView.java - Layout + RBAC
 8. ui/view/TripCreationView.java - Retirer driver selector
 9. ui/view/TripSearchView.java - Layout
 10. pom.xml - Dépendances
 11. application.properties - Session config

 ---
 Identifiants par défaut

 Compte admin :
 - Username: admin
 - Password: admin123
 - Email: admin@ensah.ma
 - Code: ADMIN001

 Codes étudiants pré-autorisés :
 - 20240001
 - 20240002
 - 20240003

 ---
 Notes importantes

 1. Ordre d'implémentation : Respecter les 4 phases dans l'ordre
 2. Tests entre phases : Tester après chaque phase avant de continuer
 3. Migration données : Si des étudiants existent déjà, DataInitializer peut gérer
 4. Sécurité passwords : BCrypt force 10 (par défaut)
 5. Session MySQL : Tables créées automatiquement par spring-session
 6. CSRF : Géré automatiquement par Vaadin (rien à faire)
 7. Hexagonal architecture : Toujours respecter Domain → Application → Infrastructure

 ---
 Prochaines étapes après authentification

 Une fois l'authentification complète :
 1. Système de réservation (bouton existe déjà)
 2. Entité Booking/Reservation
 3. Notifications entre étudiants
 4. Filtres avancés de recherche
 5. Profil utilisateur éditable