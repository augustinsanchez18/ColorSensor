package frc.robot;

import edu.wpi.first.wpilibj.I2C;

public class RobotMap {
    public static class WheelOfFortune {
        public static final int MOTOR = 21; // TODO: untested
        public static final I2C.Port COLOR_SENSOR = I2C.Port.kMXP; // TODO: untested
        public static final double[] RED_VALUES = {0.36,0.415,0.216};
        public static final double[] GREEN_VALUES = {0.22,0.501,0.275};
        public static final double[] BLUE_VALUES = {0.191,0.451,0.349};
        public static final double[] YELLOW_VALUES = {0.299,0.536,0.164};
        public static final double[] BLANK = {0,0,0};

    }
}