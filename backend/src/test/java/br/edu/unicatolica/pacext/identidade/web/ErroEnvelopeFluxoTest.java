package br.edu.unicatolica.pacext.identidade.web;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

/**
 * Prova sobre o pipeline HTTP real que as exceções de domínio do módulo viram o envelope
 * padrão (AD-5) pelo {@code ApiExceptionMapper}, com o mesmo status, código e mensagem de
 * antes da reestruturação (PR 4), quando cada uma tinha mapper próprio ou era montada à
 * mão no Resource.
 */
@QuarkusTest
class ErroEnvelopeFluxoTest {

    private static final String EMAIL_CONFIRMADO = "aluno.teste@catolicasc.edu.br";

    @Inject
    PrivateKey privateKey;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Test
    void senhaErradaDevolve401CredencialInvalida() {
        given().contentType(JSON)
                .body(Map.of("email", EMAIL_CONFIRMADO, "senha", "senha-errada"))
                .when().post("/auth/login")
                .then().statusCode(401)
                .contentType(startsWith("application/json"))
                .body("error.code", equalTo("CREDENCIAL_INVALIDA"))
                .body("error.message", equalTo("E-mail ou senha inválidos."));
    }

    @Test
    void alunoConsultandoOutroUsuarioDevolve403AcessoNegado() {
        given().header("Authorization", "Bearer " + token("ALUNO"))
                .when().get("/usuarios/1")
                .then().statusCode(403)
                .contentType(startsWith("application/json"))
                .body("error.code", equalTo("ACESSO_NEGADO"))
                .body("error.message", equalTo("Você não tem permissão para executar esta ação."));
    }

    @Test
    void moderadorConsultandoIdInexistenteDevolve404() {
        given().header("Authorization", "Bearer " + token("MODERADOR"))
                .when().get("/usuarios/999999")
                .then().statusCode(404)
                .contentType(startsWith("application/json"))
                .body("error.code", equalTo("RECURSO_NAO_ENCONTRADO"))
                .body("error.message", equalTo("Usuário não encontrado."));
    }

    /** Token de um usuário que não existe no banco — o {@code SessaoInvalidadaFilter} deixa passar. */
    private String token(String perfil) {
        return Jwt.claims()
                .issuer(issuer)
                .subject("999999")
                .claim("roles", Set.of(perfil))
                .sign(privateKey);
    }
}
