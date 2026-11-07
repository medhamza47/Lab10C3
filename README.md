# TP 10 : Formulaire de connexion personnalisé avec Spring Security

Projet réalisé dans le cadre du cours **Développement JakartaEE : Spring (ID: 17)** — **Lab 209 : TP 10 : Formulaire de connexion personnalisé**.

---

## 📌 Objectifs du TP

Ce projet met en œuvre la personnalisation intégrale du flux d'authentification avec Spring Security :
- Remplacement du formulaire par défaut par une interface HTML personnalisée sous Thymeleaf.
- Interception de la soumission du formulaire via l'endpoint dédié `loginProcessingUrl("/authenticate")`.
- Gestion des paramètres de retour :
  - Erreur d'authentification (`/login?error=true`).
  - Déconnexion confirmée (`/login?logout=true`).
- Configuration fine des redirections après authentification réussie (`defaultSuccessUrl("/home", true)`).
- Contrôle d'accès basé sur les rôles (`ROLE_USER`, `ROLE_ADMIN`).

---

## 🏗️ Structure du Projet

```text
src
 ├─ main
 │   ├─ java/ma/fstg/security
 │   │   ├─ SpringSecurityDemoApplication.java
 │   │   ├─ config/
 │   │   │   └─ SecurityConfig.java
 │   │   └─ web/
 │   │       └─ AuthController.java
 │   └─ resources/
 │       ├─ application.properties
 │       └─ templates/
 │           ├─ login.html
 │           ├─ home.html
 │           ├─ admin-dashboard.html
 │           └─ user-dashboard.html
 └─ test
     └─ java/ma/fstg/security
         ├─ SpringSecurityDemoApplicationTests.java
         └─ SecurityIntegrationTests.java
```

---

## 👥 Comptes Utilisateurs (Authentification en mémoire)

| Utilisateur | Mot de passe | Rôle attribué | Accès autorisés |
| :--- | :--- | :--- | :--- |
| **`user`** | `1111` (`{noop}1111`) | `ROLE_USER` | `/home`, `/user/dashboard` |
| **`admin`** | `1234` (`{noop}1234`) | `ROLE_ADMIN` | `/home`, `/user/dashboard`, `/admin/dashboard` |

---

## 🔒 Configuration de la Sécurité (`SecurityConfig`)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/css/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/authenticate")
            .defaultSuccessUrl("/home", true)
            .failureUrl("/login?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout=true")
            .permitAll()
        );

    return http.build();
}
```

---

## 📋 Tableau d'Analyse du Comportement

| Action | Comportement attendu |
| :--- | :--- |
| Tentative d’accès sans login (`/home`, `/user/dashboard`) | Redirection HTTP 302 vers `/login` |
| Connexion réussie (`user` ou `admin`) | Redirection vers `/home` |
| Mauvais mot de passe | Retour sur `/login?error=true` avec affichage du message d'erreur |
| Déconnexion (`/logout`) | Redirection vers `/login?logout=true` avec confirmation |
| Accès à `/admin/dashboard` avec le rôle `USER` | Erreur HTTP 403 – Accès refusé |

---

## 🚀 Tests et Exécution

### Exécution des tests automatisés
```bash
mvn clean test
```
Tous les scénarios d'authentification, de contrôle d'accès et de déconnexion sont couverts par `SecurityIntegrationTests.java`.

### Lancement du serveur
```bash
mvn spring-boot:run
```
Ouvrir dans le navigateur : [http://localhost:8080/login](http://localhost:8080/login).
