package org.freedesktop.gstreamer.play;

/**
 * Created by admin on 2016-02-09.
 */
//좌우 3.0~11.5
//상하 4.0~8

public class Servo {
    private int Servo_Num;
    private int PWM;
    public Servo(int Servo_Num) {
        if(Servo_Num == 0)
            this.PWM = 80;
        else
            this.PWM = 60;
        this.setServo_Num(Servo_Num);
    }
    public int getPWM() {
        return PWM;
    }
    public void setPWM(int PWM) {
        this.PWM = PWM;
    }
    public int getServo_Num() {
        return Servo_Num;
    }
    public void setServo_Num(int servo_Num) {
        Servo_Num = servo_Num;
    }
}
