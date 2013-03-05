package org.s4digester.wlan;

import org.apache.s4.core.adapter.AdapterApp;
import org.s4digester.wlan.event.WLANSignalEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WLAN热点提醒信令文件Adapter，要求可以在不同的节点同时某个时间段的不同信令文件
 * TODO: 为了便于测试，先用socket接口实现
 */
public class WLANSignalAdapter extends AdapterApp {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
    protected void onStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                ServerSocket serverSocket = null;
                Socket connectedSocket;
                BufferedReader in = null;
                try {
                    serverSocket = new ServerSocket(15002);
                    while (true) {
                        connectedSocket = serverSocket.accept();
                        in = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
                        String line;
                        while ((line = in.readLine())!=null){
                            String[] columns = line.split(",");
                            logger.debug("read: " + line);
                            if (columns.length > 10) {
                                WLANSignalEvent event = new WLANSignalEvent();
                                event.setImsi(columns[0]);
                                event.setEventType(columns[1]);
                                event.setSignalingTime(Long.parseLong(columns[2]));
                                event.setCause(columns[3]);
                                event.setLac(columns[4]);
                                event.setCell(columns[5]);
                                event.setCalling(columns[6]);
                                event.setCalled(columns[7]);
                                event.setApn(columns[8]);
                                event.setSgsnIp(columns[9]);
                                event.setRes2(columns[10]);
                                getRemoteStream().put(event);
                            } else{
                            	logger.error("invalid format: " + line + ", columns.length < 11.");
                            }
                        }
                        connectedSocket.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
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
