package frc.robot;

import java.util.ArrayList;
import java.util.List;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;

/**
 * This is the code for the Wheel of Fortune manipulator. It initializes motor
 * controllers and has methods for its various functions.
 * 
 * @author dri, ai, gjs
 */
public class WheelOfFortune {
    private static WheelOfFortune instance;

    private enum WOFColor {
        RED, GREEN, BLUE, YELLOW, NONE;
    }

    private WPI_TalonSRX primary;
    private String gameData;
    private ColorSensorV3 colorSensor;
    private ColorMatch colorMatch;
    private Color detectedColor;
    private List<String> colorOrder = new ArrayList<String>();
    
  
    private final Color blueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color greenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color redTarget = ColorMatch.makeColor(0.487, 0.360, 0.152);
    private final Color yellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

    private WheelOfFortune() {
        primary = new WPI_TalonSRX(RobotMap.WheelOfFortune.MOTOR);
        // TODO: should this be periodic?
        gameData = DriverStation.getInstance().getGameSpecificMessage();
        colorSensor = new ColorSensorV3(RobotMap.WheelOfFortune.COLOR_SENSOR);
        colorMatch = new ColorMatch();

        colorMatch.addColorMatch(blueTarget);
        colorMatch.addColorMatch(greenTarget);
        colorMatch.addColorMatch(redTarget);
        colorMatch.addColorMatch(yellowTarget);
    }

    /**
     * Checks to see if the instance of this class has already been created.
     * If so, return it. If not, create it and return it.
     * @return instance of the WheelOfFortune class
     */
    public static WheelOfFortune getInstance() {
        if (instance == null) {
            return new WheelOfFortune();
        }
        return instance;
    }
    private void setColorOrder(){
        colorOrder.add("B");
        colorOrder.add("G");
        colorOrder.add("R");
        colorOrder.add("Y");

    }

    /**
     * Sets the speed of the mechanism.
     * @param speed the speed from -1 to 1
     */
    public void setSpeed(double speed) {
        primary.set(speed);
    }

    /**
     * takes game data sent by the FMS and accordingly returns the Color which we need to target
     * @param gameData the data sent by the FMS
     * @return WOFColor enum for the sensor to target
     */
    private WOFColor getPCValue(String gameData) {
        WOFColor colorPassed = WOFColor.NONE;

        if (gameData.equals("B")) {
            colorPassed = WOFColor.BLUE;
        } else if (gameData.equals("G")) {
            colorPassed = WOFColor.GREEN;
        } else if (gameData.equals("R")) {
            colorPassed = WOFColor.RED;
        } else if (gameData.equals("Y")) {
            colorPassed = WOFColor.YELLOW;
        }

        return colorPassed;
    }

    /**
     * Returns the output power scaled to avoid overshoot
     * @param numColors the number of colors to go over
     */
    private double getOutputPower(double numColors) {
        if (numColors > 32) {
            return 0.15;
        } else {
            return 0.03*Math.log(numColors + 1)+0.05;
        }
    }

    /**
     * Runs color-matching on the currently detected color
     * @return a WOFColor representing the current color
     */
    public WOFColor colorDetect() {
        detectedColor = colorSensor.getColor();

        WOFColor ret = WOFColor.NONE;

        ColorMatchResult match = colorMatch.matchColor(detectedColor);
        if (match == null) {
            ret = WOFColor.NONE;
        } else if (match.color == blueTarget) {
            ret = WOFColor.BLUE;
        } else if (match.color == redTarget) {
            ret = WOFColor.RED;
        } else if (match.color == greenTarget) {
            ret = WOFColor.GREEN;
        } else if (match.color == yellowTarget) {
            ret = WOFColor.YELLOW;
        }

        return ret;
    } 
    public int calculateDesiredRotationsBlue(WOFColor startColor){
        int ret = 0;
        if(startColor == WOFColor.GREEN){
            ret = 3;
        }
        else if(startColor == WOFColor.RED){
            ret = 2;
        }
        else if(startColor == WOFColor.YELLOW){
            ret = 1;
        }
        else if (startColor == WOFColor.BLUE){
            ret = 0;
        }
        return ret;
    }
    public int calculateDesiredRotationsGreen(WOFColor startColor){
        int ret = 0;
        if(startColor == WOFColor.GREEN){
            ret = 0;
        }
        else if(startColor == WOFColor.RED){
            ret = 3;
        }
        else if(startColor == WOFColor.YELLOW){
            ret = 2;
        }
        else if (startColor == WOFColor.BLUE){
            ret = 1;
        }
        return ret;
    }
    public int calculateDesiredRotationsRed(WOFColor startColor){
        int ret = 0;
        if(startColor == WOFColor.GREEN){
            ret = 1;
        }
        else if(startColor == WOFColor.RED){
            ret = 0;
        }
        else if(startColor == WOFColor.YELLOW){
            ret = 3;
        }
        else if (startColor == WOFColor.BLUE){
            ret = 2;
        }
        return ret;
    }
    public int calculateDesiredRotationsYellow(WOFColor startColor){
        int ret = 0;
        if(startColor == WOFColor.GREEN){
            ret = 2;
        }
        else if(startColor == WOFColor.RED){
            ret = 1;
        }
        else if(startColor == WOFColor.YELLOW){
            ret = 0;
        }
        else if (startColor == WOFColor.BLUE){
            ret = 3;
        }
        return ret;
    }
    /**
     * sets the initial color read by the sensor for position control
     * @return the initial Color
     */
    public WOFColor setInitialColor() {
        return colorDetect();
    }

    /**
     * makes the wheel spin 4 times for rotation control of the color wheel
     * @param initialColor the result of the setInitialColor() method (the color we start on)
     * @param numColorChange the number of color changes to spin. 3 times is 32.
     */
    public void rotationControl(WOFColor initialColor, int numColorChange) {
        // read values of color sensor every 20 ms to check if matches original color
        primary.set(this.getOutputPower(numColorChange));
        int colorChangeCount = 0;

        WOFColor previousColor = initialColor;

        while (colorChangeCount < numColorChange) {
            WOFColor currentColor = colorDetect();
            if (!(previousColor == currentColor) && !(currentColor == WOFColor.NONE)) {
                colorChangeCount++;
                previousColor = currentColor;
                
                // set the motor output power, scaled
                primary.set(this.getOutputPower(numColorChange - colorChangeCount));
            }
        }
        
        primary.set(0);
    }

    /**
     * makes the wheel spin to the color from the Smart Dashboard or FMS
     */
    public void spin2Win() {
        WOFColor aimColor = getPCValue(setTargetColor());
        WOFColor detectedColor = colorDetect();
        try {
            switch (aimColor) {
                case RED:
                        detectedColor = colorDetect();
                        rotationControl(detectedColor, calculateDesiredRotationsRed(detectedColor));
                         
                    stop();
                    break;
                case BLUE:
                        detectedColor = colorDetect();
                        rotationControl(detectedColor, calculateDesiredRotationsBlue(detectedColor));
                    stop();
                    break;
                case GREEN:
                        detectedColor = colorDetect();
                        rotationControl(detectedColor, calculateDesiredRotationsGreen(detectedColor));
                    stop();
                    break;
                case YELLOW:
                        detectedColor = colorDetect();
                        rotationControl(detectedColor, calculateDesiredRotationsYellow(detectedColor));
                    stop();
                    break;
                case NONE:
                    System.out.println("NONE");
                    break;
                default:
                    System.out.println("DEFAULT");
                    break;
            }
        } catch (NullPointerException e) {
            System.out.println("NPE caught in WOF!");
        }      
    }

    /**
     * gets rgb values for smart dashboard 
     * @return rgb values from detected color
     */
    public Color getDetectedColor() {
        double red = colorSensor.getColor().red;
        double green = colorSensor.getColor().green;
        double blue = colorSensor.getColor().blue;

        return ColorMatch.makeColor(red, green, blue); // TODO: evaluate use of this method
    }

    /**
     * stops the motor rotation
     */
    public void stop() {
        primary.set(0);
    }

    /**
     * takes a string from the SmartDashboard and sets the target.
     */
    public static String setTargetColor(){
        String targetColor =  SmartDashboard.getString("Color", "B");
        SmartDashboard.putString("Color", targetColor);
        return targetColor;
    }

    /**
     * update values on the SmartDashboard
     */
    public void updateSD() {
        SmartDashboard.putString("Detected color", Robot.wheel.getDetectedColor().toString());
    }
}