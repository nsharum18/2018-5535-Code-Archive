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
		kDropCube,
		kDone


	}






	AutoStage Currentstage = AutoStage.kStart;






	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {

		//navX
		navX = new AHRS(SerialPort.Port.kUSB);


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
		//REncoder.setSensorPhase(false);
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


		Currentstage = AutoStage.kStart;


		REncoder.setSelectedSensorPosition(0, 0, 0);
		LEncoder.setSelectedSensorPosition(0, 0, 0);
		

		Timer.delay(.2);

		//set limelight led/camera
		table.putNumber( "ledMode", 1);
		table.putNumber( "camMode", 1);

		Comp.setClosedLoopControl(true);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {	



		SmartDashboard.putNumber("Gyro", navX.getYaw());

		Left2.configOpenloopRamp(.4, timeoutMs);
		Right1.configOpenloopRamp(.4, timeoutMs);

		SmartDashboard.putNumber("Right Sensor position", REncoder.getSelectedSensorPosition(0));
		SmartDashboard.putNumber("Left Sensor position", LEncoder.getSelectedSensorPosition(0));
		
		String gameData;
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
/*--------------------------------------------------------------------------------------------------------------*/

		if(gameData.length() > 0) {
			if(gameData.charAt(1) == 'L') {
				
				if(Currentstage == AutoStage.kStart) {

					DemoDrive.arcadeDrive(1, 0);

					if (navX.getYaw() > 2) {

						DemoDrive.arcadeDrive(1, -.2);
					}

					if (navX.getYaw() < -2) {

						DemoDrive.arcadeDrive(1, .2);

					}

					if(LEncoder.getSelectedSensorPosition(0) > 10000) {

						Currentstage = AutoStage.kTurn;


					}
				}

				if(Currentstage == AutoStage.kTurn) {

					DemoDrive.arcadeDrive(0, 1);

					if(navX.getYaw() > 3) {

						Currentstage = AutoStage.kDriveForward;


					}

				}

				if (Currentstage == AutoStage.kDriveForward) {

					DemoDrive.arcadeDrive(.8, 0);

					if (LEncoder.getSelectedSensorPosition(0) > 10500) {

						Currentstage = AutoStage.kArmsUp;

					}

				}

				if (Currentstage == AutoStage.kArmsUp) {

					Motor1.set(1);
					Timer.delay(3.5);

					Currentstage = AutoStage.kDriveForward1;

				}

				if (Currentstage == AutoStage.kDriveForward1) {
					Motor1.set(0);
					DemoDrive.arcadeDrive(.5, 0);

					if (LEncoder.getSelectedSensorPosition(0) > 15000) {

						Currentstage = AutoStage.kDropCube;
					}
				}

				if (Currentstage == AutoStage.kDropCube) {

					double2.set(DoubleSolenoid.Value.kForward);

					Timer.delay(.1);
					Currentstage = AutoStage.kDriveBack;

				}

				if (Currentstage == AutoStage.kDriveBack) {

					DemoDrive.arcadeDrive(-.5, 0);

					if(LEncoder.getSelectedSensorPosition(0) < 14800)

						Currentstage = AutoStage.kArmsDown;
				}

				if(Currentstage == AutoStage.kArmsDown) {

					Motor1.set(-.8);
					Timer.delay(3);
					Currentstage = AutoStage.kDone;



				}
				
				if (Currentstage == AutoStage.kDone) {
					Motor1.set(0);					
					
				}

				
				
			} 
			
			else {
				
				if(Currentstage == AutoStage.kStart) {

					DemoDrive.arcadeDrive(1, 0);

					if (navX.getYaw() > 2) {

						DemoDrive.arcadeDrive(1, -.2);
					}

					if (navX.getYaw() < -2) {

						DemoDrive.arcadeDrive(1, .2);

					}

					if(LEncoder.getSelectedSensorPosition(0) > 10000) {

						Currentstage = AutoStage.kTurn;


					}
				}

				if (Currentstage == AutoStage.kTurn) {

					DemoDrive.arcadeDrive(0, .8);

					if (LEncoder.getSelectedSensorPosition(0) > 12700) {

						Currentstage = AutoStage.kDriveForward1;
					}

				}

				if (Currentstage == AutoStage.kDriveForward1) {

					DemoDrive.arcadeDrive(1, 0);

					if (navX.getYaw() > 91) {

						DemoDrive.arcadeDrive(1, -.4);
					}

					if (navX.getYaw() < 89) {

						DemoDrive.arcadeDrive(1, .4);

					}

					if(LEncoder.getSelectedSensorPosition(0) > 22000) {

						Currentstage = AutoStage.kTurn1;
					}
				}

				if(Currentstage == AutoStage.kTurn1) {

					DemoDrive.arcadeDrive(0, -1);

					if(navX.getYaw() < 37) {

						Currentstage = AutoStage.kArmsUp;

					}

				}

				if(Currentstage == AutoStage.kArmsUp) {

					Motor1.set(1);
					Timer.delay(4);
					Currentstage= AutoStage.kDriveForward3;


				}



				if(Currentstage == AutoStage.kDriveForward3) {
					Motor1.set(0);
					DemoDrive.arcadeDrive(.5, 0);
					if(LEncoder.getSelectedSensorPosition(0) > 24000) {


						Currentstage = AutoStage.kDropCube;
					}


				}

				if(Currentstage == AutoStage.kDropCube) {

					double2.set(DoubleSolenoid.Value.kForward);

					Timer.delay(.1);

					Currentstage = AutoStage.kDriveBack;
				}

				if(Currentstage == AutoStage.kDriveBack) {

					DemoDrive.arcadeDrive(-.5, 0);

					if(LEncoder.getSelectedSensorPosition(0) < 22000) {

						Currentstage = AutoStage.kArmsDown;

					}

				}

				if(Currentstage == AutoStage.kArmsDown) {

					Motor1.set(-.8);
					Timer.delay(3);
					Currentstage = AutoStage.kTurn2;
				}

				if (Currentstage == AutoStage.kTurn2) {
					
					Motor1.set(0);


					DemoDrive.arcadeDrive(-1, 0);

					if(LEncoder.getSelectedSensorPosition(0) < 21500) {

						Currentstage = AutoStage.kDone;

					}

				}

				if (Currentstage == AutoStage.kDone) {
					
					Motor1.set(0);

					DemoDrive.arcadeDrive(0, 0);

					double1.set(DoubleSolenoid.Value.kReverse);


				}

				//outside
			}
		}
		
		
	}




	/**
	 * This function is called once each time the robot enters tele-operated
	 * mode
	 */
	@Override
	public void teleopInit() {



		Comp.setClosedLoopControl(true);

		Currentstage = AutoStage.kDone;
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
		Currentstage = AutoStage.kDone;


		//Drivesticks
		DemoDrive.tankDrive((xBox.getRawAxis(1)* -.7), (xBox.getRawAxis(5)* -.7));

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
			Motor1.set(-1);
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
