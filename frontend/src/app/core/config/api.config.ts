/**
 * URL base da API Quarkus. Frontend e backend rodam em origens diferentes tanto local
 * (:4200 vs :8080, AD-7) quanto em produção (Render Static Site vs Web Service, AD-6) —
 * comunicação sempre cross-origin via CORS liberado centralmente no backend, nunca proxy
 * same-origin. Fixo por enquanto; vira config por ambiente quando o deploy real existir.
 */
export const API_BASE_URL = 'http://localhost:8080';
