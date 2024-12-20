package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class LeoTwo extends Robot {
    double left;
    double right;
    double max;

    double targetPosition;

    private final ElapsedTime armTime = new ElapsedTime();

    DcMotorEx armMotor          = null;
    LionsDcMotorEx armMotorEx   = null;
    DcMotor   extensionMotor    = null;
    DcMotor   leftDrive         = null;
    DcMotor   rightDrive        = null;
    CRServo   wrist             = null;
    Servo     claw              = null;

    public LeoTwo(HardwareMap hm, Telemetry tm) {
        super(hm, tm);
    }
    public void initRobot() {
        armMotor = this.hardwareMap.get(DcMotorEx.class, "left_arm");
        armMotorEx = new LionsDcMotorEx(armMotor);
        extensionMotor = this.hardwareMap.get(DcMotor.class, "extender");
        leftDrive = this.hardwareMap.get(DcMotor.class, "left_front_drive");
        rightDrive = this.hardwareMap.get(DcMotor.class, "right_front_drive");
        wrist = this.hardwareMap.get(CRServo.class, "wrist");
        claw = this.hardwareMap.get(Servo.class, "claw");

        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);

        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armMotorEx.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armMotorEx.setCurrentAlert(9.2, CurrentUnit.AMPS);

        armMotorEx.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotorEx.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        wrist.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void move(double x_axis, double y_axis, double tilt) {
        left  = y_axis + tilt;
        right = y_axis - tilt;

        /* Normalize the values so neither exceed +/- 1.0 */
        max = Math.max(Math.abs(left), Math.abs(right));
        if (max > 1.0) {
            left /= max;
            right /= max;
        }

        leftDrive.setPower(left);
        rightDrive.setPower(right);
    }

    public void extendArm(int inOrOut) {
        if (armMotorEx.getCurrentPosition() < 300 && armMotorEx.getCurrentPosition() > -600) {
            extensionMotor.setPower(inOrOut);
            armTime.reset();
        } else if (armTime.seconds() < 1.5) {
            // Retract the arm to stay legal
            extensionMotor.setPower(-1);
        } else {
            extensionMotor.setPower(0);
        }
    }

    public void moveArm(double direction) {
        if (armMotorEx.isOverCurrent()){
            this.telemetry.addLine("MOTOR EXCEEDED CURRENT LIMIT!");
            armMotorEx.setPower(0);
            direction = 0;
        }
//        if (armMotorEx.getCurrentPosition() < -600){
//            this.telemetry.addLine("ARM TRYING TO BE ILLEGAL");
//            targetPosition = -590;
//            direction = 0;
//        }

        if (direction != 0) {
            armMotorEx.setVelocity(1000 * (direction));
            targetPosition = armMotorEx.getCurrentPosition();
        } else {
            armMotorEx.PID(targetPosition, 0.0065, 0.001, 0.00015);
        }
        this.telemetry.addData("Arm motor turning to: ", targetPosition);
        this.telemetry.addData("Arm motor is currently: ", armMotorEx.getCurrentPosition());
    }

    public void moveClaw(int direction) {
        claw.setPosition(direction);
    }

    public void moveWrist(int direction) {
        wrist.setPower(direction);
    }

    public int getArmPosition() {
        return armMotorEx.getCurrentPosition();
    }

    public void setArmPositionZero() {
        armMotorEx.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        targetPosition = 0;
    }

}