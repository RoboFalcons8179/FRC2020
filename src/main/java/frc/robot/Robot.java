/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.shuffleboard.*;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSink;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
// import edu.wpi.first.wpilibj.GenericHID.Hand;
// import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
// import edu.wpi.first.wpilibj.smartdashboard.*;
// import edu.wpi.first.wpilibj.PowerDistributionPanel;
// import edu.wpi.first.wpilibj.SpeedControllerGroup;
// import com.ctre.phoenix.motorcontrol.can.*;
// import edu.wpi.first.wpilibj.PWMVictorSPX;
// import edu.wpi.cscore.VideoMode.PixelFormat;
// import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj.Sendable;

import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
//import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
// import com.ctre.phoenix.motorcontrol.Faults;
// import com.ctre.phoenix.motorcontrol.InvertType;
// import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
// import com.ctre.phoenix.motorcontrol.SensorTerm;
// import com.ctre.phoenix.motorcontrol.FollowerType;
// import com.ctre.phoenix.motorcontrol.DemandType;
// import com.ctre.phoenix.motorcontrol.StatusFrame;
// import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
// import com.ctre.phoenix.motorcontrol.can.*;
// import edu.wpi.first.wpilibj.DutyCycleEncoder;
// import com.ctre.phoenix.motorcontrol.IMotorController;
// import com.ctre.phoenix.motorcontrol.can.BaseMotorController;
// import com.ctre.phoenix.motorcontrol.can.VictorSPXPIDSetConfiguration;

public class Robot extends TimedRobot {

  // BEGIN Declare and Attach CAN IDs to devices
  // PDP
  //private final PowerDistributionPanel PDP = new PowerDistributionPanel(0);
  
  // Drive Motors
  private final WPI_VictorSPX driveA = new WPI_VictorSPX(2);
  private final WPI_VictorSPX driveB = new WPI_VictorSPX(4);
  private final WPI_VictorSPX driveC = new WPI_VictorSPX(13);
  private final WPI_VictorSPX driveD = new WPI_VictorSPX(14);
  
  // Belt Motors
  
  private final WPI_VictorSPX conv = new WPI_VictorSPX(3);
  private final WPI_TalonFX bbar = new WPI_TalonFX(11);

  // Lift Motors

  private final WPI_TalonFX lifta = new WPI_TalonFX(5);
  private final WPI_TalonFX liftb = new WPI_TalonFX(10);

  // Shooter Talons

  private final WPI_TalonSRX shoota = new WPI_TalonSRX(7);
  private final WPI_TalonSRX shootb = new WPI_TalonSRX(8);
  // Tilt Not here yet
  private final WPI_TalonFX tilt = new WPI_TalonFX(6);

  // END Attach CAN IDs to devices

  
  // Drive Functions
  private final DifferentialDrive vroom = new DifferentialDrive(driveA, driveB);

  // Controllers
  private final Joystick logA = new Joystick(0);
  private final Joystick xbox = new Joystick(1);
  private final Joystick Logi = new Joystick(2);


  // Other global variables

  double lift_backup = 0;
  double tilt_backup = 0;
  double bb = 0;
  double cc = 0;
  int senseX = 0;

  // Lift Limits - Encoder units
  int max_max  = 360000;
  int max_stop = 355000;
  int max_slow = 270000;
  int min_slow =  70000;
  int min_stop =  50000;
  int min_min  =   3000;
  double lift_speed = 0;

  // Tilt Stuff
  int kTimeoutMs = 30; 
  int kSlotIdx = 0;
  int kPIDLoopIdx = 0;
  int maxTilt = 1100;

  double startTime;

  double currentDistance;

  double leftShoot;
  double rightShoot;

  UsbCamera camera0;
  UsbCamera camera1;
  VideoSink server0;
  VideoSink server1;
  NetworkTableEntry camera00_table;

  private static final double kValueToInches = 0.125/2.54;
  private final AnalogInput m_ultrasonic = new AnalogInput(0);

  // GYRO
  Gyro gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);


  double safety = 1; //0.g
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {


    // Talon Encoder Stuff
    TalonFXConfiguration configs = new TalonFXConfiguration();
    configs.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;

    lifta.configAllSettings(configs);
    liftb.configAllSettings(configs);

    lifta.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 20);
    lifta.setNeutralMode(NeutralMode.Brake);

    liftb.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 20);
    liftb.setNeutralMode(NeutralMode.Brake);

    liftb.setSensorPhase(true);

    lifta.configForwardSoftLimitEnable(true);
    lifta.configForwardSoftLimitThreshold(max_max);

    lifta.configReverseSoftLimitEnable(true);
    lifta.configReverseSoftLimitThreshold(min_min);
    
    liftb.configForwardSoftLimitEnable(true);
    liftb.configForwardSoftLimitThreshold(-1 * min_min);

    liftb.configReverseSoftLimitEnable(true);
    liftb.configReverseSoftLimitThreshold(-1*max_max);


    // Set up Voltage Compesation

    shoota.configVoltageCompSaturation(11);
    shoota.enableVoltageCompensation(true);
    shoota.configNeutralDeadband(0.04);
    shoota.setNeutralMode(NeutralMode.Coast);
    shootb.setSensorPhase(true);

    
    shootb.configVoltageCompSaturation(11);
    shootb.enableVoltageCompensation(true);
    shootb.configNeutralDeadband(0.04);
    shootb.setNeutralMode(NeutralMode.Coast);
    shootb.setSensorPhase(false);
    shootb.setInverted(true);

    // Set up Tilt PID Stuff
    tilt.configFactoryDefault();
    // tilt.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor.toFeedbackDevice(),kPIDLoopIdx,kTimeoutMs);
    // tilt.configNeutralDeadband(0.001);
    // tilt.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10,kTimeoutMs);
    // tilt.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, kTimeoutMs);
    
    // tilt.configNominalOutputForward(0);
    // tilt.configNominalOutputReverse(0);
    // tilt.configPeakOutputForward(1,kTimeoutMs);
    // tilt.configPeakOutputReverse(-1,kTimeoutMs);

    // tilt.config_kF(kSlotIdx, 18.6,kTimeoutMs);
    // tilt.config_kP(kSlotIdx, 1,kTimeoutMs);
    // tilt.config_kI(kSlotIdx, 0.0,kTimeoutMs);
    // tilt.config_kD(kSlotIdx, 0.0,kTimeoutMs);

    // tilt.configMotionCruiseVelocity(1100, kTimeoutMs);
    // tilt.configMotionAcceleration(0, kTimeoutMs);
    //tilt.configMotionSCurveStrength(3);

    

    // Set Controller Channels
    logA.setZChannel(4);
    logA.setThrottleChannel(3);
    logA.setTwistChannel(2);

    xbox.setZChannel(5);
    xbox.setThrottleChannel(2);
    xbox.setTwistChannel(3);

    Logi.setThrottleChannel(3);

    lifta.setInverted(false);
    lifta.setSensorPhase(false);

    liftb.setSensorPhase(true);


    //Cameras
    camera0 = CameraServer.getInstance().startAutomaticCapture();
    camera0.setResolution(160, 90);
    camera0.setFPS(3);
    camera0.setPixelFormat(PixelFormat.kMJPEG);

    camera1 = CameraServer.getInstance().startAutomaticCapture();
    camera1.setResolution(320, 180);
    camera1.setFPS(10);
    camera0.setPixelFormat(PixelFormat.kMJPEG);

    // camera1.setBrightness(1);
    // camera1.setExposureAuto();

    camera1.setExposureAuto();
    camera1.setBrightness(50);
  
    // Tilt 
    tilt.setNeutralMode(NeutralMode.Brake);
    tilt.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);
    tilt.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);

    // Gryo
    gyro.calibrate();
    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */

  boolean bb_on;
  boolean cc_on;

  @Override
  public void robotPeriodic() {

    
    // Smart Dashboard Stuff Here
    SmartDashboard.putNumber("LeftShoot",leftShoot);
    SmartDashboard.putNumber("RightShoot",rightShoot);
    //SmartDashboard.putNumber("PDP Voltage", PDP.getVoltage());
    SmartDashboard.putNumber("Beater Bar",bb);
    SmartDashboard.putNumber("Conveyor",cc);
    SmartDashboard.putNumber("US Distance", currentDistance/12);
    SmartDashboard.putNumber("Gyro", gyro.getAngle());
    SmartDashboard.putNumber("Falcon Encoder 5 pos", lifta.getSelectedSensorPosition());
    SmartDashboard.putNumber("Falcon Encoder 10 pos", liftb.getSelectedSensorPosition());

    SmartDashboard.putNumber("Tilt Raw Output",tilt.getSelectedSensorPosition());
    SmartDashboard.putNumber("Tilt Degrees", (tilt.getSelectedSensorPosition() * 0.001779) + 4);

    currentDistance = m_ultrasonic.getValue() * kValueToInches;

    

// Pull this
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {

    startTime = Timer.getFPGATimestamp();
    tiltHome();
    setTilt(16.0);

  }

  /**
   * This function is called periodically during autonomous.
   */

  @Override
  public void autonomousPeriodic() {
    
    // Basic Timer
   double time = Timer.getFPGATimestamp();
  
    if (time-startTime < 3) {
      shoota.set(ControlMode.PercentOutput, .60);
      shootb.set(ControlMode.PercentOutput, .60);
    }
    else if (3 < time-startTime && time-startTime < 3.5) {
      shoota.set(ControlMode.PercentOutput, .60);
      shootb.set(ControlMode.PercentOutput, .60);
      bbar.set(ControlMode.PercentOutput,0.5);
      conv.set(ControlMode.PercentOutput,.75);
    
    }
    else if (3.5 < time-startTime && time-startTime < 6) {
      bbar.set(ControlMode.PercentOutput,0);
      conv.set(ControlMode.PercentOutput,0);
      tiltHome();
      setTilt(16.0);
    }

    else if (6 < time-startTime && time-startTime < 12) {
      shoota.set(ControlMode.PercentOutput, .60);
      shootb.set(ControlMode.PercentOutput, .60);
      bbar.set(ControlMode.PercentOutput,0.5);
      conv.set(ControlMode.PercentOutput,.75);
    
    }
   else if (12 < time-startTime && time-startTime < 14){
     driveA.set(0.4);
     driveB.set(-0.4);
     driveC.follow(driveA);
     driveD.follow(driveB);
     shoota.set(ControlMode.PercentOutput, 0);
     shootb.set(ControlMode.PercentOutput, -1 * 0);
     bbar.set(ControlMode.PercentOutput,-0);
     conv.set(ControlMode.PercentOutput,0);
  }
   else {
    driveA.set(0);
    driveB.set(0);
    driveC.follow(driveA);
    driveD.follow(driveB);
   }

  }

  /**
   * This function is called periodically during operator control.
   */
  
  @Override
  public void teleopInit() {
    
    //Zero the Lifters
    lifta.setSelectedSensorPosition(0);
    liftb.setSelectedSensorPosition(0);
    
    // COMMENT THIS OUT IF NEED TO BE SAFE
    // Home the Tilt 
    // tiltHome();
    
  }

  
   @Override
  public void teleopPeriodic() {

    // Drive
    
    double zoom = logA.getY() * .80 * safety;

    if (logA.getTwist() > 0.5) {
      zoom = -1 * logA.getTwist();
    }
    else if (logA.getThrottle() > 0.5) {
      zoom = logA.getThrottle();
    }



    // Twist but with buttons
    if(xbox.getRawButton(6) || logA.getRawButton(6)) { 
      vroom.arcadeDrive(0.5, -0.8);
      driveC.follow(driveA);
      driveD.follow(driveB);
    }
    else if (xbox.getRawButton(5) || logA.getRawButton(5)) {
      vroom.arcadeDrive(-0.5, 0.8);
      driveC.follow(driveA);
      driveD.follow(driveB);
    } 
    else {
      vroom.arcadeDrive(zoom, -1* logA.getZ() * safety);
      driveC.follow(driveA);
      driveD.follow(driveB);
    }


    // Conveyor
 

    final double bo = 0.50;
    final double co = 0.65; //65
    
    if (logA.getRawButtonPressed(2)) {
      if (bb == 0) {
        bb = bo + 0.01;
      }
      else {
        bb = 0;
      }
    }
    else {
      if (bb != bo + 0.01)
        bb = 0;
    }
    
    cc = 0;
    
    
    if (logA.getRawButton(1) || xbox.getRawButton(1)) {
       bb = bo;
       cc = co;
    }
    if (logA.getRawButton(4) || xbox.getRawButton(4)) {
      cc = co;
    }
    
    if (xbox.getRawButton(2)) {
      bb = bo;
    }
    if (xbox.getRawButton(3)) {
      cc = -0.75 * co;
    }


    if (xbox.getY() > 0.4 || xbox.getY() < -0.4) {
        bb =  xbox.getY();
    }
    if (xbox.getZ() > 0.4 || xbox.getZ() < -0.4) {
        cc = -1 * xbox.getZ();
    }
    bbar.set(ControlMode.PercentOutput,bb);
    conv.set(ControlMode.PercentOutput,cc);



    // Lifter
    senseX = lifta.getSelectedSensorPosition(0);
    if (Logi.getRawButton(12)) {
      lift_speed = Logi.getTwist();
      }
      else {
        if (Logi.getRawButtonPressed(7)) {
          lift_speed = 0.11;
        }
        
        else if (Logi.getRawButtonPressed(8)) {
          lift_speed = -0.11;
        }

        if (lift_speed > 0.1) {
          if (senseX > (max_stop))
            lift_speed = 0;
          else if (senseX > (max_slow))
            lift_speed = .35;
          else
            lift_speed = .85;
        }
        else if (lift_speed < -0.1) {
          if (senseX < (min_stop))
            lift_speed = 0;
          else if (senseX < (min_slow))
            lift_speed = -.5;
          else
            lift_speed = -.95;
        }
      }

      if(Logi.getRawButton(11)) {
        lift_speed = 0;
      }



    lifta.set(ControlMode.PercentOutput, lift_speed);
    liftb.set(ControlMode.PercentOutput, -1 * lift_speed);

    // lifter backup

  //  lift_backup = -1 * xbox.getY();

  //  lifta.set(ControlMode.PercentOutput,lift_backup);

  //  liftb.set(ControlMode.PercentOutput, -1 * lift_backup);




    // Shooter

    double shootout = (-1*Logi.getThrottle() + 1) / 2;
    
    shootout = remapThrottle(shootout);

    leftShoot = shootout;
    rightShoot = shootout;


    // Calculating Spin
    calcShoot(shootout, xbox.getThrottle(),xbox.getTwist());

    // Long Shot
    if (logA.getRawButton(8)) {
      leftShoot = 0.56;
      rightShoot = 0.56;

    }

    // Shorter Shot
    if (logA.getRawButton(7)) {
      leftShoot = 0.475;
      rightShoot = 0.475;

    }
    // Auton Shot
    if (logA.getRawButton(3)) {
      leftShoot = 0.60;
      rightShoot = 0.60;

    }

    shoota.set(leftShoot);
    shootb.set(rightShoot);

    // Set Tilts for canned shots
    if (logA.getRawButtonPressed(8)) {
      tiltHome();
      setTilt(22);

    }
    if (logA.getRawButtonPressed(7)) {
      tiltHome();
      setTilt(22);
    }
    if(logA.getRawButtonPressed(3)) {
      tiltHome();
      setTilt(16);
    }

    // shoota.set(ControlMode.PercentOutput, xbox.getTwist());
    // shootb.set(ControlMode.PercentOutput, -1*xbox.getThrottle());

    // shootb.follow(shoota);
    // shootb.setInverted(true);

    // Tilt
    

    // tilt backup
    tilt_backup = xbox.getZ();
    int xpov = xbox.getPOV();
    int lpov = logA.getPOV();
    
    if (logA.getRawButton(8)) {
        setTilt(13.0);
    }
    
    if ((xpov == 315 || xpov == 0 || xpov == 45 || xpov ==360)||(lpov == 315 || lpov == 0 || lpov == 45 || lpov ==360)) {
        tilt.set(ControlMode.PercentOutput,0.1);
    } 
    else if ((xpov == 180 || xpov == 135 || xpov == 225)||(lpov == 180 || lpov == 135 || lpov == 225)) {
        tilt.set(ControlMode.PercentOutput,-0.07);
    }
    else {
      tilt.set(ControlMode.PercentOutput,0);
    }

    // Zero Gyro
    if(xbox.getRawButtonPressed(7)) {
      gyro.reset();
    }

  }

  /**
   * This function is called periodically during test mode.
   */
  
  @Override
  public void testInit() {
    tiltHome();
    setTilt(20);
    setTilt(11);
  }
  
  @Override
  public void testPeriodic() {
  }


  // Auto Tilt Control

  public void tiltHome() {
      
    tilt.set(ControlMode.PercentOutput, -0.15);
    double starttime = Timer.getFPGATimestamp();

    while(true) {
      double time = Timer.getFPGATimestamp();
      if (time - starttime > 1) {
        tilt.set(ControlMode.PercentOutput,0);
        tilt.setSelectedSensorPosition(0);
        return;
      }
    }
  }

  public void setTilt (double target_d) {

    double target = (target_d - 3)/ 0.001779711;
    double current = tilt.getSelectedSensorPosition();

    while(true) {
      if(current < target - 2000){
        tilt.set(ControlMode.PercentOutput,0.1);
      }
      else if(target + 5000 < current) {
        tilt.set(ControlMode.PercentOutput, -0.07);
      }
      else {
        break;
      }

      current = tilt.getSelectedSensorPosition();
    }
  }

  public void setTiltMagic (double target_d) {
    double target = (target_d - 2)/ 0.001779711;
    tilt.set(ControlMode.MotionMagic, target);
    System.out.println(tilt.getSelectedSensorPosition());
    System.out.println(tilt.getSelectedSensorPosition() * 0.001779711 + 2);
    

  }


  // Used for shooting Spin
  double bA;
  double bB;
  double bC;
  double pDrop = 0.3;

  private void calcShoot(double shootout, double left, double right) {

    double minShoot = 0.20;
    double deadBand = 0.30;

    if(shootout < minShoot || (left <  deadBand && right < deadBand)) {
      return;
    }

    mapBands(shootout,minShoot);



    // Bands: 0-30,30-60 ,60-90, 90-100
      if (.30 <= left && left < 0.4)
        leftShoot = bA;
      else if (.40 <= left && left < 0.9)
        leftShoot = bB;
      else if (.90 <= left && left <= 1) {
        leftShoot= bC;
      }

      if (.30 <= right && right < 0.4)
        rightShoot = bA;
      else if (.40 <= right && right < 0.9)
        rightShoot = bB;
      else if (.90 <= right && right <= 1) {
        rightShoot= bC;
      }
      

  }

  private void mapBands(double max, double min) {
      bA = max;
      bB = max - 0.1;
      bC = max - 0.2;

      if (max - pDrop < min) {
        bB = max - (max - min) / 2;
        bC = min;
      }

      return;

  }

  private double remapThrottle(double shootout) {

    if (shootout < 0.1) {
      return 0;
    }
    else if (shootout < 0.95) {
      return (0.3 + ((0.7-0.3)/0.85 * (shootout - 0.1)));
    }
    else {
      return (1);
    }

  }
}
