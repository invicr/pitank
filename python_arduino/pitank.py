import threading
import time
import socket
import serial
import sys

#Serial Port [GPIO and USB]
Arduino = serial.Serial('/dev/ttyAMA0', 115200)

# Socket Object
ctl_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
svr_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

ctl_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
svr_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
# Thread Boolean Object
Serial_Cycle = True
Socket_Cycle = True
# Serial Msg
serial_send_msg = '\0'
serial_recv_msg = '\0'

# Socket Thread > This Socket the receive a Motor signal and two ServoMotors signal
class SocketThread(threading.Thread):
   def run(self):
      global Serial_Thread, serial_send_msg, serial_recv_msg, Socket_Cycle, Serial_Cycle
      Now_Time = time.localtime()
      print "[%d:%d:%d] Socket Ready..." % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
      ctl_sock.bind(('', 8888))
      ctl_sock.listen(10)
      ctl_conn, ctl_addr = ctl_sock.accept()
      Now_Time = time.localtime()
      print "[%d:%d:%d] Socket Connected..." % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
      # Serial Thread Started
      Serial_Thread = ArduinoThread()
      Serial_Thread.start()
      while Socket_Cycle: 
         sock_recv_msg = ctl_conn.recv(1024)
         Now_Time = time.localtime()
         print "[SOCKET : %d:%d:%d] %s" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec, sock_recv_msg)
         serial_send_msg = sock_recv_msg         
         split_msg = sock_recv_msg.split('\n')
	 #print "msg : %s" (split_msg)          
         if (split_msg[0] == 'Quit' or len(split_msg[0]) < 1):
            Socket_Cycle = False
            Serial_Cycle = False
            ctl_conn.close()
      Serial_Thread.join()
      Now_Time = time.localtime()
      print "\n[%d:%d:%d] Socket Thread End..."  % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
class ArduinoThread(threading.Thread):
   def run(self):
      global serial_send_msg, serial_recv_msg, Serial_Cycle
      Now_Time = time.localtime()
      print "[%d:%d:%d] Arduino Thread Start" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
      while Serial_Cycle:
         time.sleep(0.13)
         print "[Serial] %s" % (serial_send_msg)
         Arduino.write(serial_send_msg)       
      Now_Time = time.localtime()
      print "\n[%d:%d:%d] Arduino Thread End" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)

# Main Function > Controls a total of three threads

try:
   Now_Time = time.localtime()
   print "[%d:%d:%d] PiTank Ready..." % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
   Arduino.write("0/0/0/0/80/60/\n")
   svr_sock.bind(('', 8887))
   svr_sock.listen(10)
   svr_conn, svr_addr = svr_sock.accept()
   Now_Time = time.localtime()
   print "[%d:%d:%d] Client Connecting..." % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
   svr_msg = svr_conn.recv(1024)
   Now_Time = time.localtime()
   print "[%d:%d:%d] %s" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec, svr_msg)
   if svr_msg == "Connect\n":
      Now_Time = time.localtime()
      print "[%d:%d:%d] Connect Success" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
      svr_conn.send("Connect")
   else:
      Now_Time = time.localtime()
      print "[%d:%d:%d] Connect Fail" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
   svr_conn.close()
   svr_sock.close()
   Now_Time = time.localtime()
   print "[%d:%d:%d] PiTank Process Started" % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
   Socket_Thread = SocketThread()
   Socket_Thread.start()
   Socket_Thread.join()
finally:
   Arduino.write("-1/0/0/0/80/60/\n")
   Arduino.close()
   Now_Time = time.localtime()
   print "\n[%d:%d:%d] FlyGarchi Process End..." % (Now_Time.tm_hour, Now_Time.tm_min, Now_Time.tm_sec)
