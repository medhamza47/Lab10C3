package ma.fstg.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Scénario 1: Accès public à la page de connexion '/login' autorisé sans authentification")
    void shouldAllowPublicAccessToLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Scénario 2: Tentative d'accès sans login vers '/home' redirige vers '/login'")
    void shouldRedirectAnonymousUserToLoginForHome() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Scénario 3: Tentative d'accès sans login vers '/user/dashboard' redirige vers '/login'")
    void shouldRedirectAnonymousUserToLoginForUserDashboard() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Scénario 4: Tentative d'accès sans login vers '/admin/dashboard' redirige vers '/login'")
    void shouldRedirectAnonymousUserToLoginForAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Scénario 5: Connexion réussie via POST '/authenticate' avec le compte 'user' / '1111' redirige vers '/home'")
    void shouldAuthenticateValidUser() throws Exception {
        mockMvc.perform(post("/authenticate")
                        .param("username", "user")
                        .param("password", "1111")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(authenticated().withUsername("user").withRoles("USER"));
    }

    @Test
    @DisplayName("Scénario 6: Connexion réussie via POST '/authenticate' avec le compte 'admin' / '1234' redirige vers '/home'")
    void shouldAuthenticateValidAdmin() throws Exception {
        mockMvc.perform(post("/authenticate")
                        .param("username", "admin")
                        .param("password", "1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(authenticated().withUsername("admin").withRoles("ADMIN"));
    }

    @Test
    @DisplayName("Scénario 7: Mauvais mot de passe redirige vers '/login?error=true'")
    void shouldFailAuthenticationWithBadCredentials() throws Exception {
        mockMvc.perform(post("/authenticate")
                        .param("username", "user")
                        .param("password", "wrongpass")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Scénario 8: Utilisateur avec rôle 'USER' a accès à '/user/dashboard'")
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldAllowUserRoleToAccessUserDashboard() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-dashboard"));
    }

    @Test
    @DisplayName("Scénario 9: Utilisateur avec rôle 'USER' reçoit Erreur 403 – Accès refusé sur '/admin/dashboard'")
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldDenyUserRoleFromAccessingAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scénario 10: Utilisateur avec rôle 'ADMIN' a accès à '/admin/dashboard'")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAdminRoleToAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"));
    }

    @Test
    @DisplayName("Scénario 11: Utilisateur avec rôle 'ADMIN' a aussi accès à '/user/dashboard'")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldAllowAdminRoleToAccessUserDashboard() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-dashboard"));
    }

    @Test
    @DisplayName("Scénario 12: Déconnexion via POST '/logout' redirige vers '/login?logout=true'")
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(unauthenticated());
    }
}
