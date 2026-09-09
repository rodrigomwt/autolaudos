package br.com.vista.rvm.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;
	
	@Value("${app.url}")
	private String url;

	public void sendActivationEmail(String to, String token) {
		String link = url + "/?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setFrom("no-reply@autolaudos.com.br");
		message.setSubject("Cadastro de senha - Auto Laudos");
		message.setText("""
				Olá!

				Recebemos uma solicitação para o cadastro de senha, seja para o primeiro acesso ou redefinir uma nova senha.
				Clique no link abaixo para definir sua senha:

				%s

				Este link expira em 1 hora.

				Se você não solicitou isso, ignore este email.
				""".formatted(link));

		mailSender.send(message);
	}
	
	public void sendOrderFinished(String to, String placa) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setFrom("no-reply@autolaudos.com.br");
		message.setSubject("Consulta - Auto Laudos");
		message.setText("""
				Olá!

				Sua consulta referente a placa %s já está disponivel, basta acessar nosso site e efetuar o login.

				Se você não solicitou isso, ignore este email.
				""".formatted(placa));

		mailSender.send(message);
	}
}