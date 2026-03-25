package frc.robot.subsystems.intake;

import java.util.function.DoubleSupplier;

import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.AutoLogOutput;

import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {
  // Add any necessary motor controllers, sensors, or other components here
  private String intakeState = "neutral";

  private final TalonFX intakeMotor; // States our motor name

  public Intake() {
    // Constructor for the Intake subsystem
    // Initialize components here
    intakeMotor = new TalonFX(51);

  }

  @Override
  public void periodic() {

    if (intakeState == "intake") {
      intake();
    } else if (intakeState == "outtake") {
      outake();
    } else if (intakeState == "neutral") {
      stopIntake();
    }
    // This method will be called once per scheduler run
  }

  // Add methods to control the intake subsystem
  public void intake() {
    intakeMotor.set(1/2);
    // Code to start the intake mechanism
  }

  public void SetStateToIntake() {
    intakeState = "intake";
  }

  public void outake() {
    intakeMotor.set(-2/3);
  }

  public void setStateToOuttake() {
    intakeState = "outtake";
  }

  public Command stopIntakeCommand() {
    return new InstantCommand(
        () -> {
          intakeState = "neutral";
        },
        this);
  }

  public Command setIntakeSpeed(DoubleSupplier speed, Intake intake) {
    return new InstantCommand(
        () -> {
          intake.intakeState = "manual";
          intake.intakeMotor.set(speed.getAsDouble());
          SmartDashboard.putNumber("intake speed", speed.getAsDouble());
        },
        this);
  }

  public void stopIntake() {
    // Code to stop the intake mechanism
    intakeMotor.set(0);
  }
}
