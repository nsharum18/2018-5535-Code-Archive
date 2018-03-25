
package org.usfirst.frc.team5535.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Talon;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SerialPort;

@SuppressWarnings({ "deprecation" })



/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	Joystick xbox; 
	Talon left, right;
	RobotDrive tank;
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
		tank = new RobotDrive(7, 8);
		tank.setExpiration(0.005);
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
		
		Timer.delay(0.005);

	}


	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {

	}

}
