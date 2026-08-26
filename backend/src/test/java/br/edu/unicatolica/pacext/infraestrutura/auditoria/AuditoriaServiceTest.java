package br.edu.unicatolica.pacext.infraestrutura.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Testa {@link AuditoriaService} isoladamente com um {@link EntityManager} mockado —
 * evita subir o runtime completo do Quarkus/Postgres (indisponível neste ambiente sem
 * Docker) apenas para validar a lógica de registro genérico exigida pela AD-11.
 */
class AuditoriaServiceTest {

    @Test
    void registraEventoComEntidadeAssociada() {
        EntityManager entityManager = mock(EntityManager.class);
        AuditoriaService service = new AuditoriaService();
        service.entityManager = entityManager;

        service.registrar(7L, "comunidades", "COMUNIDADE_ALTERADA", "Comunidade", 3L, "campo nome alterado");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(entityManager).persist(captor.capture());

        LogAuditoria log = captor.getValue();
        assertEquals(7L, log.usuarioId);
        assertEquals("comunidades", log.modulo);
        assertEquals("COMUNIDADE_ALTERADA", log.acao);
        assertEquals("Comunidade", log.entidadeAfetada);
        assertEquals(3L, log.entidadeId);
        assertEquals("campo nome alterado", log.detalhes);
        assertNotNull(log.criadoEm);
    }

    @Test
    void registraEventoSemEntidadeAssociada() {
        EntityManager entityManager = mock(EntityManager.class);
        AuditoriaService service = new AuditoriaService();
        service.entityManager = entityManager;

        service.registrar(1L, "identidade", "LOGIN", "login bem-sucedido");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(entityManager).persist(captor.capture());

        LogAuditoria log = captor.getValue();
        assertEquals("identidade", log.modulo);
        assertEquals("LOGIN", log.acao);
        assertEquals(null, log.entidadeAfetada);
        assertEquals(null, log.entidadeId);
    }

    @Test
    void exigeModuloEAcaoObrigatorios() {
        AuditoriaService service = new AuditoriaService();
        service.entityManager = mock(EntityManager.class);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(1L, null, "LOGIN", "detalhe"));
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(1L, "identidade", null, "detalhe"));
        verify(service.entityManager, org.mockito.Mockito.never()).persist(any());
    }
}
