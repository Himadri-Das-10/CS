package Controller;

import Backend.Backend;
import CODES.CODES;
import EMAIL.Email;
import SceneManager.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.time.Instant;

public class Otp {

    private Timeline timer;
    @FXML
    private Button enterBtn;

    @FXML
    private TextField otpTF;

    @FXML
    private Label resendLabel;

    @FXML
    private Label timerLabel;


    @FXML
    public void initialize()
    {
        Email.getInstance().sendOTP(Home.getInstance().getEmail());
        startTimer();
    }



    @FXML
    void enterBtnClicked(ActionEvent event)
    {
        String otp = otpTF.getText().strip();

        if (CODES.SUCCESS.equals(Email.getInstance().isOTPValid(otp)))
        {
            System.out.println("OTP entered successfully");
            Backend.getInstance().addUser(Home.getInstance().getEmail(), Home.getInstance().getUsername());

            SceneManager.getInstance().changeScene("mainPage");

        }
        else if (CODES.OTPEXP.equals(Email.getInstance().isOTPValid(otp)))
        {
            otpTF.clear();
            otpTF.setPromptText("Your OTP expired");
        }
        else
        {
            otpTF.clear();
            otpTF.setPromptText("Invalid OTP");
        }
    }

    @FXML
    void resendLabelClicked(MouseEvent event) {

        // Prevent clicking while the label is disabled
        if (resendLabel.isDisable()) {
            return;
        }

        // Generate and send a new OTP
        Email.getInstance().sendOTP(Home.getInstance().getEmail());

        // Disable the resend label again
        disableResendLabel();

        // Restart the 1-minute countdown
        startTimer();
    }





    private void startTimer() {

        // Resend should not be available while OTP is valid
        disableResendLabel();

        timer = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> updateTimer()
                )
        );

        timer.setCycleCount(Timeline.INDEFINITE);

        timer.play();

        updateTimer();
    }

    private void updateTimer() {

        Instant expiry = Email.getInstance().getOtpExpiry();

        long remaining = java.time.Duration.between(
                Instant.now(),
                expiry
        ).getSeconds();

        if (remaining <= 0) {

            timerLabel.setText("OTP expired");

            timer.stop();

            // OTP expired → allow resend
            enableResendLabel();

            return;
        }

        timerLabel.setText(
                "OTP expires in: " + remaining + "s"
        );
    }







    private void disableResendLabel() {

        resendLabel.setDisable(true);

        // Grey while disabled
        resendLabel.setStyle(
                "-fx-text-fill: grey;"
        );
    }

    private void enableResendLabel() {

        resendLabel.setDisable(false);

        // White when available
        resendLabel.setStyle(
                "-fx-text-fill: white;"
        );
    }

}
