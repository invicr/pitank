package org.freedesktop.gstreamer.play;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import android.os.AsyncTask;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class FirstConnectionSocket extends AsyncTask {
    private static String SERV_IP;
    private static final int PORT = 8887;
    private static boolean Connect_Check;
    private static final int TIME_OUT = 3000;

    public FirstConnectionSocket() {
        //SERV_IP = "192.168.0.12";
        SERV_IP = "mjgo.iptime.org";
        setConnect_Check(false);
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            // 타임아웃 설정
            SocketAddress socketAddress = new InetSocketAddress(SERV_IP, PORT);
            Socket socket = new Socket();
            socket.setSoTimeout(TIME_OUT);

            socket.connect(socketAddress, TIME_OUT);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Connect");
            String msg = in.readLine();
            Log.d("F_Socket", msg);

            if (msg.equals("Connect")) {
                setConnect_Check(true);
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isConnect_Check() {
        return Connect_Check;
    }

    public static void setConnect_Check(boolean connect_Check) {
        Connect_Check = connect_Check;
    }
}
