package br.edu.unicatolica.pacext.infraestrutura.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Entidade da tabela {@code log_auditoria} — trilha de auditoria única e compartilhada
 * exigida pela RNF07 (AD-11 da Architecture Spine). Infraestrutura transversal: não
 * pertence a nenhum dos 12 módulos de domínio. Só é gravada através de
 * {@link AuditoriaService} — nenhum módulo escreve nesta tabela diretamente.
 */
@Entity
@Table(name = "log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    /** Id do usuário que praticou a ação auditada; nulo para eventos sem ator humano. */
    @Column(name = "usuario_id")
    public Long usuarioId;

    /** Nome do módulo que originou o evento (ex.: "identidade", "comunidades"). */
    @Column(name = "modulo", nullable = false, length = 50)
    public String modulo;

    /** Código da ação auditada (ex.: "LOGIN", "DENUNCIA_CRIADA", "COMUNIDADE_ALTERADA"). */
    @Column(name = "acao", nullable = false, length = 100)
    public String acao;

    /** Tipo da entidade de domínio afetada, quando aplicável (ex.: "Comunidade"). */
    @Column(name = "entidade_afetada", length = 100)
    public String entidadeAfetada;

    /** Id da entidade de domínio afetada, quando aplicável. */
    @Column(name = "entidade_id")
    public Long entidadeId;

    /** Detalhes livres do evento (texto ou JSON serializado como texto). */
    @Column(name = "detalhes", columnDefinition = "text")
    public String detalhes;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;
}
