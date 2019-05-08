package main.java;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.java.Utilities.Piano;
import main.java.Utilities.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.List;
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
    public static List<Point> front;
    public static List<Point> side;
    public static Point globalFront = new Point(0, 0);
    public static Point globalSide = new Point(0, 0);
    static int counter = 50;
    private static String SECOND_CAMERA_ID = "http://192.168.8.104:4747/video";

    @FXML
    private boolean startFirstCamera(ActionEvent event) {
        HandRecognition handRecognition = new HandRecognition();
        if (!this.firstCameraActive) {
            this.firstCapture.open(FIRST_CAMERA_ID);

            if (this.firstCapture.isOpened()) {
//                firstCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 400);
//                firstCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 400);
                this.firstCameraActive = true;
                firstButton.setText("Stop");
                grabFrame(firstCurrentFrame, alteredFirstFrame, firstCapture, true, false,
                        handRecognition);
            } else {
                System.err.println("Camera is unaccessible...");
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
        HandRecognition handRecognition = new HandRecognition();
        loadNecessaryLibs();

        if (!this.secondCameraActive) {
            this.secondCapture.open(SECOND_CAMERA_ID);

            if (this.secondCapture.isOpened()) {
                secondCapture.set(Videoio.CAP_PROP_FRAME_WIDTH, 200);
                secondCapture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 200);
                this.secondCameraActive = true;
                secondButton.setText("Stop");
                grabFrame(secondCurrentFrame, alteredSecondFrame, secondCapture, false, true,
                        handRecognition);
            } else {
                System.err.println("Camera is unaccessible...");
            }
        } else {
            this.secondCameraActive = false;
            secondButton.setText("Start");
            this.stopAcquisition(secondCapture);
        }
        return this.secondCameraActive;
    }

    public synchronized void resolveNote() {
//        if (null != front)
//            front.forEach(thing -> System.out.print("x :" + thing.x + " y : " + thing.y));
//        System.out.println("x :" + globalFront.x);
//        System.out.println("y :" + globalSide.y);
        //0..640
        if (firstCameraActive && secondCameraActive) {
            counter--;
            if (counter == 0) {
                try {
                    if (globalFront.y < 300) {
                        System.out.println("x : " + globalFront.x);
                        if (globalFront.x > 0 && globalFront.x < 128
                                && globalSide.y < 180 && globalSide.y > 0) {
                            Thread t1 = new Thread(new Piano(1));
                            t1.start();
                        }

                        if (globalFront.x > 128 && globalFront.x < 256
                                && globalSide.y < 250 && globalSide.y > 180) {
                            Thread t1 = new Thread(new Piano(3));
                            t1.start();
                        }

                        if (globalFront.x > 256 && globalFront.x < 384
                                && globalSide.y < 300 && globalSide.y > 250) {
                            Thread t1 = new Thread(new Piano(6));
                            t1.start();
                        }

                        if (globalFront.x > 384 && globalFront.x < 512
                                && globalSide.y < 450 && globalSide.y > 300) {
                            Thread t1 = new Thread(new Piano(9));
                            t1.start();
                        }
                        if (globalFront.x > 512 && globalFront.x < 640
                                && globalSide.y < 500 && globalSide.y > 450) {
                            Thread t1 = new Thread(new Piano(12));
                            t1.start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                counter = 50;
            }
        }

    }


    private void loadNecessaryLibs() {
        System.load("D:/programs/opencv/build/x64/vc14/bin/opencv_ffmpeg401_64.dll");
    }

    private void grabFrame(ImageView currentFrame, ImageView additionalFrame, VideoCapture capture,
                           boolean isFront, boolean isSide, HandRecognition handRecognition) {

        Runnable frameGrabber = () -> {
            Mat frame = grabFrame(capture);
            Mat original = frame;
            updateImageView(currentFrame, Utils.mat2Image(original));
            Mat imageToShow = null;
            try {
                imageToShow = handRecognition.getHand(frame);
//                System.out.println(HandRecognition.getFinger().x);
                List<Point> fingers = handRecognition.getFingers();
                Point finger = handRecognition.getFinger();
                if (isFront == true) {
                    front = fingers;
                    globalFront = finger;
                } else {
                    if (isSide == true) {
                        side = fingers;
                        globalSide = finger;
                    }
                }
                resolveNote();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != additionalFrame) {
                updateImageView(additionalFrame, Utils.mat2Image(imageToShow));

            }
        };

        this.timer = Executors.newSingleThreadScheduledExecutor();
        this.timer.scheduleAtFixedRate(frameGrabber, 0, 40, TimeUnit.MILLISECONDS);
    }

    private Mat grabFrame(VideoCapture capture) {
        Mat frame = new Mat();

        if (capture.isOpened()) {
            try {
                capture.read(frame);
            } catch (Exception e) {
                System.err.println("Isimtis vaizdo apdorojimo metu: " + e);
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