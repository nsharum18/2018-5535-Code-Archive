
package org.usfirst.frc.team5535.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;

@SuppressWarnings({ "deprecation", "unused" })



/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	String autoSelected;
	Joystick xbox; 
	JoystickButton xbox1a, xboxselect1, xboxstart1, xboxx, xboxb, xboxy; 
	Solenoid exampleSolenoid, Solenoid2; 
	Talon left, right;
	RobotDrive tank;
	Compressor c = new Compressor(0); //makes a compressor on standard port
	Timer timer;
	AHRS navX;
	
	public enum Enumerations{
		
		kDriveForward,
		kTurn,
		kDone;
	}
Enumerations currentStage = Enumerations.kDone;

	/**
    RobotDrive myRobot;
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {

		
		navX = new AHRS(SerialPort.Port.kUSB);

		xbox = new Joystick(0);
		xbox1a = new JoystickButton(xbox, 1);
		xboxselect1 = new JoystickButton(xbox, 7); // Select xbox player one
		xboxstart1 = new JoystickButton(xbox, 8); //Start xbox player one


		tank = new RobotDrive(7, 8);
		tank.setExpiration(0.005);
		xboxb = new JoystickButton(xbox, 2);
		exampleSolenoid = new Solenoid(1);

		c.setClosedLoopControl(false); 

		timer = new Timer();

		


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
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */

	public void autonomousInit() {

		timer.reset();
		timer.start();
	   	navX.reset();

	   	currentStage = Enumerations.kDriveForward;
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		
	if (currentStage == Enumerations.kDriveForward) {
		tank.arcadeDrive(.5, 0);
		
		
		if (navX.getYaw() > 2) {
			
			tank.arcadeDrive(.5, -.5);
			
		}
		
		if(navX.getYaw() < -2) {
			
			tank.arcadeDrive(.5, .5);
		}
		
		if (timer.get() > 12) {
			
			currentStage = Enumerations.kDone;
		}
		
	}
	
	if (currentStage == Enumerations.kDone) {
		
		tank.arcadeDrive(0, 0);
	}
	
	//outside
		
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {

		tank.tankDrive(xbox.getRawAxis(1), xbox.getRawAxis(5));   	
		if(xbox1a.get()){
			exampleSolenoid.set(true);
		}
		else if(xboxb.get()){
		}
		else if (xboxselect1.get()){
			c.setClosedLoopControl(false);
		}
		else if (xboxstart1.get()){
			c.setClosedLoopControl(true);
		}
		else{
			exampleSolenoid.set(false);

		}
		Timer.delay(0.005);

	}


	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {

	}

}
