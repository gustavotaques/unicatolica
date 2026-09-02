package br.edu.unicatolica.pacext.infraestrutura.seguranca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * {@link JwtSecurityFilterTest} monta o {@code JWTAuthContextInfo} manualmente, sem subir o
 * runtime completo do Quarkus (sem Docker para os Dev Services de Postgres neste ambiente) —
 * então uma mudança em {@code application.properties}, como remover ou renomear
 * {@code smallrye.jwt.path.groups}, passaria despercebida por aqueles testes. Este teste lê o
 * {@code application.properties} real (o mesmo carregado pelo Quarkus via CDI em runtime) e
 * garante que ele configura exatamente o que os testes do filtro assumem manualmente,
 * fechando essa lacuna sem depender de um ambiente com Docker.
 */
class ApplicationPropertiesJwtConfigTest {

    private Properties carregarApplicationProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            assertNotNull(in, "application.properties precisa estar no classpath.");
            properties.load(in);
        }
        return properties;
    }

    @Test
    void mapeiaClaimRolesParaGetGroups() throws IOException {
        Properties properties = carregarApplicationProperties();
        assertEquals("roles", properties.getProperty("smallrye.jwt.path.groups"),
                "smallrye.jwt.path.groups precisa ser 'roles' (AD-2) — se mudar aqui sem "
                        + "atualizar JwtSecurityFilterTest, o filtro para de reconhecer a claim "
                        + "'roles' em produção sem que nenhum teste acuse.");
    }

    @Test
    void issuerEsperadoBateComOUsadoNosTestesDoFiltro() throws IOException {
        Properties properties = carregarApplicationProperties();
        assertEquals("${JWT_ISSUER:https://pacext.unicatolica.edu.br}",
                properties.getProperty("mp.jwt.verify.issuer"),
                "issuer padrão precisa continuar batendo com JwtSecurityFilterTest.ISSUER.");
    }

    @Test
    void chavePublicaVemDeVariavelDeAmbienteNuncaDeArquivoCommitado() throws IOException {
        Properties properties = carregarApplicationProperties();
        assertEquals("${JWT_PUBLIC_KEY:}", properties.getProperty("mp.jwt.verify.publickey"),
                "a chave pública precisa vir de JWT_PUBLIC_KEY (env var) — nenhuma chave, "
                        + "de dev ou produção, pode ficar commitada no repo (RNF04/OWASP ASVS).");
    }
}
