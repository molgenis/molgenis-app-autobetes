package plugins.forum;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lmd.LmdUser;

public class Small
{

	public boolean sendActivationMail(LmdUser fu)
	{
		String subject= "Aanmelding";
		// localhost:8080 -> voedingsdagboek.nl
		String message = "Beste " + fu.getFirstName() + ",<BR/><BR/>Je kunt nu je account activeren door op de onderstaande link te klikken.<BR/><A HREF=\"http://voedingsdagboek.nl/molgenis.do?__target=ForumPlugin&__action=activate&activationCode=" + fu.getActivationCode() + "\">Activeer mijn account en log in</A> (inloggen alleen eerste keer automatisch).<BR/><BR/><BR/>Hartelijke groeten en veel plezier,<BR/><BR/>Het Voedingsdagboek-team<BR/><BR/>PS Je kunt ook de volgende link in je browser kopieren/plakken: http://voedingsdagboek.nl/molgenis.do?__target=ForumPlugin&__action=activate&activationCode=" + fu.getActivationCode();
		return sendMail(fu.getEmail(), subject, message);
	}
	
	public boolean sendFeedback(String subject, String message)
	{
		return sendMail("info@voedingsdagboek.nl", subject, message);
	}
	
	public boolean sendMail(String to, String subject, String message)
	{
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication("info@voedingsdagboek.nl", "xxx");
			}
		});

		try
		{
			Message mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress("info@voedingsdagboek.nl"));
			mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			mimeMessage.setSubject(subject);
			mimeMessage.setContent(message, "text/html");

			Transport.send(mimeMessage);

			System.out.println("E-mail sturen gelukt!");
			return true;

		} catch (MessagingException e)
		{
			return false;
			// throw new RuntimeException(e);
		}
	}
}
