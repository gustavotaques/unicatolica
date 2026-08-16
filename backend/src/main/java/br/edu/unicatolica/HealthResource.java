package br.edu.unicatolica;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.Map;

/**
 * Rota inicial da API — checkpoint PAC VI (ponto de partida mínimo).
 * Serve como health check enquanto os módulos de RF01-RF80 (ver
 * docs/unicatolica-pacext-contexto.md) são implementados.
 */
@Path("/api/health")
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "unicatolica-backend",
                "timestamp", Instant.now().toString()
        );
    }
}
