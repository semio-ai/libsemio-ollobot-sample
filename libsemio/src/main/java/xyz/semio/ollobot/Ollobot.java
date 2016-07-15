package xyz.semio.ollobot;

import xyz.semio.ollobot.service.BTConnectionService;
import xyz.semio.ollobot.utils.Dynamixel;
import xyz.semio.ollobot.utils.OLLOBOT;

public class Ollobot {
  private BTConnectionService _service;

  public Ollobot(BTConnectionService service) {
    this._service = service;
    //this.stop();
  }

  public void stop() {
    this.setBlueLed(false);
    this.setGreenLed(false);
    this.setMotorVelocity(1, 0);
    this.setMotorVelocity(2, 0);
  }

  public void setMotorVelocity(int port, int velocity) {
    final int ports[] = new int[]{
        OLLOBOT.Address.PORT_1_MOTOR_SPEED,
        137
    };
    velocity = (int)((double)velocity / 127.0 * 100.0);
    byte[] packet = Dynamixel.packetWriteWord(OLLOBOT.ID, ports[port - 1], velocity);
    this._service.sendMessageToRemote(packet);
  }

  public void setLeftMotorVelocity(int velocity) {
    this.setMotorVelocity(1, -velocity);
  }

  public void setRightMotorVelocity(int velocity) {
    this.setMotorVelocity(2, velocity);
  }

  public void drive(int leftVel, int rightVel) {
    this.setLeftMotorVelocity(leftVel);
    this.setRightMotorVelocity(rightVel);
  }

  public void setBlueLed(boolean state) {
    byte[] packet = Dynamixel.packetWriteByte(OLLOBOT.ID, OLLOBOT.Address.BLUE_LED, state ? 1 : 0);
    this._service.sendMessageToRemote(packet);
  }

  public void setGreenLed(boolean state) {
    byte[] packet = Dynamixel.packetWriteByte(OLLOBOT.ID, OLLOBOT.Address.GREEN_LED, state ? 1 : 0);
    this._service.sendMessageToRemote(packet);
  }
}
