package br.edu.unicatolica.pacext.identidade.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Usuário do módulo Identidade e Acesso (RF05-RF13) — dono exclusivo da tabela
 * {@code usuario} (AD-3). Só o cadastro (Story 1.2, fora do escopo desta história) cria
 * novos registros em produção; esta história (1.4) só lê.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "nome", nullable = false, length = 200)
    public String nome;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    public String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    public String senhaHash;

    /** Perfil global do usuário (RF13) — ex.: "ALUNO", "MODERADOR". */
    @Column(name = "perfil", nullable = false, length = 30)
    public String perfil;

    /** Login só é permitido com e-mail confirmado (Story 1.3, RF06). */
    @Column(name = "email_confirmado", nullable = false)
    public boolean emailConfirmado;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;
}
