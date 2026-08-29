package br.edu.unicatolica.pacext.infraestrutura.email;

import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Serviço injetável de envio de e-mail — infraestrutura transversal (mesmo espírito de
 * {@link br.edu.unicatolica.pacext.infraestrutura.auditoria.AuditoriaService}), não
 * pertence a nenhum módulo de domínio.
 *
 * <p>Sem {@code quarkus.mailer.host} configurado, o Quarkus usa uma mailbox mock em
 * dev/{@code %test} (nada sai pela rede; visível em {@code /q/dev-ui}) — SMTP real só é
 * exigido em produção. Configuração de SMTP de produção fica fora desta implementação
 * (ver deferred-work.md).</p>
 */
@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    public void enviarConfirmacaoCadastro(String destinatario, String nome, String linkConfirmacao) {
        String corpo = """
                Olá, %s!

                Confirme seu e-mail para começar a usar a UniCatólica:

                %s

                Se você não fez este cadastro, ignore esta mensagem.
                """.formatted(nome, linkConfirmacao);

        mailer.send(Mail.withText(destinatario, "Confirme seu e-mail — UniCatólica", corpo));
    }
}
