package EMAIL;

import CODES.CODES;
import Offload.SeprateTask;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Properties;

public class Email {

    // Singleton instance ensures that only one Email object
    // is created and reused throughout the application.
    private static Email email;

    // SecureRandom generates unpredictable OTP values.
    private final SecureRandom random = new SecureRandom();

    // Stores the currently active OTP.
    private String otp;

    // Stores the exact time at which the OTP expires.
    private Instant otpExpiry = Instant.now().plusSeconds(120);


    // Returns the single Email instance used by the application.
    public static Email getInstance() {

        if (email == null) {
            email = new Email();
        }

        return email;
    }


    // Returns the currently stored OTP.
    public String getOtp() {
        return otp;
    }


    // Returns the expiry time of the current OTP.
    public Instant getOtpExpiry() {
        return otpExpiry;
    }


    // Private constructor prevents other classes from
    // creating multiple Email objects.
    private Email() {
    }


    // Generates a six-digit OTP, stores it and sends it
    // to the specified recipient.
    public String sendOTP(String recipientEmail) {

        // Generates a number from 000000 to 999999.
        otp = String.format(
                "%06d",
                random.nextInt(1_000_000)
        );

        // OTP remains valid for 5 minutes.otpExpiry = Instant.now().plusSeconds(120);


        // Creates the HTML email body.
        String html = """
                <html>
                <body style="font-family: Arial;">
                    <h2>Email Verification</h2>
                    <p>Your verification code is:</p>
                    <h1>%s</h1>
                    <p>This code expires in 2 minutes.</p>
                </body>
                </html>
                """.formatted(otp);


        // Sends the email.
        sendEmail(
                recipientEmail,
                "Your Verification Code",
                html
        );

        return otp;
    }


    // Checks whether the supplied OTP matches the stored OTP
    // and whether the five-minute validity period has expired.
    public CODES isOTPValid(String enteredOTP) {

        if (otp == null || otpExpiry == null) {
            return CODES.OTPINV;
        }

        if (Instant.now().isAfter(otpExpiry)) {
            return CODES.OTPEXP;
        }

        return otp.equals(enteredOTP)? CODES.SUCCESS: CODES.OTPINV;
    }


    // Handles the SMTP configuration and sends the email.
    private void sendEmail(
            String recipientEmail,
            String subject,
            String html
    ) {

        Properties properties = new Properties();

        // Specifies the SMTP server and port.
        properties.put(
                "mail.smtp.host",
                EmailConfig.EMAIL_HOST
        );

        properties.put(
                "mail.smtp.port",
                EmailConfig.EMAIL_PORT
        );

        // SMTP authentication is required.
        properties.put(
                "mail.smtp.auth",
                "true"
        );

        // Encrypts the SMTP connection using STARTTLS.
        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );


        // Creates an authenticated email session.
        Session session = Session.getInstance(
                properties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                EmailConfig.EMAIL_USERNAME,
                                EmailConfig.EMAIL_PASSWORD
                        );
                    }
                }
        );


        try {

            // Creates the email message.
            Message message = new MimeMessage(session);

            // Sets the sender.
            message.setFrom(
                    new InternetAddress(
                            EmailConfig.EMAIL_USERNAME
                    )
            );

            // Sets the recipient.
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail)
            );

            // Sets the subject.
            message.setSubject(subject);

            // Sets the HTML body.
            message.setContent(
                    html,
                    "text/html; charset=UTF-8"
            );

            // Sends the email.
            SeprateTask.getInstance().offload(()-> {
                try {
                    Transport.send(message);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

            System.out.println("Sent otp successfully....");

        } catch (MessagingException e) {

            // Handles errors when creating or sending
            // the email.
            System.out.println("Error sending email");
        }

    }
}