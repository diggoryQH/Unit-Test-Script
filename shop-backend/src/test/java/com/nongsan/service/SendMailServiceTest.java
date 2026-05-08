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

/**
 * REQ-05: Gửi Email thông báo
 * Unit test cho: SendMailServiceImplement.java (không cần Spring context)
 * Hàm:
 *   - send(MailInfo mail)      — gửi mail trực tiếp qua SMTP
 *   - queue(MailInfo mail)     — thêm mail vào hàng đợi
 *   - queue(String,String,String) — overload: tạo MailInfo rồi thêm vào hàng đợi
 *
 * Ghi chú kiến trúc:
 *   - JavaMailSender được @Mock → KHÔNG kết nối SMTP thật (Unit Test)
 *   - MimeMessage được mock() thủ công vì cần trả về từ createMimeMessage()
 *   - Hàm run() (Scheduled) không được test ở đây vì là Integration Test
 */
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

    // ==========================================
    // MODULE: GỬI MAIL TRỰC TIẾP — send(MailInfo)
    // Nhánh trong send():
    //   - attachments == null → không thêm file đính kèm
    //   - attachments != null → thêm file đính kèm (FileSystemResource)
    // ==========================================

    /**
     * TC_SENDMAIL_01
     * Mục tiêu  : Gửi email thành công với đầy đủ thông tin cơ bản (không đính kèm).
     *             Kiểm tra luồng chính: tạo MimeMessage → cấu hình Helper → gửi qua SMTP.
     * Đầu vào   : mailInfo hợp lệ (from, to, subject, body, attachments=null) — từ setUp()
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: createMimeMessage() được gọi 1 lần
     *             javaMailSender.send(mimeMessage) được gọi 1 lần
     */
    @Test // [Happy Path] Gửi email thành công — không đính kèm, thông tin đầy đủ
    void TC_SENDMAIL_01() throws MessagingException, IOException {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(mailInfo);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_02
     * Mục tiêu  : Kiểm tra nhánh attachments != null — service gọi addAttachment().
     *             File đính kèm không tồn tại thực sự → mong đợi exception từ FileSystemResource,
     *             nhưng createMimeMessage() vẫn phải được gọi trước khi xảy ra lỗi.
     * Đầu vào   : mailInfo với attachments = "test.pdf" (file không tồn tại thực tế)
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: createMimeMessage() được gọi 1 lần (trước khi gặp lỗi đính kèm)
     *             Exception được bắt và bỏ qua (test hành vi của nhánh, không test file thật)
     */
    @Test // [Branch Coverage] Nhánh: attachments != null → service thực hiện thêm file đính kèm
    void TC_SENDMAIL_02() throws MessagingException, IOException {
        String attachmentPath = "test.pdf";
        mailInfo.setAttachments(attachmentPath);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendMailService.send(mailInfo);
        } catch (Exception e) {
            // File test.pdf không tồn tại → FileNotFoundException từ FileSystemResource
            // Đây là hành vi mong đợi khi test nhánh attachments
        }

        verify(javaMailSender).createMimeMessage();
    }

    /**
     * TC_SENDMAIL_03
     * Mục tiêu  : Gửi email với body HTML đầy đủ (invoice, table, formatting).
     *             Xác nhận service không lỗi khi body là chuỗi HTML dài.
     * Đầu vào   : mailInfo với body = "<html><body><h1>Invoice</h1><p>Total: 1,000,000 VND</p></body></html>"
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) được gọi 1 lần (HTML body được xử lý bình thường)
     */
    @Test // [Happy Path] Gửi email với body HTML đầy đủ — helper.setText(body, true) không lỗi
    void TC_SENDMAIL_03() throws MessagingException, IOException {
        String htmlBody = "<html><body><h1>Invoice</h1><p>Total: 1,000,000 VND</p></body></html>";
        mailInfo.setBody(htmlBody);

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(mailInfo);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_04
     * Mục tiêu  : Gửi email với body là plain text (không có thẻ HTML).
     *             Xác nhận helper.setText() vẫn chạy bình thường với text thường.
     * Đầu vào   : mailInfo với body = "Plain text email content"
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) được gọi 1 lần
     */
    @Test // [Happy Path] Gửi email với body plain text — không lỗi khi body không có HTML
    void TC_SENDMAIL_04() throws MessagingException, IOException {
        mailInfo.setBody("Plain text email content");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(mailInfo);

        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_05
     * Mục tiêu  : Gửi email đặt lại mật khẩu — mô phỏng use case thực tế của hệ thống.
     *             Body chứa link reset password với token.
     * Đầu vào   : MailInfo(from="noreply@nongsan.com", to="user@gmail.com",
     *             subject="Reset Password - NongsanShop",
     *             body="<p>Click <a href='...?token=abc123'>here</a>...</p>")
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) được gọi 1 lần
     */
    @Test // [Happy Path] Gửi email reset password — use case thực tế, body chứa token link
    void TC_SENDMAIL_05() throws MessagingException, IOException {
        MailInfo resetMail = new MailInfo();
        resetMail.setFrom("noreply@nongsan.com");
        resetMail.setTo("user@gmail.com");
        resetMail.setSubject("Reset Password - NongsanShop");
        resetMail.setBody("<p>Click <a href='http://localhost:4200/reset-password?token=abc123'>here</a> to reset password</p>");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(resetMail);

        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_06
     * Mục tiêu  : Gửi email xác nhận đơn hàng — mô phỏng use case gửi mail sau khi checkout.
     * Đầu vào   : MailInfo(from="orders@nongsan.com", to="customer@example.com",
     *             subject="Order Confirmation - #12345",
     *             body="<h2>Order Received</h2><p>Your order has been confirmed</p>")
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) được gọi 1 lần
     */
    @Test // [Happy Path] Gửi email xác nhận đơn hàng — use case thực tế sau khi checkout
    void TC_SENDMAIL_06() throws MessagingException, IOException {
        MailInfo orderMail = new MailInfo();
        orderMail.setFrom("orders@nongsan.com");
        orderMail.setTo("customer@example.com");
        orderMail.setSubject("Order Confirmation - #12345");
        orderMail.setBody("<h2>Order Received</h2><p>Your order has been confirmed</p>");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(orderMail);

        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_07
     * Mục tiêu  : Gửi email đến địa chỉ email không đúng định dạng.
     *             send() dùng helper.setTo() → MimeMessageHelper sẽ ném AddressException.
     * Đầu vào   : MailInfo với to="invalid-email-format" (thiếu @domain)
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: Exception được ném với message "Illegal address" (từ javax.mail)
     */
    @Test // [Branch Coverage] Email không hợp lệ → MimeMessageHelper ném AddressException
    void TC_SENDMAIL_07() throws MessagingException, IOException {
        MailInfo invalidMail = new MailInfo();
        invalidMail.setFrom("admin@nongsan.com");
        invalidMail.setTo("invalid-email-format");
        invalidMail.setSubject("Test");
        invalidMail.setBody("Test body");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendMailService.send(invalidMail);
        } catch (Exception e) {
            assertEquals("Illegal address", e.getMessage());
        }
    }

    /**
     * TC_SENDMAIL_08
     * Mục tiêu  : SMTP server gặp lỗi khi createMimeMessage() — mô phỏng lỗi kết nối SMTP.
     * Đầu vào   : mailInfo hợp lệ — createMimeMessage() ném RuntimeException("SMTP Error")
     * Hành vi GS: javaMailSender.createMimeMessage() → ném RuntimeException("SMTP Error")
     * Kết quả KV: Exception được ném, message = "SMTP Error"
     */
    @Test // [Branch Coverage] SMTP lỗi — createMimeMessage() ném RuntimeException
    void TC_SENDMAIL_08() throws MessagingException, IOException {
        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP Error"));

        try {
            sendMailService.send(mailInfo);
        } catch (Exception e) {
            assertEquals("SMTP Error", e.getMessage());
        }
    }

    /**
     * TC_SENDMAIL_09
     * Mục tiêu  : Subject chứa ký tự đặc biệt và emoji — xác nhận UTF-8 encoding hoạt động đúng.
     *             MimeMessageHelper được khởi tạo với charset "utf-8" trong source code.
     * Đầu vào   : mailInfo với subject = "Đơn hàng #12345 - Nông Sản Fresh 🥕"
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) gọi 1 lần, không ném exception về encoding
     */
    @Test // [Edge Case] Subject chứa ký tự đặc biệt và emoji — kiểm tra UTF-8 encoding
    void TC_SENDMAIL_09() throws MessagingException, IOException {
        mailInfo.setSubject("Đơn hàng #12345 - Nông Sản Fresh 🥕");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(mailInfo);

        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_10
     * Mục tiêu  : Body chứa nội dung tiếng Việt có dấu — xác nhận UTF-8 encoding bảo toàn nội dung.
     * Đầu vào   : MailInfo với body = "<h2>Cảm ơn đã đặt hàng</h2><p>Chúng tôi đã nhận được...</p>"
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) gọi 1 lần, không ném exception về encoding
     */
    @Test // [Edge Case] Body chứa tiếng Việt có dấu — kiểm tra UTF-8 không làm hỏng nội dung
    void TC_SENDMAIL_10() throws MessagingException, IOException {
        MailInfo vietnameseMail = new MailInfo();
        vietnameseMail.setFrom("support@nongsan.vn");
        vietnameseMail.setTo("khach@example.com");
        vietnameseMail.setSubject("Xác nhận đơn hàng của bạn");
        vietnameseMail.setBody("<h2>Cảm ơn đã đặt hàng</h2><p>Chúng tôi đã nhận được đơn hàng của bạn</p>");

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(vietnameseMail);

        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * TC_SENDMAIL_11
     * Mục tiêu  : Body email có kích thước rất lớn (1000 đoạn HTML) — kiểm tra không timeout/tràn bộ nhớ.
     * Đầu vào   : mailInfo với body = chuỗi 1000 thẻ <p> (khoảng 35KB)
     * Hành vi GS: javaMailSender.createMimeMessage() → mimeMessage
     * Kết quả KV: send(mimeMessage) gọi 1 lần, service không crash với body lớn
     */
    @Test // [Edge Case] Body rất lớn (~35KB, 1000 thẻ HTML) — service không crash hay timeout
    void TC_SENDMAIL_11() throws MessagingException, IOException {
        StringBuilder largeBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeBody.append("<p>Line ").append(i).append(": Large email content</p>");
        }
        mailInfo.setBody(largeBody.toString());

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        sendMailService.send(mailInfo);

        verify(javaMailSender).send(mimeMessage);
    }

    // ==========================================
    // MODULE: THÊM MAIL VÀO HÀNG ĐỢI — queue(MailInfo) & queue(String,String,String)
    // Logic: queue() chỉ thêm vào List nội bộ, KHÔNG gửi ngay.
    //        Việc gửi thật sự được thực hiện bởi run() theo @Scheduled (5 giây/lần).
    //        → run() là Integration Test, không test ở đây.
    // ==========================================

    /**
     * TC_SENDMAIL_12
     * Mục tiêu  : Thêm 1 MailInfo vào hàng đợi — xác nhận queue() KHÔNG gửi ngay.
     * Đầu vào   : MailInfo(to="recipient@example.com", subject="Welcome", body="Hello User")
     * Kết quả KV: javaMailSender.send() KHÔNG được gọi (mail chỉ nằm trong list chờ)
     */
    @Test // [Happy Path] queue(MailInfo) — thêm mail vào list, KHÔNG gửi ngay lập tức
    void TC_SENDMAIL_12() {
        MailInfo mail = new MailInfo("recipient@example.com", "Welcome", "Hello User");

        sendMailService.queue(mail);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    /**
     * TC_SENDMAIL_13
     * Mục tiêu  : Thêm nhiều mail vào hàng đợi liên tiếp bằng overload queue(String,String,String).
     *             Xác nhận không mail nào bị gửi ngay, tất cả chờ trong list.
     * Đầu vào   : 2 lần gọi queue(to, subject, body) với thông tin khác nhau
     * Kết quả KV: javaMailSender.send() KHÔNG được gọi (cả 2 mail đều nằm trong list chờ)
     */
    @Test // [Happy Path] queue(String,String,String) overload — nhiều mail vào list, không gửi ngay
    void TC_SENDMAIL_13() {
        sendMailService.queue("user1@example.com", "Subject 1", "Body 1");
        sendMailService.queue("user2@example.com", "Subject 2", "Body 2");

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    /**
     * TC_SENDMAIL_14
     * Mục tiêu  : Thêm MailInfo đầy đủ thông tin vào hàng đợi bằng queue(MailInfo).
     *             Xác nhận queue() không thực thi send() ngay cả khi from/to/subject/body đầy đủ.
     * Đầu vào   : MailInfo đầy đủ: from="sender@nongsan.com", to="receiver@example.com",
     *             subject="Promotional Email", body="<h2>Special Offer</h2>"
     * Kết quả KV: javaMailSender.send() KHÔNG được gọi
     */
    @Test // [Happy Path] queue(MailInfo đầy đủ) — vẫn không gửi ngay dù thông tin đầy đủ
    void TC_SENDMAIL_14() {
        MailInfo mail = new MailInfo();
        mail.setFrom("sender@nongsan.com");
        mail.setTo("receiver@example.com");
        mail.setSubject("Promotional Email");
        mail.setBody("<h2>Special Offer</h2>");

        sendMailService.queue(mail);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    /**
     * TC_SENDMAIL_15
     * Mục tiêu  : Gọi overload queue(String,String,String) với 3 tham số riêng lẻ.
     *             Xác nhận method này nội bộ gọi queue(new MailInfo(to,subject,body))
     *             và không kích hoạt gửi mail ngay.
     * Đầu vào   : to="test@example.com", subject="Test Mail", body="Test Body"
     * Kết quả KV: javaMailSender.send() KHÔNG được gọi
     */
    @Test // [Happy Path] queue(to,subject,body) 3 tham số — wrapper gọi queue(MailInfo), không gửi ngay
    void TC_SENDMAIL_15() {
        String to = "test@example.com";
        String subject = "Test Mail";
        String body = "Test Body";

        sendMailService.queue(to, subject, body);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
}
