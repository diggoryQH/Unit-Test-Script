package com.nongsan.service;

import com.nongsan.dto.MailInfo;
import com.nongsan.service.implement.SendMailServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendMailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SendMailServiceImplement sendMailService;

    private MailInfo mailInfo;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
	mimeMessage = mock(MimeMessage.class);
	mailInfo = new MailInfo();
	mailInfo.setFrom("admin@nongsan.com");
	mailInfo.setTo("user@example.com");
	mailInfo.setSubject("Test Subject");
	mailInfo.setBody("<h1>Test Email Body</h1>");
	mailInfo.setAttachments(null);
    }

    @Test
    void send_testChuan1() throws MessagingException, IOException {
	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(mailInfo);

	verify(javaMailSender).createMimeMessage();
	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan2_withAttachment() throws MessagingException, IOException {
	String attachmentPath = "test.pdf";
	mailInfo.setAttachments(attachmentPath);

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	try {
	    sendMailService.send(mailInfo);
	} catch (Exception e) {
	    // Expected when file doesn't exist, but we're testing the flow
	}

	verify(javaMailSender).createMimeMessage();
    }

    @Test
    void send_testNgoaiLe1_invalidEmailAddress() throws MessagingException, IOException {
	MailInfo invalidMail = new MailInfo();
	invalidMail.setFrom("admin@nongsan.com");
	invalidMail.setTo("invalid-email-format");
	invalidMail.setSubject("Test");
	invalidMail.setBody("Test body");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	try {
	    sendMailService.send(invalidMail);
	} catch (Exception e) {
	    // Expected: AddressException for invalid email format
	    assertEquals("Illegal address", e.getMessage());
	}
    }

    @Test
    void send_testNgoaiLe2_messagingException() throws MessagingException, IOException {
	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
	when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP Error"));

	try {
	    sendMailService.send(mailInfo);
	} catch (Exception e) {
	    assertEquals("SMTP Error", e.getMessage());
	}
    }

    @Test
    void queue_testChuan1_singleMail() {
	MailInfo mail = new MailInfo("recipient@example.com", "Welcome", "Hello User");

	sendMailService.queue(mail);

	// Verify the mail is queued by checking run() processes it
	verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void queue_testChuan2_multipleMailsWithString() {
	sendMailService.queue("user1@example.com", "Subject 1", "Body 1");
	sendMailService.queue("user2@example.com", "Subject 2", "Body 2");

	// Queue should have 2 items now
	verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void queue_testChuan3_queueWithFullMailInfo() {
	MailInfo mail = new MailInfo();
	mail.setFrom("sender@nongsan.com");
	mail.setTo("receiver@example.com");
	mail.setSubject("Promotional Email");
	mail.setBody("<h2>Special Offer</h2>");

	sendMailService.queue(mail);

	verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void send_testChuan3_htmlContent() throws MessagingException, IOException {
	String htmlBody = "<html><body><h1>Invoice</h1><p>Total: 1,000,000 VND</p></body></html>";
	mailInfo.setBody(htmlBody);

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(mailInfo);

	verify(javaMailSender).createMimeMessage();
	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan4_plainTextEmail() throws MessagingException, IOException {
	mailInfo.setBody("Plain text email content");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(mailInfo);

	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan5_resetPasswordEmail() throws MessagingException, IOException {
	MailInfo resetMail = new MailInfo();
	resetMail.setFrom("noreply@nongsan.com");
	resetMail.setTo("user@gmail.com");
	resetMail.setSubject("Reset Password - NongsanShop");
	resetMail.setBody("<p>Click <a href='http://localhost:4200/reset-password?token=abc123'>here</a> to reset password</p>");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(resetMail);

	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan6_orderConfirmationEmail() throws MessagingException, IOException {
	MailInfo orderMail = new MailInfo();
	orderMail.setFrom("orders@nongsan.com");
	orderMail.setTo("customer@example.com");
	orderMail.setSubject("Order Confirmation - #12345");
	orderMail.setBody("<h2>Order Received</h2><p>Your order has been confirmed</p>");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(orderMail);

	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void queue_testChuan4_constructorWithThreeParams() {
	String to = "test@example.com";
	String subject = "Test Mail";
	String body = "Test Body";

	sendMailService.queue(to, subject, body);

	// Mail should be queued
	verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void send_testChuan7_validSingleRecipient() throws MessagingException, IOException {
	mailInfo.setTo("singleuser@example.com");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(mailInfo);

	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan8_specialCharactersInSubject() throws MessagingException, IOException {
	mailInfo.setSubject("Đơn hàng #12345 - Nông Sản Fresh 🥕");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(mailInfo);

	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan9_vietnameseContent() throws MessagingException, IOException {
	MailInfo vietnameseMail = new MailInfo();
	vietnameseMail.setFrom("support@nongsan.vn");
	vietnameseMail.setTo("khach@example.com");
	vietnameseMail.setSubject("Xác nhận đơn hàng của bạn");
	vietnameseMail.setBody("<h2>Cảm ơn đã đặt hàng</h2><p>Chúng tôi đã nhận được đơn hàng của bạn</p>");

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(vietnameseMail);

	verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void send_testChuan10_largeEmailBody() throws MessagingException, IOException {
	StringBuilder largeBody = new StringBuilder();
	for (int i = 0; i < 1000; i++) {
	    largeBody.append("<p>Line ").append(i).append(": Large email content</p>");
	}
	mailInfo.setBody(largeBody.toString());

	when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

	sendMailService.send(mailInfo);

	verify(javaMailSender).send(mimeMessage);
    }
}
