package com.ramo.networkexperiment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

import android.util.Log;

public class GMailSender extends javax.mail.Authenticator {
	private String mailhost = "smtp.gmail.com";
	private String user;
	private String password;
	private Session session;

	public GMailSender(String user, String password) {
		this.user = user;
		this.password = password;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

		session = Session.getDefaultInstance(props, this);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	public synchronized void sendMail(String subject, String body,
			String sender, String recipients) throws Exception {
		MimeMessage message = new MimeMessage(session);
		DataHandler handler = new DataHandler(new ByteArrayDataSource(
				body.getBytes(), "text/plain"));
		message.setSender(new InternetAddress(sender));
		message.setSubject(subject);
		message.setDataHandler(handler);
		if (recipients.indexOf(',') > 0)
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipients));
		else
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(
					recipients));
		Transport.send(message);
	}
	
	
	public synchronized void sendMailWithFile(String subject, String body, String sender,
			String recipients, String filePath, String fileName)
	{
		try
		{
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(sender));
			msg.setSubject(subject);
			//msg.setContent(body, "text/html;charset=EUC-KR");
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(
					recipients));

			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(body, "text/html;charset=EUC-KR");
			
			MimeBodyPart attachPart = new MimeBodyPart();
			attachPart.setDataHandler(new DataHandler(new FileDataSource(
					new File(filePath))));
			attachPart.setFileName(fileName);
			Multipart multipart=new MimeMultipart();
			multipart.addBodyPart(bodyPart);
			multipart.addBodyPart(attachPart);
			
			msg.setContent(multipart);
			
			
			Transport.send(msg);
			Log.d("lastiverse", "sent");
		} catch (Exception e)
		{
			Log.d("lastiverse", "Exception occured : ");
			Log.d("lastiverse", e.toString());
			Log.d("lastiverse", e.getMessage());
		} // try-catch
	} // void sendMailWithFile
	

	public class ByteArrayDataSource implements DataSource, javax.activation.DataSource {
		private byte[] data;
		private String type;

		public ByteArrayDataSource(byte[] data, String type) {
			super();
			this.data = data;
			this.type = type;
		}

		public ByteArrayDataSource(byte[] data) {
			super();
			this.data = data;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getContentType() {
			if (type == null)
				return "application/octet-stream";
			else
				return type;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		public String getName() {
			return "ByteArrayDataSource";
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("Not Supported");
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLogWriter(PrintWriter arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLoginTimeout(int arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isWrapperFor(Class<?> arg0) throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Connection getConnection() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Connection getConnection(String arg0, String arg1)
				throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}
	}
}


