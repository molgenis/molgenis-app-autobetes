package org.molgenis.autobetes.controller;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;

public class Small
{
	private MolgenisSettings ms;

	public Small(MolgenisSettings ms)
	{
		this.ms = ms;
	}

	public boolean sendActivationMail(String email, String activationUrl)
	{
		try
		{
			System.err.println(">> ZIE IK DIT? " + ms.getProperty("admin.password"));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String subject = "Registration";
		String message = "Dear user "
				+ ",<BR/><BR/>To activate your account, please visit <A HREF=\""
				+ activationUrl
				+ "\">this link</A>.<BR/><BR/><BR/>Best regards,<BR/><BR/>The Autobetes team<BR/><BR/>PS Alternatively paste this link in your browser: "
				+ activationUrl;
		return sendMail(email, subject, message);
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
				return new PasswordAuthentication("autobetes@gmail.com", "xxx");
			}
		});

		try
		{
			Message mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress("autobetes@gmail.com"));
			mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			mimeMessage.setSubject(subject);
			mimeMessage.setContent(message, "text/html");

			Transport.send(mimeMessage);

			System.out.println("E-mail sturen gelukt!");
			return true;

		}
		catch (MessagingException e)
		{
			System.err.println(">> ERRROR >> " + e);
			return false;
			// throw new RuntimeException(e);
		}
	}
}
