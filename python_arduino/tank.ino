#include <Makeblock.h>
#include <Arduino.h>
#include <SoftwareSerial.h>

#include <string.h>
#include <Servo.h>
#include <Wire.h>

#define BUF_SIZE     36
#define INIT_SERVO_0 80
#define INIT_SERVO_1 60
#define TIME_DELAY   130
// Order Array Definition
#define ORDER_ARRAY_SIZE 6
#define ORDER_MAIN       0
#define ORDER_FRAME_X    1
#define ORDER_FRAME_Y    2
#define ORDER_DIRECTION  3
#define ORDER_SERVO_0    4
#define ORDER_SERVO_1    5
// Direction
#define  STOP           0
#define  FRONT          1
#define  FRONT_RIGHT    2
#define  TURN_RIGHT     3
#define  BACK_RIGHT     4
#define  BACK           5
#define  BACK_LEFT      6
#define  TURN_LEFT      7
#define  FRONT_LEFT     8

// Device Object
Servo Servo_0, Servo_1;
char inChar = -1;
char inData[BUF_SIZE];

// req Recv Value Definition
int User_Order = -1;
int req_Frame_X = 0;
int req_Frame_Y = 0;
int req_Direction = 0;
int req_Servo_Value_0 = INIT_SERVO_0;
int req_Servo_Value_1 = INIT_SERVO_1;

MeDCMotor MotorL(M1);
MeDCMotor MotorR(M2);
MePort port(PORT_6);

int servo1pin =  port.pin1();
int servo2pin =  port.pin2();

int index = 0;
char *tmp_split_msg;


//Function Definition
void Recv_AND_Split_Serial_Msg();
void Control_Servo_Motor();
void DC_Motor_Contorl();

/*
 * Board Init Setting Function
 */
void setup() {
  
  Serial.begin(115200);
  Servo_0.attach(servo1pin);
  Servo_1.attach(servo2pin);

}

void loop() {
  Recv_AND_Split_Serial_Msg();
  Control_Servo_Motor();
  DC_Motor_Contorl();
  delay(TIME_DELAY);
}

void Recv_AND_Split_Serial_Msg() {
  while (Serial.available() > 0) {
    char received = Serial.read();
    inData[index++] = received;
    
    if (received == '\n') {
      //Serial.print(inData);
      tmp_split_msg = strtok(inData, "/");
      for(int i = 0; i < ORDER_ARRAY_SIZE; i++) {
        switch (i) {
          case ORDER_MAIN:
            User_Order = atoi(tmp_split_msg);
            Serial.println(User_Order);
            break;
          case ORDER_FRAME_X:
            req_Frame_X = atoi(tmp_split_msg);
            Serial.println(req_Frame_X);
            break;
          case ORDER_FRAME_Y:
            req_Frame_Y = atoi(tmp_split_msg);
            Serial.println(req_Frame_Y);
            break;
          case ORDER_DIRECTION:
            req_Direction = atoi(tmp_split_msg);
            Serial.println(req_Direction);
            break;
          case ORDER_SERVO_0:
            req_Servo_Value_0 = atoi(tmp_split_msg);
            Serial.println(req_Servo_Value_0);
            break;
          case ORDER_SERVO_1:
            req_Servo_Value_1 = atoi(tmp_split_msg);
            Serial.println(req_Servo_Value_1);
            break;
        }
        tmp_split_msg = strtok(NULL, "/");
      }      
      index = 0;   
    }
  }  
}

void Control_Servo_Motor() {
  if (User_Order == 1) {
    Servo_0.write(req_Servo_Value_0);
    Servo_1.write(req_Servo_Value_1);
  } else {
    Servo_0.write(INIT_SERVO_0);
    Servo_1.write(INIT_SERVO_1);
  }
}

void Forward_Backward() {
  MotorL.run(req_Frame_Y);
  MotorR.run(-req_Frame_Y); 
}
void TurnLeft_Right() {
  MotorL.run(-req_Frame_X);
  MotorR.run(-req_Frame_X); 
}
void Right_Forward() {
  int calc_pwm = req_Frame_Y - req_Frame_X;
  MotorL.run(-req_Frame_Y);
  MotorR.run(calc_pwm);
}
void Right_Backward() {
  int calc_pwm = req_Frame_Y - req_Frame_X;
  MotorL.run(-calc_pwm);
  MotorR.run(req_Frame_Y);
}
void Left_Foreward() {
  int calc_pwm = req_Frame_Y - req_Frame_X;
  MotorL.run(-req_Frame_Y);
  MotorR.run(calc_pwm);  
}
void Left_Backward() {
  int calc_pwm = req_Frame_Y - req_Frame_X;
  MotorL.run(-calc_pwm);
  MotorR.run(req_Frame_Y);
}
void Stop() {
  MotorL.run(0);
  MotorR.run(0);  
}
void DC_Motor_Contorl() {
  switch (req_Direction) {
    case STOP:
      Stop();
      break;
    case FRONT:
    case BACK:
      Forward_Backward();
      break;
    case TURN_LEFT:
    case TURN_RIGHT:
      TurnLeft_Right();
      break;
    case FRONT_RIGHT:
      Right_Forward();
      break;
    case BACK_RIGHT:
      Right_Backward();
      break;
    case FRONT_LEFT:
      Left_Foreward();
      break;
    case BACK_LEFT:
      Left_Backward();
      break;
  }
}
