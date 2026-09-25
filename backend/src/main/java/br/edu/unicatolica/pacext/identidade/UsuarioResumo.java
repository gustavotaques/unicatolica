package br.edu.unicatolica.pacext.identidade;

/** Dados de um usuário que outros módulos podem exibir (ex.: nome do autor de uma publicação). */
public record UsuarioResumo(Long id, String nome, String curso) {
}
