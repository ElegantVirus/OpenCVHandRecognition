package main.java;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.cvtColor;


public class HandRecognition {

    private static final long serialVersionUID = 1L;
    private JLabel lab = new JLabel();

    private static boolean start = false;
    private static Point finger;

    public static Point getFinger() {
        return finger;
    }

    public static void setFinger(Point finger) {
        HandRecognition.finger = finger;
    }

    public HandRecognition() {

    }

    public void frametolabel(Mat matframe) {
        MatOfByte cc = new MatOfByte();
        Imgcodecs.imencode(".JPG", matframe, cc);
        byte[] bytes = cc.toArray();
        InputStream ss = new ByteArrayInputStream(bytes);
        try {
            BufferedImage aa = ImageIO.read(ss);
            lab.setIcon(new ImageIcon(aa));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Mat colorFilterHSV(double h, double s, double v, double h1, double s1, double v1, Mat image) {
        Mat modified = new Mat();
        if (image != null) {
            Core.inRange(image, new Scalar(h, s, v), new Scalar(h1, s1, v1), modified);
        } else {
            System.out.println("Vaizdo klaida");
        }
        Imgcodecs.imwrite("hsv.png", modified);

        return modified;
    }

    public Mat morphologicalFilter(int kd, int ke, Mat image) {
        Mat result = new Mat();
        Imgproc.erode(image, result, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(ke, ke)));
        Imgproc.dilate(result, result, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(kd, kd)));
        return result;

    }

    public List<MatOfPoint> searchOutline(Mat original, Mat image, boolean drawA, boolean drawB, int pixelFilter) {
        List<MatOfPoint> outline = new LinkedList<MatOfPoint>();
        List<MatOfPoint> contoursbig = new LinkedList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(image, outline, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));

        for (int i = 0; i < outline.size(); i++) {
            if (outline.get(i).size().height > pixelFilter) {
                contoursbig.add(outline.get(i));
                if (drawA && !drawB)
                    Imgproc.drawContours(original, outline, i, new Scalar(0, 255, 0),
                            2, 8, hierarchy, 0, new Point());
            }

            if (drawB && !drawA) {
                Imgproc.drawContours(original, outline, i, new Scalar(0, 255, 255),
                        2, 8, hierarchy, 0, new Point());
            }

        }
        return contoursbig;
    }

    public List<Point> outlineList(Mat image, int pixelFilter) {
        List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        List<MatOfPoint> contoursBig = new LinkedList<MatOfPoint>();
        List<Point> pointsList = new LinkedList<Point>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));

        for (int i = 0; i < contours.size(); i++) {
            if (contours.get(i).size().height > pixelFilter) {
                contoursBig.add(contours.get(i));
            }

        }
        if (contoursBig.size() > 0) {

            pointsList = contoursBig.get(0).toList();

        }
        return pointsList;
    }

    public List<Point> defectEnvelope(Mat image, List<MatOfPoint> outlines, boolean draw, int depthTreshold) {
        List<Point> defects = new LinkedList<Point>();

        for (int i = 0; i < outlines.size(); i++) {
            MatOfInt hull_ = new MatOfInt();
            MatOfInt4 convexityDefects = new MatOfInt4();

            @SuppressWarnings("unused")
            List<Point> outlinePoints = outlines.get(i).toList();

            Imgproc.convexHull(outlines.get(i), hull_);

            if (hull_.size().height >= 4) {


                Imgproc.convexityDefects(outlines.get(i), hull_, convexityDefects);

                List<Point> pts = new ArrayList<Point>();
                MatOfPoint2f pr = new MatOfPoint2f();
                Converters.Mat_to_vector_Point(outlines.get(i), pts);
                pr.create((pts.size()), 1, CvType.CV_32S);
                pr.fromList(pts);
                if (pr.height() > 10) {
                    RotatedRect r = Imgproc.minAreaRect(pr);
                    Point[] rect = new Point[4];
                    r.points(rect);

                    Imgproc.line(image, rect[0], rect[1], new Scalar(0, 100, 0), 2);
                    Imgproc.line(image, rect[0], rect[3], new Scalar(0, 100, 0), 2);
                    Imgproc.line(image, rect[1], rect[2], new Scalar(0, 100, 0), 2);
                    Imgproc.line(image, rect[2], rect[3], new Scalar(0, 100, 0), 2);
                    Imgproc.rectangle(image, r.boundingRect().tl(), r.boundingRect().br(), new Scalar(50, 50, 50));
                }

                int[] buff = new int[4];
                int[] zx = new int[1];
                int[] zxx = new int[1];
                for (int i1 = 0; i1 < hull_.size().height; i1++) {
                    if (i1 < hull_.size().height - 1) {
                        hull_.get(i1, 0, zx);
                        hull_.get(i1 + 1, 0, zxx);
                    } else {
                        hull_.get(i1, 0, zx);
                        hull_.get(0, 0, zxx);
                    }
                    if (draw)
                        Imgproc.line(image, pts.get(zx[0]), pts.get(zxx[0]), new Scalar(140, 140, 140), 2);
                }


                for (int i1 = 0; i1 < convexityDefects.size().height; i1++) {
                    convexityDefects.get(i1, 0, buff);
                    if (buff[3] / 256 > depthTreshold) {
                        if (pts.get(buff[2]).x > 0 && pts.get(buff[2]).x < 1024 && pts.get(buff[2]).y > 0 && pts.get(buff[2]).y < 768) {
                            defects.add(pts.get(buff[2]));
                            Imgproc.circle(image, pts.get(buff[2]), 6, new Scalar(0, 255, 0));
                            if (draw)
                                Imgproc.circle(image, pts.get(buff[2]), 6, new Scalar(0, 255, 0));

                        }
                    }
                }
                if (defects.size() < 3) {
                    int dim = pts.size();
                    Imgproc.circle(image, pts.get(0), 3, new Scalar(0, 255, 0), 2);
                    Imgproc.circle(image, pts.get(0 + dim / 4), 3, new Scalar(0, 255, 0), 2);
                    defects.add(pts.get(0));
                    defects.add(pts.get(0 + dim / 4));


                }
            }
        }
        return defects;
    }

    public Point centreOfPalm(List<Point> defects) {
        MatOfPoint2f pr = new MatOfPoint2f();
        Point center = new Point();
        float[] radius = new float[1];
        pr.create((defects.size()), 1, CvType.CV_32S);
        pr.fromList(defects);

        if (pr.size().height > 0) {
            start = true;
            Imgproc.minEnclosingCircle(pr, center, radius);

        } else {
            start = false;
        }
        return center;

    }

    public List<Point> getFingers(List<Point> outlinePoints, Point center) {
        List<Point> fingerPoints = new LinkedList<Point>();
        List<Point> fingers = new LinkedList<Point>();
        int interval = 55;
        for (int j = 0; j < outlinePoints.size(); j++) {
            Point prec = new Point();
            Point vertice = new Point();
            Point next = new Point();
            vertice = outlinePoints.get(j);
            if (j - interval > 0) {

                prec = outlinePoints.get(j - interval);
            } else {
                int a = interval - j;
                prec = outlinePoints.get(outlinePoints.size() - a - 1);
            }
            if (j + interval < outlinePoints.size()) {
                next = outlinePoints.get(j + interval);
            } else {
                int a = j + interval - outlinePoints.size();
                next = outlinePoints.get(a);
            }

            Point v1 = new Point();
            Point v2 = new Point();
            v1.x = vertice.x - next.x;
            v1.y = vertice.y - next.y;
            v2.x = vertice.x - prec.x;
            v2.y = vertice.y - prec.y;
            double dotproduct = (v1.x * v2.x) + (v1.y * v2.y);
            double length1 = Math.sqrt((v1.x * v1.x) + (v1.y * v1.y));
            double length2 = Math.sqrt((v2.x * v2.x) + (v2.y * v2.y));
            double angle = Math.acos(dotproduct / (length1 * length2));
            angle = angle * 180 / Math.PI;
            if (angle < 60) {
                double centerPrec = Math.sqrt(((prec.x - center.x) * (prec.x - center.x)) +
                        ((prec.y - center.y) * (prec.y - center.y)));
                double centerVert = Math.sqrt(((vertice.x - center.x) * (vertice.x - center.x))
                        + ((vertice.y - center.y) * (vertice.y - center.y)));
                double centerNext = Math.sqrt(((next.x - center.x) * (next.x - center.x))
                        + ((next.y - center.y) * (next.y - center.y)));
                if (centerPrec < centerVert && centerNext < centerVert) {

                    fingerPoints.add(vertice);
                }
            }
        }

        Point media = new Point();
        media.x = 0;
        media.y = 0;
        int med = 0;
        boolean t = false;
        if (fingerPoints.size() > 0) {
            double dif = Math.sqrt(((fingerPoints.get(0).x - fingerPoints.get(fingerPoints.size() - 1).x)
                    * (fingerPoints.get(0).x - fingerPoints.get(fingerPoints.size() - 1).x))
                    + ((fingerPoints.get(0).y
                    - fingerPoints.get(fingerPoints.size() - 1).y)
                    * (fingerPoints.get(0).y - fingerPoints.get(fingerPoints.size() - 1).y)));
            if (dif <= 20) {
                t = true;
            }
        }
        for (int i = 0; i < fingerPoints.size() - 1; i++) {

            double d = Math.sqrt(((fingerPoints.get(i).x - fingerPoints.get(i + 1).x) *
                    (fingerPoints.get(i).x - fingerPoints.get(i + 1).x)) +
                    ((fingerPoints.get(i).y - fingerPoints.get(i + 1).y) *
                            (fingerPoints.get(i).y - fingerPoints.get(i + 1).y)));

            if (d > 20 || i + 1 == fingerPoints.size() - 1) {
                Point p = new Point();

                p.x = (int) (media.x / med);
                p.y = (int) (media.y / med);

                //if(p.x>0 && p.x<1024 && p.y<768 && p.y>0){

                fingers.add(p);
                //}

                if (t && i + 1 == fingers.size() - 1) {
                    Point ult = new Point();
                    if (fingers.size() > 1) {
                        ult.x = (fingers.get(0).x + fingers.get(fingers.size() - 1).x) / 2;
                        ult.y = (fingers.get(0).y + fingers.get(fingers.size() - 1).y) / 2;
                        fingers.set(0, ult);
                        fingers.remove(fingers.size() - 1);
                    }
                }
                med = 0;
                media.x = 0;
                media.y = 0;
            } else {

                media.x = (media.x + fingerPoints.get(i).x);
                media.y = (media.y + fingerPoints.get(i).y);
                med++;

            }
        }

        return fingers;
    }

    public Point movingFilter(List<Point> buffer, Point current) {
        Point media = new Point();
        media.x = 0;
        media.y = 0;
        for (int i = buffer.size() - 1; i > 0; i--) {
            buffer.set(i, buffer.get(i - 1));
            media.x = media.x + buffer.get(i).x;
            media.y = media.y + buffer.get(i).y;
        }
        buffer.set(0, current);
        media.x = (media.x + buffer.get(0).x) / buffer.size();
        media.y = (media.y + buffer.get(0).y) / buffer.size();
        return media;
    }

    public Mat getHand(Mat original) {
        System.out.println("current thread :" + Thread.currentThread());
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Point center = new Point();
        Point finger = new Point();

        List<Point> buffer = new LinkedList<Point>();
        List<Point> buffered = new LinkedList<Point>();
        List<Point> fingers;

        Mat demo = original;
//        blur(original, demo, new Size(3, 3));
        cvtColor(demo, demo, Imgproc.COLOR_BGR2HSV);

        Mat morphological = morphologicalFilter(2, 7, colorFilterHSV(
                0, 10, 60, 20, 150, 255, demo));
//        modified = morphologicalFilter(2, 7, colorFilterHSV(0, 100, 0, 100, 255, 40, original));
        Imgcodecs.imwrite("morphologicalFilter.png", morphological);

        List<Point> defects = defectEnvelope(original, searchOutline(original, morphological,
                false, false, 450), true, 5);

        if (buffer.size() < 7) {
            buffer.add(centreOfPalm(defects));
        } else {
            center = movingFilter(buffer, centreOfPalm(defects));
        }

        fingers = getFingers(outlineList(morphological, 200), center);

        if (fingers.size() == 1 && buffered.size() < 5) {
            buffered.add(fingers.get(0));
            finger = fingers.get(0);
        } else {
            if (fingers.size() == 1) {
                finger = movingFilter(buffered, fingers.get(0));
            }
        }
//        System.out.println(finger.x+" "+finger.y);
        setFinger(finger);
        frametolabel(original);
        return original;

    }
}



