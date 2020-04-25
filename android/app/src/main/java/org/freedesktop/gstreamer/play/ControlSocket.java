package org.freedesktop.gstreamer.play;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class ControlSocket extends Thread {

    private static String SERV_IP;
    private static final int PORT = 8888;
    private boolean setThread = false;
    private int direction;
    private int Frame_X, Frame_Y;
    private Servo Servo[];
    private String msg;

    public ControlSocket() {
        //SERV_IP = "192.168.0.12";
        SERV_IP = "mjgo.iptime.org";
        direction = Play.Direction_Status.Stop.ordinal();
        setThread = true;
        Frame_X = 0;
        Frame_Y = 0;

        Servo = new Servo[2];

        Servo[0] = new Servo(0);
        Servo[1] = new Servo(1);
    }

    public void run() {
        System.out.println("Connection!");
        try {
            Socket socket = new Socket(SERV_IP, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            while (setThread) {
                sleep(100);
                msg = "1" + "/" + Frame_X + "/" + Frame_Y + "/" + direction + "/" + Servo[0].getPWM() + "/" + Servo[1].getPWM();
                out.println(msg);
            }
            out.println("Quit");

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void ctl_Servo(int Servo_No, int PWM) {
        this.Servo[Servo_No].setPWM(PWM);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void set_XY(int frame_X, int frame_Y) {
        this.Frame_X = frame_X;
        this.Frame_Y = frame_Y;
    }

    public void setThread(boolean OnOff) {
        setThread = OnOff;
    }
}
