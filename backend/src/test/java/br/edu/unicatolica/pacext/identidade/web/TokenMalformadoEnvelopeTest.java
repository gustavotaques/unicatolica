package br.edu.unicatolica.pacext.identidade.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Prova end-to-end (Defeito D2) de que {@link
 * br.edu.unicatolica.pacext.infraestrutura.seguranca.JwtSecurityFilter} é o único ponto do
 * backend que responde por autenticação, como a AD-2 promete — inclusive quando o token
 * tem formato de JWT (três segmentos separados por ".") mas é inválido (assinatura ou
 * claims incorretos), não só quando está ausente ou vazio.
 *
 * <p>Antes do fix, o mecanismo nativo do {@code quarkus-smallrye-jwt} (ativo na camada
 * HTTP, antes de qualquer filtro JAX-RS rodar — ver o mesmo caminho de execução exposto
 * pelo defeito D1) intercepta esse caso e responde 401 com corpo vazio, nunca alcançando
 * {@code JwtSecurityFilter} nem o envelope de erro padrão (AD-5).</p>
 */
@QuarkusTest
class TokenMalformadoEnvelopeTest {

    @Test
    void tokenComFormatoJwtInvalidoDevolve401ComEnvelopeDeErroAD5() {
        given().header("Authorization", "Bearer xxx.yyy.zzz")
                .when().get("/usuarios/me")
                .then().statusCode(401)
                .body("error.code", equalTo("NAO_AUTENTICADO"));
    }
}
