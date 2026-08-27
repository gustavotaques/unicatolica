package br.edu.unicatolica.pacext.identidade.dominio;

/**
 * Lançada quando e-mail ou senha estão incorretos, ou o e-mail ainda não foi
 * confirmado — a mensagem exposta ao cliente é sempre genérica (RF07), nunca indica
 * qual das duas condições falhou, para não vazar quais e-mails existem na base.
 */
public class CredenciaisInvalidasException extends RuntimeException {
}
