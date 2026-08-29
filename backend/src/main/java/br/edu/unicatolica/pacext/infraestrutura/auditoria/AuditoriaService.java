package br.edu.unicatolica.pacext.infraestrutura.auditoria;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;

/**
 * Serviço injetável de registro de auditoria (AD-11 da Architecture Spine, RNF07).
 *
 * <p>Todo módulo que precisa registrar um evento de auditoria (login, denúncia, remoção
 * de conteúdo, alteração administrativa, ...) injeta este serviço — infraestrutura
 * transversal, não pertence a nenhum dos 12 módulos de domínio. Nenhum módulo cria a
 * própria tabela de log nem escreve em {@code log_auditoria} diretamente.</p>
 */
@ApplicationScoped
public class AuditoriaService {

    private static final int LIMITE_MODULO = 50;
    private static final int LIMITE_ACAO = 100;
    private static final int LIMITE_ENTIDADE_AFETADA = 100;

    @Inject
    EntityManager entityManager;

    /**
     * Registra um evento de auditoria de forma genérica — qualquer módulo pode chamar
     * este método para qualquer tipo de evento, sem precisar de um método dedicado por
     * módulo ou por tipo de ação.
     *
     * @param usuarioId       id do usuário que praticou a ação; {@code null} se não houver ator humano
     * @param modulo          nome do módulo de origem do evento (ex.: "identidade")
     * @param acao            código da ação auditada (ex.: "LOGIN")
     * @param entidadeAfetada tipo da entidade de domínio afetada, ou {@code null}
     * @param entidadeId      id da entidade de domínio afetada, ou {@code null}
     * @param detalhes        detalhes livres do evento, ou {@code null}
     */
    @Transactional
    public void registrar(Long usuarioId, String modulo, String acao, String entidadeAfetada, Long entidadeId,
            String detalhes) {
        if (modulo == null || modulo.isBlank()) {
            throw new IllegalArgumentException("modulo é obrigatório para registrar um evento de auditoria.");
        }
        if (acao == null || acao.isBlank()) {
            throw new IllegalArgumentException("acao é obrigatória para registrar um evento de auditoria.");
        }
        if (modulo.length() > LIMITE_MODULO) {
            throw new IllegalArgumentException("modulo excede o limite de " + LIMITE_MODULO + " caracteres.");
        }
        if (acao.length() > LIMITE_ACAO) {
            throw new IllegalArgumentException("acao excede o limite de " + LIMITE_ACAO + " caracteres.");
        }
        if (entidadeAfetada != null && entidadeAfetada.length() > LIMITE_ENTIDADE_AFETADA) {
            throw new IllegalArgumentException(
                    "entidadeAfetada excede o limite de " + LIMITE_ENTIDADE_AFETADA + " caracteres.");
        }

        LogAuditoria log = new LogAuditoria();
        log.usuarioId = usuarioId;
        log.modulo = modulo;
        log.acao = acao;
        log.entidadeAfetada = entidadeAfetada;
        log.entidadeId = entidadeId;
        log.detalhes = detalhes;
        log.criadoEm = Instant.now();

        entityManager.persist(log);
    }

    /** Sobrecarga para eventos sem entidade de domínio associada (ex.: login). */
    @Transactional
    public void registrar(Long usuarioId, String modulo, String acao, String detalhes) {
        registrar(usuarioId, modulo, acao, null, null, detalhes);
    }
}
