package org.usfirst.frc.team5535.robot;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.cscore.UsbCamera;

import org.usfirst.frc.team5535.robot.Robot.VisionStage;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
@SuppressWarnings({ "deprecation" })
public class Robot extends IterativeRobot {


	String autoSelected;
	Joystick stick = new Joystick(0);
	private DifferentialDrive DemoDrive;
	Joystick xBox;
	WPI_TalonSRX Left1, Left2, Right1, Right2;
	JoystickButton xBoxa, xBoxselect1, xBoxstart1, xBoxx, xBoxy, xBoxb, xBoxlb, xBoxrb, xBoxStick;
	DoubleSolenoid double1, double2, double3;
	Compressor Comp = new Compressor(0);
	Spark Motor1;
	Talon winch;
	NetworkTable table = NetworkTable.getTable("limelight");
	UsbCamera Back_Camera;
	Timer timer;
	double loop, t;
	Command autonomusCommand;
	SendableChooser<String> autoChooser = new SendableChooser<>();
	AHRS navX;
	
	int timeoutMs = 10;

	//creates auto choices
	final String defaultAuto = "Left";
	final String customAuto = "Right";
	final String customAuto1 = "Straight Eight";

	//encoders
	public static final int LENC = 2;
	public static final int RENC = 4;

	WPI_TalonSRX LEncoder = new WPI_TalonSRX(LENC);
	WPI_TalonSRX REncoder = new WPI_TalonSRX(RENC);


	public enum AutoStage{
		
		kStart,
		kDriveForward,
		kDriveForward1,
		kDriveForward2,
		kDriveForward3,
		kTurn,
		kTurn1,
		kTurn2,
		kTurn3,
		kDriveBack,
		kDriveBack1,
		kDriveBack2,
		kArmsDown,
		kArmsUp,
		kLowerFly,
		kRaiseFly,
		kDone
		
		
	}
	

	public enum VisionStage {
		kStart,
		kNoTargetFound,
		kFoundTarget, 
		kCenterOnTarget,
		kDriveForward,
		kDriveForward1,
		kDriveForward2,
		kTurn,
		kTurn1,
		kTurn2,
		kRaise,
		kDropCube,
		kDone
	}



	VisionStage currentStage = VisionStage.kNoTargetFound;
	AutoStage Currentstage = AutoStage.kStart;






	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {

		//navX
		navX = new AHRS(SerialPort.Port.kUSB);

		//auto choices
		autoChooser = new SendableChooser();
		autoChooser.addDefault("Left", defaultAuto);
		autoChooser.addObject("Right", customAuto);
		autoChooser.addObject("Straight Eight", customAuto1);
		SmartDashboard.putData("Auto Choices", autoChooser); //throws data to SmartDashboard*/

		//timer
		timer = new Timer();



		//winch motor
		winch = new Talon(1);

		//cameras
		Back_Camera = CameraServer.getInstance().startAutomaticCapture();

		//xBox controller
		xBox = new Joystick(0);

		//buttons
		xBoxa = new JoystickButton(xBox, 1);
		xBoxb = new JoystickButton(xBox, 2);
		xBoxx = new JoystickButton(xBox, 3);
		xBoxy = new JoystickButton(xBox, 4);
		xBoxlb = new JoystickButton(xBox, 5);
		xBoxrb = new JoystickButton(xBox, 6);
		xBoxselect1 = new JoystickButton(xBox, 7);
		xBoxstart1 = new JoystickButton(xBox, 8);
		xBoxStick = new JoystickButton(xBox, 9);

		//compressor
		Comp.setClosedLoopControl(false);

		//Talons
		Left1 = new WPI_TalonSRX(1);
		Left2 = new WPI_TalonSRX(2);
		Right1 = new WPI_TalonSRX(4); 
		Right2 = new WPI_TalonSRX(3);

		//Climber 
		Motor1 = new Spark(0);

		//talon groups
		SpeedControllerGroup m_left = new SpeedControllerGroup(Left1, Left2);
		SpeedControllerGroup m_right = new SpeedControllerGroup(Right1, Right2);

		//Drivetrain
		DemoDrive = new DifferentialDrive(m_left, m_right);

		//Solenoids
		double1 = new DoubleSolenoid(0,1);
		double2 = new DoubleSolenoid(2,3);
		double3 = new DoubleSolenoid(4,5);



		REncoder.configSelectedFeedbackSensor(com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder, 0, 0);
		REncoder.setSensorPhase(false);
		LEncoder.configSelectedFeedbackSensor(com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder, 0, 0);
		LEncoder.setSensorPhase(false);

	}

	public void calibrateNavX()
	{	
		System.out.println("Calibrating NavX...");


		while(navX.isCalibrating())
		{
			Timer.delay(0.005);
		}
		System.out.println("Calibration done.");
	}


	/**
	 * This function is run once each time the robot enters autonomous mode
	 */
	@Override
	public void autonomousInit() {
		



		//set limelight led/camera

		table.putNumber( "ledMode", 1);
		table.putNumber( "camMode", 0);

		
		currentStage = VisionStage.kStart;

		REncoder.setSelectedSensorPosition(0, 0, 0);
		LEncoder.setSelectedSensorPosition(0, 0, 0);

		//sendable
		System.out.println("Autonomus Init");
		autoSelected = autoChooser.getSelected();
		System.out.println("Auto selected: " + autoSelected);
		
		//loop set
		loop = 2.4;
		
				

		Timer.delay(.2);

		//set limelight led/camera
		table.putNumber( "ledMode", 0);
		table.putNumber( "camMode", 0);

		Comp.setClosedLoopControl(true);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {	
		
		
		
		double targetx = table.getNumber("tx", 0); //Horizontal correction
		double targeta = table.getNumber("ta", 0); //Distance correction
		double targetv = table.getNumber("tv", 0); //Wether there is a target or not
		SmartDashboard.putNumber("Gyro", navX.getYaw());
		
		Left2.configOpenloopRamp(.4, timeoutMs);
		Right1.configOpenloopRamp(.4, timeoutMs);
		
		SmartDashboard.putNumber("Right Sensor position", REncoder.getSelectedSensorPosition(0));
		SmartDashboard.putNumber("Left Sensor position", LEncoder.getSelectedSensorPosition(0));
		String gameData;
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
		
		if(gameData.length() > 0) {
			if(gameData.charAt(1) == 'L') {
				
			
		if (currentStage == VisionStage.kStart) {
		
			DemoDrive.arcadeDrive(1,0);
			
			if (LEncoder.getSelectedSensorPosition(0) > 600) {
				
				currentStage = VisionStage.kTurn;
			} 
			
			
		}
		
		if (currentStage == VisionStage.kTurn) {
			
			DemoDrive.arcadeDrive(0, -1);
			
			if (LEncoder.getSelectedSensorPosition(0) < -1200) {
				
				
				currentStage = VisionStage.kDriveForward1;
				
			}
		}
		
		
		if (currentStage == VisionStage.kDriveForward1) {
			
			DemoDrive.arcadeDrive(1, 0);
			
			if (LEncoder.getSelectedSensorPosition(0) > 900) {
				
				currentStage = VisionStage.kTurn1;
			}
			
			
		}
		
		if (currentStage == VisionStage.kTurn1) {
			
			DemoDrive.arcadeDrive(0, 1);
			
			if(LEncoder.getSelectedSensorPosition(0) > 1600)
				
				currentStage = VisionStage.kNoTargetFound;
			
		}
		
		//outside
				
				
		
		if (currentStage == VisionStage.kNoTargetFound) 
		{	Motor1.set(1);
			Timer.delay(1);
			System.out.println( "No target found");
			

			if (targetv == 1) {
				currentStage = VisionStage.kCenterOnTarget;
			}
		}

		if (currentStage == VisionStage.kCenterOnTarget) {
			Motor1.set(0);
			System.out.println("Centering on target");


			if (targetx > 1) {
				DemoDrive.arcadeDrive(.7, .5);

			}

			if (targetx < -2.5) {
				DemoDrive.arcadeDrive(.7, -.5);
			}

			if(targetx >= -10 && targetx <= 10 && targetv == 1) {

				currentStage = VisionStage.kDriveForward1;
			}

		}

		if (currentStage == VisionStage.kDriveForward1) {
			System.out.println("Driving Forward");


			if (targeta < 10)  
			{  DemoDrive.arcadeDrive(.7, -.01);

			}

			if (targetx < -2.5) {
				DemoDrive.arcadeDrive(.7, -.5);


			}
			if (targetx > 1) {
				DemoDrive.arcadeDrive(.7, .5);
			}

			if (targeta >= 10 && targeta <= 30 && targetx >= -10 && targetx <= 10 && targetv == 1) {

				currentStage = VisionStage.kDropCube;
			}

		}


		if (currentStage == VisionStage.kDropCube) {
			System.out.println("Dropping cube");
			
			REncoder.setSelectedSensorPosition(0, 0, 0);
			LEncoder.setSelectedSensorPosition(0, 0, 0);

			if (targetv == 1 && targeta >= 10)
				double2.set(DoubleSolenoid.Value.kForward);

			currentStage = VisionStage.kDone;
			
			



		}
		if (currentStage == VisionStage.kDone) {
			System.out.println("Done");


			table.putNumber( "camMode", 1);
			table.putNumber( "ledMode", 1);

			
		}
			}
		
		else {
			
			if (currentStage == VisionStage.kStart) {
				
				DemoDrive.arcadeDrive(1,0);
				
				if (LEncoder.getSelectedSensorPosition(0) > 600) {
					
					currentStage = VisionStage.kTurn;
				} 
				
				
			}
			
			if (currentStage == VisionStage.kTurn) {
				
				DemoDrive.arcadeDrive(0, 1);
				
				if (LEncoder.getSelectedSensorPosition(0) > 1200) {
					
					
					currentStage = VisionStage.kDriveForward1;
					
				}
			}
			
			
			if (currentStage == VisionStage.kDriveForward1) {
				
				DemoDrive.arcadeDrive(1, 0);
				
				if (LEncoder.getSelectedSensorPosition(0) > 1900) {
					
					currentStage = VisionStage.kTurn1;
				}
				
				
			}
			
			if (currentStage == VisionStage.kTurn1) {
				
				DemoDrive.arcadeDrive(0, -1);
				
				if(LEncoder.getSelectedSensorPosition(0) < 1200)
					
					currentStage = VisionStage.kNoTargetFound;
				
			}
			
			//outside
					
					
			
			if (currentStage == VisionStage.kNoTargetFound) 
			{	Motor1.set(1);
				Timer.delay(1);
				System.out.println( "No target found");
				

				if (targetv == 1) {
					currentStage = VisionStage.kCenterOnTarget;
				}
			}

			if (currentStage == VisionStage.kCenterOnTarget) {
				Motor1.set(0);
				System.out.println("Centering on target");


				if (targetx > 1) {
					DemoDrive.arcadeDrive(.7, .5);

				}

				if (targetx < -2.5) {
					DemoDrive.arcadeDrive(.7, -.5);
				}

				if(targetx >= -10 && targetx <= 10 && targetv == 1) {

					currentStage = VisionStage.kDriveForward1;
				}

			}

			if (currentStage == VisionStage.kDriveForward1) {
				System.out.println("Driving Forward");


				if (targeta < 10)  
				{  DemoDrive.arcadeDrive(.7, -.01);

				}

				if (targetx < -2.5) {
					DemoDrive.arcadeDrive(.7, -.5);


				}
				if (targetx > 1) {
					DemoDrive.arcadeDrive(.7, .5);
				}

				if (targeta >= 10 && targeta <= 30 && targetx >= -10 && targetx <= 10 && targetv == 1) {

					currentStage = VisionStage.kDropCube;
				}

			}


			if (currentStage == VisionStage.kDropCube) {
				System.out.println("Dropping cube");
				
				REncoder.setSelectedSensorPosition(0, 0, 0);
				LEncoder.setSelectedSensorPosition(0, 0, 0);

				if (targetv == 1 && targeta >= 10)
					double2.set(DoubleSolenoid.Value.kForward);

				currentStage = VisionStage.kDone;
				
				



			}
			if (currentStage == VisionStage.kDone) {
				System.out.println("Done");


				table.putNumber( "camMode", 1);
				table.putNumber( "ledMode", 1);

				
			}
		
			
		}
		}
			
		}
		
		//outside

		
	




	/**
	 * This function is called once each time the robot enters tele-operated
	 * mode
	 */
	@Override
	public void teleopInit() {
		


 
		currentStage = VisionStage.kDone;
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		SmartDashboard.putNumber("Gyro", navX.getYaw());
		
		Left2.configOpenloopRamp(0, timeoutMs);
		Right1.configOpenloopRamp(0, timeoutMs);

		//limelight camera/led set
		table.putNumber( "ledMode", 1);
		table.putNumber( "camMode", 1);

		//set stage
		currentStage = VisionStage.kDone;


		//Drivesticks
		DemoDrive.tankDrive((xBox.getRawAxis(1)* -.8), (xBox.getRawAxis(5)* -.85));

		//Buttons

		//compressor on						
		if (xBoxstart1.get()) {
			Comp.setClosedLoopControl(true);


		}
		//compressor off		
		else if (xBoxselect1.get()) {
			Comp.setClosedLoopControl(false);

		}
		//Arms up
		else if (xBoxa.get()) {

			double1.set(DoubleSolenoid.Value.kForward);

		}
		//Arms down
		else if (xBoxb.get()) {

			double1.set(DoubleSolenoid.Value.kReverse);
		}
		//Winch windup
		else if (xBoxx.get()) {

			winch.set(1);
		}
		//unused
		else if (xBoxy.get()) {

			//climber in
			double3.set(DoubleSolenoid.Value.kForward);

		}
		//Flysection down
		else if (xBox.getRawAxis(2) == 1.0 || xBox.getRawAxis(2) > 0) {
			Motor1.set(-.8);
		}
		//Flysection up
		else if (xBox.getRawAxis(3) == 1.0 || xBox.getRawAxis(2) > 0) {
			Motor1.set(1);

		}
		else if (xBoxlb.get()) {
			//Arms Close
			double2.set(DoubleSolenoid.Value.kForward);

		}

		else if (xBoxrb.get()) {
			//Arms Open
			double2.set(DoubleSolenoid.Value.kReverse);

		}

		else if (xBoxStick.get()) {

			//Climber out

			double3.set(DoubleSolenoid.Value.kReverse);



		}


		else {
			//off until used
			double1.set(DoubleSolenoid.Value.kOff);
			double2.set(DoubleSolenoid.Value.kOff);
			double3.set(DoubleSolenoid.Value.kOff);
			Motor1.set(0);
			winch.set(0);
		}


	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {	}
}
