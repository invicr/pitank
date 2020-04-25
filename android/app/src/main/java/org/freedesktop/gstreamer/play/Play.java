/* GStreamer
 *
 * Copyright (C) 2014 Sebastian Dröge <sebastian@centricular.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 */

package org.freedesktop.gstreamer.play;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.freedesktop.gstreamer.Player;


public class Play extends AppCompatActivity implements SurfaceHolder.Callback {
    private PowerManager.WakeLock wake_lock;
    private Player player;

    public enum Direction_Status {
        Stop, Front, Front_Right, Turn_Right, Back_Right, Back, Back_Left, Turn_Left, Front_Left
    }

    ;

    private Button Connect, Quit;
    private TextView Order_Status_Value, Connect_Status_Value;
    private RelativeLayout Frame_Joystick, Servo_Joystick;
    private TextView Frame_x_text, Frame_y_text, Frame_angle_text, Frame_distance_text, Frame_direction_text;
    private TextView Servo_x_text, Servo_y_text, Servo_angle_text, Servo_distance_text, Servo_direction_text;

    private JoyStickClass F_Joystick, S_Joystick;
    private ControlSocket cs;
    private FirstConnectionSocket fcs;
    private Boolean ConnectCheck = false;
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Player.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_player);

        player = new Player();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wake_lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GStreamer Play");
        wake_lock.setReferenceCounted(false);


        final GStreamerSurfaceView gsv = (GStreamerSurfaceView) this.findViewById(R.id.surface_video);

        player.setVideoDimensionsChangedListener(new Player.VideoDimensionsChangedListener() {
            public void videoDimensionsChanged(Player player, final int width, final int height) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.i("GStreamer", "Media size changed to " + width + "x" + height);
                        if (width > 0 && height > 0) {
                            gsv.media_width = width;
                            gsv.media_height = height;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    gsv.requestLayout();
                                }
                            });
                        } else {
                            Log.i("GStreamer", "Ignoring media size.");
                        }
                    }
                });
            }
        });

        SurfaceHolder sh = gsv.getHolder();
        sh.addCallback(this);

        player.setUri("rtsp://mjgo.iptime.org:8554/test");
        //player.setUri("rtsp://192.168.0.11:8554/test");

        //GStreamer 영상
        player.play();

        context = this;
        init();

        Joystick_setting(Frame_Joystick, F_Joystick, Frame_x_text, Frame_y_text, Frame_angle_text, Frame_distance_text, Frame_direction_text);
        Joystick_setting(Servo_Joystick, S_Joystick, Servo_x_text, Servo_y_text, Servo_angle_text, Servo_distance_text,Servo_direction_text);
    }

    protected void onDestroy() {
        player.close();
        super.onDestroy();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width " + width + " height " + height);
        player.setSurface(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        player.setSurface(null);
    }

    private void init() {
        Connect = (Button) findViewById(R.id.connect_btn);
        Connect.setOnClickListener(btn_ClickListener);

        Quit = (Button) findViewById(R.id.quit_btn);
        Quit.setOnClickListener(btn_ClickListener);

        Order_Status_Value = (TextView) findViewById(R.id.order_status_value);
        Connect_Status_Value = (TextView) findViewById(R.id.connect_status_value);

        Frame_x_text = (TextView) findViewById(R.id.frame_x_text);
        Frame_y_text = (TextView) findViewById(R.id.frame_y_text);
        Frame_angle_text = (TextView) findViewById(R.id.frame_angle_text);
        Frame_distance_text = (TextView) findViewById(R.id.frame_distance_text);
        Frame_direction_text = (TextView) findViewById(R.id.frame_direction_text);
        Frame_Joystick = (RelativeLayout) findViewById(R.id.frame_joystick_layout);

        F_Joystick = new JoyStickClass(getApplicationContext(), Frame_Joystick, R.drawable.image_button);
        F_Joystick.setStickSize(150, 150);
        F_Joystick.setLayoutSize(600, 600);
        F_Joystick.setLayoutAlpha(150);
        F_Joystick.setStickAlpha(100);
        F_Joystick.setOffset(90);
        F_Joystick.setMinimumDistance(50);

        Servo_x_text = (TextView) findViewById(R.id.servo_x_text);
        Servo_y_text = (TextView) findViewById(R.id.servo_y_text);
        Servo_angle_text = (TextView) findViewById(R.id.servo_angle_text);
        Servo_distance_text = (TextView) findViewById(R.id.servo_distance_text);
        Servo_direction_text = (TextView) findViewById(R.id.servo_direction_text);
        Servo_Joystick = (RelativeLayout) findViewById(R.id.servo_joystick_layout);

        S_Joystick = new JoyStickClass(getApplicationContext(), Servo_Joystick, R.drawable.image_button);
        S_Joystick.setStickSize(150, 150);
        S_Joystick.setLayoutSize(600, 600);
        S_Joystick.setLayoutAlpha(150);
        S_Joystick.setStickAlpha(100);
        S_Joystick.setOffset(90);
        S_Joystick.setMinimumDistance(50);

    }

    private void Joystick_setting(final RelativeLayout Joystick_Layout, final JoyStickClass Joystick_obj,
                                  final TextView text_X, final TextView text_Y, final TextView text_angle, final TextView text_Distance,
                                  final TextView text_Direction) {
        Joystick_Layout.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View arg0, MotionEvent arg1) {
                Joystick_obj.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int Joystick_X = 0, Joystick_Y = 0;
                    if (Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                        if (Joystick_obj.getX() <= 255 && Joystick_obj.getX() >= -255) {
                            Joystick_X = Joystick_obj.getX();
                        } else if (Joystick_obj.getX() > 255) {
                            Joystick_X = 255;
                        } else if (Joystick_obj.getX() < -255) {
                            Joystick_X = -255;
                        }
                        if (Joystick_obj.getY() <= 255 && Joystick_obj.getY() >= -255) {
                            Joystick_Y = Joystick_obj.getY();
                        } else if (Joystick_obj.getY() > 255) {
                            Joystick_Y = 255;
                        } else if (Joystick_obj.getY() < -255) {
                            Joystick_Y = -255;
                        }
                    } else {
                        if (Joystick_obj.getX() >= 15 && Joystick_obj.getX() <= 175) {
                            Joystick_X = Joystick_obj.getX();
                        } else if (Joystick_obj.getX() > 175) {
                            Joystick_X = 175;
                        } else {
                            Joystick_X = 15;
                        }
                        if (Joystick_obj.getY() >= 36 && Joystick_obj.getY() <= 115) {
                            Joystick_Y = Joystick_obj.getY();
                        } else if (Joystick_obj.getY() > 115) {
                            Joystick_Y = 115;
                        } else {
                            Joystick_Y = 36;
                        }
                    }

                    text_X.setText("X : " + Joystick_X);
                    text_Y.setText("Y : " + Joystick_Y);

                    text_angle.setText("Angle : " + String.valueOf(Joystick_obj.getAngle()));
                    text_Distance.setText("Distance : " + String.valueOf(Joystick_obj.getDistance()));

                    if (ConnectCheck) {
                        if (Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            Order_Status_Value.setText("프레임 제어");
                            cs.set_XY(Joystick_X, Joystick_Y);
                        } else {
                            Order_Status_Value.setText("카메라 제어");
                            cs.ctl_Servo(0, Joystick_X);
                            cs.ctl_Servo(1, Joystick_Y);
                        }
                    }

                    int direction = Joystick_obj.get8Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        text_Direction.setText("Direction : Up");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Front.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                        text_Direction.setText("Direction : Up Right");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Front_Right.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                        text_Direction.setText("Direction : Right");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Turn_Right.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                        text_Direction.setText("Direction : Down Right");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Back_Right.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                        text_Direction.setText("Direction : Down");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Back.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                        text_Direction.setText("Direction : Down Left");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Back_Left.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_LEFT) {
                        text_Direction.setText("Direction : Left");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Turn_Left.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_UPLEFT) {
                        text_Direction.setText("Direction : Up Left");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Front_Left.ordinal());
                        }
                    } else if (direction == JoyStickClass.STICK_NONE) {
                        text_Direction.setText("Direction : Center");
                        if (ConnectCheck && Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                            cs.setDirection(Direction_Status.Stop.ordinal());
                        }
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    if (Joystick_Layout.getId() == R.id.frame_joystick_layout) {
                        text_X.setText("X : 입력대기");
                        text_Y.setText("Y : 입력대기");
                        text_angle.setText("Angle : 입력대기");
                        text_Distance.setText("Distance : 입력대기");
                        text_Direction.setText("Direction : 입력대기");
                        if (ConnectCheck) {
                            cs.set_XY(0, 0);
                        }

                    } else {
                        if (!ConnectCheck) {
                            text_X.setText("X : 입력대기");
                            text_Y.setText("Y : 입력대기");
                        }
                        text_angle.setText("Angle : 입력대기");
                        text_Distance.setText("Distance : 입력대기");
                        text_Direction.setText("Direction : 입력대기");
                    }
                }
                return true;
            }
        });
    }

    Button.OnClickListener btn_ClickListener = new View.OnClickListener() {
        @SuppressWarnings("unchecked")
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.connect_btn:
                    if (!ConnectCheck) {
                        try {
                            Connect_Status_Value.setText("접속 중");
                            fcs = new FirstConnectionSocket();
                            fcs.execute(this);
                            fcs.get();
                            ConnectCheck = FirstConnectionSocket.isConnect_Check();
                            if (ConnectCheck) {
                                cs = new ControlSocket();
                                cs.start();

                                Connect_OnOff(true);

                                Connect_Status_Value.setText("접속 완료");
                                Order_Status_Value.setText("명령 없음");
                                Toast.makeText(context, "접속에 성공하였습니다.", Toast.LENGTH_SHORT).show();

                            } else {
                                // 접속 실패
                                Connect_Status_Value.setText("접속 실패");
                                Toast.makeText(context, "접속에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Connect_Status_Value.setText("에러 발생");
                            Toast.makeText(context, "에러가 발생하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ConnectCheck = FirstConnectionSocket.isConnect_Check();
                        if (ConnectCheck) {
                            Connect_OnOff(false);
                            cs.setThread(false);
                            ConnectCheck = false;
                            Quit.setEnabled(true);

                            //player.close();

                            Log.d("Socket", "DisConnected");
                            Toast.makeText(context, "접속을 해제하였습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("Socket", "DisConnected Fail");
                            Toast.makeText(context, "접속 해제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.quit_btn:
                    AlertDialog.Builder d = new AlertDialog.Builder(context);
                    d.setMessage("정말 종료하시겠습니까?");
                    d.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // process전체 종료
                            if (ConnectCheck) {
                                cs.setThread(false);
                            }
                            finish();
                        }
                    });
                    d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    d.show();
                    break;
            }
        }
    };

    public void Connect_OnOff(boolean OnOff) {
        // ON
        if (OnOff) {
            Quit.setEnabled(false);
            ConnectCheck = true;
            Connect.setText("연결 해제");
            Frame_x_text.setText("X : 입력대기");
            Frame_y_text.setText("Y : 입력대기");
            Frame_angle_text.setText("Angle : 입력대기");
            Frame_distance_text.setText("Distance : 입력대기");
            Frame_direction_text.setText("Direction : 입력대기");

            Servo_x_text.setText("X : 입력대기");
            Servo_y_text.setText("Y : 입력대기");
            Servo_angle_text.setText("Angle : 입력대기");
            Servo_distance_text.setText("Distance : 입력대기");
            Servo_direction_text.setText("Direction : 입력대기");
        }
        // OFF
        else {
            Quit.setEnabled(true);
            ConnectCheck = true;
            Connect.setText("연결");
            Connect_Status_Value.setText("접속 해제");
            Order_Status_Value.setText("비활성화");
        }
    }

}
