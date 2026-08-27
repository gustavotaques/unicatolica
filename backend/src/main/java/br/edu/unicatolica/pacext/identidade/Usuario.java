package br.edu.unicatolica.pacext.identidade;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade {@code usuario} — módulo Identidade (Story 1.2/1.3). Perfil padrão mínimo
 * (RF05): nome e curso, capturados no cadastro sem depender do Epic 4 (Perfil Acadêmico).
 * Ver modelo em {@code _bmad-output/implementation-artifacts/modelo-dados-semana-1.md}.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "nome", nullable = false, length = 150)
    public String nome;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    public String senhaHash;

    @Column(name = "data_nascimento", nullable = false)
    public LocalDate dataNascimento;

    /**
     * Texto informado no cadastro (RF24.1) — casado em runtime pelo módulo Comunidades
     * contra {@code comunidade.nome} onde {@code tipo='CURSO'}. Ver
     * modelo-dados-semana-1.md, decisão "curso como texto solto em usuario".
     */
    @Column(name = "curso", nullable = false, length = 150)
    public String curso;

    @Column(name = "email_confirmado", nullable = false)
    public boolean emailConfirmado = false;

    @Column(name = "token_confirmacao_email", length = 255)
    public String tokenConfirmacaoEmail;

    @Column(name = "token_confirmacao_expira_em")
    public Instant tokenConfirmacaoExpiraEm;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    @Column(name = "atualizado_em")
    public Instant atualizadoEm;

    /** Claim {@code roles} do JWT (AD-2) — carregado em EAGER por ser pequeno e sempre necessário. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_papel", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "papel")
    @Enumerated(EnumType.STRING)
    public Set<Papel> papeis = new HashSet<>();
}
