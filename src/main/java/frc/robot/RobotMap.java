package frc.robot;

import edu.wpi.first.wpilibj.I2C;

public class RobotMap {
    public static class WheelOfFortune {
        public static final int MOTOR = 21; // TODO: untested
        public static final I2C.Port COLOR_SENSOR = I2C.Port.kOnboard; // TODO: untested
        public static final double[] RED_VALUES = {100, 0, 0};
        public static final double[] GREEN_VALUES = {0, 100, 0};
        public static final double[] BLUE_VALUES = {0, 0, 100};
        public static final double[] YELLOW_VALUES = {50, 50, 50};
        public static final double[] BLANK = {0, 0, 0}; 

        
    }
}