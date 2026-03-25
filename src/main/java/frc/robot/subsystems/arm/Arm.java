package frc.robot.subsystems.arm;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.AutoLogOutput;

// for magic controller PID

public class Arm extends SubsystemBase {
  // Add any necessary motor controllers, sensors, or other components here
  private String state = "neutral";
  private final TalonFX armMotor;
  private final double armTopLimit = .02; // make sure zero is arm up, or adjust later
  private final double armBottomLimit = 0.2; // in rotations, ADJUST LATER
  private final CANcoder armCANcoder = new CANcoder(53);
  private Boolean commandRan = false;

  private static final double kP = 0.8; // adjust later
  private static final double kI = 0.0; // adjust later
  private static final double kD = 0.0; // adjust later
  private boolean jiggleModeUp = true;
  // arm feedforward values?
  // makes a new PID controller
  PIDController armPidController = new PIDController(kP, kI, kD);

  private static double armPosition = 0;

  public Arm() {
    // Constructor for the Arm subsystem
    // Initialize components here
    SmartDashboard.putString("arm pid output", "0");
    SmartDashboard.putString("arm ", state);
    SmartDashboard.putBoolean("jiggle mode", jiggleModeUp);
    SmartDashboard.putBoolean("commandRan", commandRan);
    armMotor = new TalonFX(52);
    armCANcoder.setPosition(
        0); // hopefully callibrates encoder to zero when robot turns on, adjust if not
    armMotor.setNeutralMode(NeutralModeValue.Brake);
    configureCurrentLimits();
  }

  public void configureCurrentLimits() {
    TalonFXConfiguration toConfigure = new TalonFXConfiguration();
    CurrentLimitsConfigs currentLimits = new CurrentLimitsConfigs();
    // lets the current limits be controlled
    currentLimits.SupplyCurrentLimitEnable = true;
    currentLimits.SupplyCurrentLimit = 50; // Highest limit of amps
    currentLimits.SupplyCurrentLowerLimit = 40; // goes down to 40 Amps if it reaches 50 amps
    currentLimits.SupplyCurrentLowerTime = 1; // lowers limit for 1 seecond]

    // Stator Current Limit(motor output)
    currentLimits.StatorCurrentLimitEnable = true;
    currentLimits.StatorCurrentLimit = 120;

    toConfigure.CurrentLimits = currentLimits;
    // Apply Configuration
    armMotor.getConfigurator().apply(toConfigure);
  }

  @AutoLogOutput
  private void
      updateArmPosition() { // having a method for this feels excessive but its the only way i can
    // figure
    // out to log arm position
    if (armCANcoder != null) {
      armPosition = armCANcoder.getPosition().getValueAsDouble(); // idk what unit... figure out
      SmartDashboard.putString("Arm Encoder Position", String.valueOf(armPosition));
    }
  }

  @Override
  public void periodic() {
    updateArmPosition(); // uncomment when motor is on and set
    if (state.equals("Extend")) {
      extendArm();
    } else if (state.equals("Retract")) {
      retractArm();
    } else {
      armMotor.stopMotor();
    }

    SmartDashboard.putString("Arm state", state);
    SmartDashboard.putString("arm ", state);
    SmartDashboard.putBoolean("jiggle mode", jiggleModeUp);
    // This method will be called once per scheduler run
  }

  // Add methods to control the arm subsystem

  public void extendArm() {
    armPidController.setSetpoint(armBottomLimit);
    double setpoint = armPidController.getSetpoint();
    double output = armPidController.calculate(armPosition, setpoint);
    armMotor.set(output);
    SmartDashboard.putString("arm pid output", Double.toString(output));
  }

  public void retractArm() {
    armPidController.setSetpoint(armTopLimit);
    double error = armPidController.getErrorTolerance(); // not that useful
    double output = armPidController.calculate(armPosition, armTopLimit);
    SmartDashboard.putString("arm pid output", Double.toString(output));
    armMotor.set(output);
  }

  public Command extendArmCommand() {
    return new InstantCommand(
        () -> {
          state = "Extend";
          commandRan = true;
          SmartDashboard.putBoolean("commandRan", commandRan);
        },
        this);
  }

  public Command retractArmCommand() {
    return new InstantCommand(
        () -> {
          state = "Retract";
          commandRan = true;
        },
        this);
  }

  public void manualArm(DoubleSupplier speed, Arm arm) {
    if (armPosition < armTopLimit && speed.getAsDouble() < 0) {
      armMotor.set(speed.getAsDouble());
    } else if (armPosition > armBottomLimit && speed.getAsDouble() > 0) {
      armMotor.set(speed.getAsDouble());
    } else {
      armMotor.set(0);
    }
  }

  public Command manualArmCommand(DoubleSupplier speed, Arm arm) {
    return new InstantCommand(
        () -> {
          manualArm(speed, arm);
        },
        this);
  }
}
