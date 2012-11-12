package org.s4digester.tourist;

import org.apache.s4.core.adapter.AdapterApp;
import org.s4digester.tourist.event.SignalingEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 信令文件Adapter，要求可以在不同的节点同时某个时间段的不同信令文件
 * TODO:为了便于测试，先用socket接口实现
 */
public class SignalingAdapter extends AdapterApp {
    @Override
    protected void onStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                ServerSocket serverSocket = null;
                Socket connectedSocket;
                BufferedReader in = null;
                try {
                    serverSocket = new ServerSocket(15000);
                    while (true) {
                        connectedSocket = serverSocket.accept();
                        in = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
                        String line;
                        while ((line = in.readLine())!=null){
                            String[] columns = line.split(",");
                            System.out.println("read: " + line);
                            if (columns.length > 3) {
                                SignalingEvent event = new SignalingEvent();
                                event.setImsi(columns[0]);
                                event.setTime(Long.parseLong(columns[1]));
                                event.setLoc(columns[2]);
                                event.setCell(columns[3]);
                                getRemoteStream().put(event);
                            }
                        }
                        connectedSocket.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    // System.exit(-1);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }).start();
    }
}
