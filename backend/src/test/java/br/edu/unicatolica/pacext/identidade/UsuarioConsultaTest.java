package br.edu.unicatolica.pacext.identidade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.unicatolica.pacext.identidade.dominio.UsuarioRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Prova contra o Postgres real que a consulta em lote de {@link UsuarioConsulta} funciona. */
@QuarkusTest
class UsuarioConsultaTest {

    @Inject
    UsuarioConsulta usuarioConsulta;

    @Inject
    UsuarioRepository usuarioRepository;

    @Test
    void buscarResumosDevolveSoOsIdsExistentes() {
        Long id = usuarioRepository.buscarPorEmail("aluno.teste@catolicasc.edu.br").orElseThrow().id;

        Map<Long, UsuarioResumo> resumos = usuarioConsulta.buscarResumos(List.of(id, 999_999L));

        assertEquals(1, resumos.size());
        assertEquals(id, resumos.get(id).id());
    }
}
