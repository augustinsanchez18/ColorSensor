package frc.robot;

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
 * @author dri, ai
 */
public class WheelOfFortune {
    private static WheelOfFortune instance;

    private enum WOFColor {
        BLUE, GREEN, RED, YELLOW, WHAT;
    };

    private WPI_TalonSRX primary;
    private String gameData;
    private ColorSensorV3 colorSensor;
    private double rotations = 0;
    private ColorMatch colorMatch;
    private Color detectedColor;
  
    private final Color blueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
    private final Color greenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
    private final Color redTarget = ColorMatch.makeColor(0.487, 0.360, 0.152);
    private final Color yellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

    private WheelOfFortune() {
        primary = new WPI_TalonSRX(RobotMap.WheelOfFortune.MOTOR);
        gameData = DriverStation.getInstance().getGameSpecificMessage();
        colorSensor = new ColorSensorV3(RobotMap.WheelOfFortune.COLOR_SENSOR);
        colorMatch = new ColorMatch();

        colorMatch.addColorMatch(blueTarget);
        colorMatch.addColorMatch(greenTarget);
        colorMatch.addColorMatch(redTarget);
        colorMatch.addColorMatch(yellowTarget);
    }

    public void setSpeed(double speed) {
        primary.set(speed);
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

    /**
     * Runs color-matching on the currently detected color
     * @return a string representing the current color
     */
    private WOFColor colorDetect() {
        detectedColor = colorSensor.getColor();

        WOFColor ret = WOFColor.WHAT;

        try {
            ColorMatchResult match = colorMatch.matchColor(detectedColor);

            if (match.color == blueTarget) {
                ret = WOFColor.BLUE;
            } else if (match.color == redTarget) {
                ret = WOFColor.RED;
            } else if (match.color == greenTarget) {
                ret = WOFColor.GREEN;
            } else if (match.color == yellowTarget) {
                ret = WOFColor.YELLOW;
            }          
        } catch (NullPointerException e) {
            System.out.println("NPE in WheelOfFortune!");
        }

        return ret;
    } 

    /**
     * takes game data sent by the FMS and accordingly returns the Color which we need to target
     * @param gameData the data sent by the FMS
     * @return Color (in RGB) for the sensor to target
     */
    private Color getPCValue(char gameData) {
        Color targetColor = ColorMatch.makeColor(RobotMap.WheelOfFortune.BLANK[0],
                                                 RobotMap.WheelOfFortune.BLANK[1],
                                                 RobotMap.WheelOfFortune.BLANK[2]);
        if (gameData == 'B') {
            targetColor = ColorMatch.makeColor(RobotMap.WheelOfFortune.BLUE_VALUES[0],
                                               RobotMap.WheelOfFortune.BLUE_VALUES[1],
                                               RobotMap.WheelOfFortune.BLUE_VALUES[2]);
        } else if (gameData == 'G') {
            targetColor = ColorMatch.makeColor(RobotMap.WheelOfFortune.GREEN_VALUES[0],
                                               RobotMap.WheelOfFortune.GREEN_VALUES[1],
                                               RobotMap.WheelOfFortune.GREEN_VALUES[2]);
        } else if (gameData == 'R') {
            targetColor = ColorMatch.makeColor(RobotMap.WheelOfFortune.RED_VALUES[0], 
                                               RobotMap.WheelOfFortune.RED_VALUES[1],
                                               RobotMap.WheelOfFortune.RED_VALUES[2]);
        } else if (gameData == 'Y') {
            targetColor = ColorMatch.makeColor(RobotMap.WheelOfFortune.YELLOW_VALUES[0], 
                                               RobotMap.WheelOfFortune.YELLOW_VALUES[1],
                                               RobotMap.WheelOfFortune.YELLOW_VALUES[2]);
        }

        return targetColor;
    }

    /**
     * sets the initial color read by the sensor for position control
     * @return the initial Color
     */
    public Color setInitialColor() {
        return ColorMatch.makeColor(colorSensor.getColor().red,
                                    colorSensor.getColor().green,
                                    colorSensor.getColor().blue);
    }

    /**
     * makes the wheel spin 4 times for rotation control of the color wheel
     * @param initialColor the result of the setInitialColor() method (the color we start on)
     */
    public void rotationControl(Color initialColor) {
        // read values of color sensor every 20 ms to check if matches original color
        double red = colorSensor.getColor().red;
        double green = colorSensor.getColor().green;
        double blue = colorSensor.getColor().blue;

        if (rotations <= 4) { // only run motor if we're under 4 counts of the WoF
            primary.set(0.1);

            //Checks if the current reading equals the initial reading, and if so, increases the
            //rotation count by 0.5 since 2 instances of each color per rotation
            if (red == initialColor.red &&
                green == initialColor.green &&
                blue == initialColor.blue) {

                rotations += 0.5;
            }
        } else {
            primary.set(0); //stop if we've met required rotation number
        }
    }

    /**
     * called once when we start rotation control for the first time, so we know what color to
     * look for to increase rotation count
     * @return the first detected color from the color sensor
     */
    public Color getDetectedColor() {
        double red = colorSensor.getColor().red;
        double green = colorSensor.getColor().green;
        double blue = colorSensor.getColor().blue;

        return ColorMatch.makeColor(0.3, 0.4, 0.4); // TODO: evaluate use of this method
    }
    
    /**
     * controls the rotation of the field's WoF in order to set it to a specific color, as received
     * by the FMS
     */
    public void positionControl() {
        // TODO: account for offset of the color wheel based on mechanical design
        try {
            ColorMatchResult result = colorMatch.matchColor(getDetectedColor());
            if (!result.color.equals(getPCValue(gameData.charAt(0)))) { // TODO: handle exceptions
                //primary.set(0.1); // TODO: change motor values
                System.out.println("not null");
            } else {
                primary.set(0); // stop the motor when the color is reached
            }
        } catch (NullPointerException e) {
            System.out.println("null");
        }
    }

    /**
     * stops the motor rotation
     */
    public void stop() {
        primary.set(0);
    }

    /**
     * update values on the SmartDashboard
     */
    public void updateSD() {
        SmartDashboard.putString("Detected color", Robot.wheel.getDetectedColor().toString());
    }
}
