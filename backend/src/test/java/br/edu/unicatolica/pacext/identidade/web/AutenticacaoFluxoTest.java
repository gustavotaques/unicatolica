package br.edu.unicatolica.pacext.identidade.web;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Prova end-to-end (Defeito D1) de que o fluxo login -> acesso autenticado -> logout ->
 * acesso rejeitado funciona sobre o pipeline HTTP/JAX-RS real, coisa que nenhum outro
 * teste do módulo cobre — os demais instanciam recursos/filtro diretamente com mocks e
 * nunca disparam a chamada bloqueante ao banco que {@link
 * br.edu.unicatolica.pacext.infraestrutura.seguranca.JwtSecurityFilter} faz na thread de
 * I/O do Vert.x.
 *
 * <p>Antes do fix, esta suíte falha já no segundo passo ({@code GET /usuarios/me} logo
 * após o login) com 401 — não só no passo pós-logout — porque toda requisição autenticada
 * dispara {@code BlockingOperationNotAllowedException} dentro do filtro, engolida
 * silenciosamente e disfarçada de token inválido.</p>
 */
@QuarkusTest
class AutenticacaoFluxoTest {

    private static final String EMAIL_CONFIRMADO = "aluno.teste@catolicasc.edu.br";
    private static final String SENHA = "Senha123!";

    @Test
    void loginDaAcessoELogoutInvalidaSessao() {
        String token = given().contentType(JSON)
                .body(Map.of("email", EMAIL_CONFIRMADO, "senha", SENHA))
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("token");

        given().header("Authorization", "Bearer " + token)
                .when().get("/usuarios/me")
                .then().statusCode(200);

        given().header("Authorization", "Bearer " + token)
                .when().post("/auth/logout")
                .then().statusCode(204);

        given().header("Authorization", "Bearer " + token)
                .when().get("/usuarios/me")
                .then().statusCode(401);
    }
}
