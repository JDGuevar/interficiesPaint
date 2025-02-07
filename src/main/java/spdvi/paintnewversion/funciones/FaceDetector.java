/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package spdvi.paintnewversion.funciones;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;


/**
 *
 * @author Usuario
 */
public class FaceDetector {
    
    public static void main(String[] args){
        File dll = new File("src\\main\\java\\spdvi\\paintnewversion\\funciones\\opencv_java490.dll");
        System.load(dll.getAbsolutePath());
        Mat image = Imgcodecs.imread("images\\abdullah.jpg");
        // mètode detector
        detectAndSave(image);
    }

    private static void detectAndSave(Mat image) {
        //create som objectes
        MatOfRect faces = new MatOfRect();
        
        
        // TO GRAY SCALE
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(image,grayFrame, Imgproc.COLOR_BGR2GRAY);
        
        
        // improve contrast for better result
        Imgproc.equalizeHist(grayFrame, grayFrame);
        
        
        // mida mínima cares detectables en píxels
        int height = grayFrame.height();
        int absoluteFaceSize = 0;
        if (Math.round(height*0.2f)>0){
            absoluteFaceSize = Math.round(height * 0.2f);
        }
        
        // Detect faces
        CascadeClassifier faceCascade = new CascadeClassifier();
        
        //load trained data file
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0|Objdetect.CASCADE_SCALE_IMAGE, 
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        
        // Write to file
        Rect[] faceArray = faces.toArray();
        for (int i=0; i<faceArray.length; i++){
            //draw rect
            Imgproc.rectangle(image, faceArray[i], new Scalar(255,123,45), 3);
        }
        
        Imgcodecs.imwrite("images/output.jpg", image);
                
    }
    
}
