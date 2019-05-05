package main.java;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.java.Utilities.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraController {
    @FXML
    public ImageView alteredSecondFrame;
    @FXML
    private Button firstButton;
    @FXML
    private Button secondButton;
    @FXML
    private ImageView firstCurrentFrame;
    @FXML
    private ImageView alteredFirstFrame;
    @FXML
    private ImageView secondCurrentFrame;
    private ScheduledExecutorService timer;
    private VideoCapture firstCapture = new VideoCapture();
    private VideoCapture secondCapture = new VideoCapture();
    private boolean firstCameraActive = false;
    private boolean secondCameraActive = false;
    private static int FIRST_CAMERA_ID = 0;
    HandRecognition handRecognition = new HandRecognition();

    private static String SECOND_CAMERA_ID = "http://192.168.8.104:4747/video";
    private static boolean handLow;

    @FXML
    private boolean startFirstCamera(ActionEvent event) {
        if (!this.firstCameraActive) {
            this.firstCapture.open(FIRST_CAMERA_ID);

            if (this.firstCapture.isOpened()) {
                firstCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 200);
                firstCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 200);
                this.firstCameraActive = true;
                firstButton.setText("Stop");
                grabFrame(firstCurrentFrame, alteredFirstFrame, firstCapture);
            } else {
                System.err.println("Neimanoma prisijungti prie kameros...");
            }
        } else {
            this.firstCameraActive = false;
            firstButton.setText("Start");
            this.stopAcquisition(firstCapture);
        }
        return this.firstCameraActive;
    }

    @FXML
    private boolean startSecondCamera(ActionEvent event) {

        loadNecessaryLibs();

        if (!this.secondCameraActive) {
            this.secondCapture.open(SECOND_CAMERA_ID);

            if (this.secondCapture.isOpened()) {
                secondCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 200);
                secondCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 200);
                this.secondCameraActive = true;
                secondButton.setText("Stop");
                grabFrame(secondCurrentFrame, alteredSecondFrame, secondCapture);
            } else {
                System.err.println("Neimanoma prisijungti prie kameros...");
            }
        } else {
            this.secondCameraActive = false;
            secondButton.setText("Start");
            this.stopAcquisition(secondCapture);
        }
        return this.secondCameraActive;
    }

    private void loadNecessaryLibs() {
        System.load("D:/programs/opencv/build/x64/vc14/bin/opencv_ffmpeg401_64.dll");
    }

    private void grabFrame(ImageView currentFrame, ImageView additionalFrame, VideoCapture capture) {

        Runnable frameGrabber = () -> {
            Mat frame = grabFrame(capture);
            Mat original = frame;
            updateImageView(currentFrame, Utils.mat2Image(original));
            Mat imageToShow = null;
            handLow = false;
            try {
                imageToShow = handRecognition.getHand(frame);

//                if (handRecognition.getFinger().y > 450) {
//                    handLow = true;
////                    System.out.println("X: " + handRecognition.getFinger().x + " y: " + handRecognition.getFinger().y);
//                }
                // note = new Note();
                //        Piano piano = new Piano(note);
                //        new Thread(piano).start();
                //
                //        if (fingah < 200 && fingah > 0) {
                //            System.out.println("C");
                //            note.notify();
                ////            Sound.playNote(1);
                ////            player.play("C");
                //        }
                //     System.out.println(handLow);
            } catch (Exception e) {
                // e.printStackTrace();
            }
            if (null != additionalFrame) {
                updateImageView(additionalFrame, Utils.mat2Image(imageToShow));

            }
        };

        this.timer = Executors.newSingleThreadScheduledExecutor();
        this.timer.scheduleAtFixedRate(frameGrabber, 0, 40, TimeUnit.MILLISECONDS);
    }

    private void grabX() {

    }

    private void grabYZ() {

    }

    private Mat grabFrame(VideoCapture capture) {
        Mat frame = new Mat();

        if (capture.isOpened()) {
            try {
                capture.read(frame);
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }


    private void stopAcquisition(VideoCapture capture) {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Isimtis stabdo kadro fiksavima, bandoma atlaisvinti kamera " + e);
            }
        }

        if (capture.isOpened()) {
            capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopAcquisition(secondCapture);
        this.stopAcquisition(firstCapture);
    }

}