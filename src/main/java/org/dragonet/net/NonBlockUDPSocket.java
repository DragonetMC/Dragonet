/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 *                       Version 3, 29 June 2007
 *
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 * You can view LICENCE file for details. 
 *
 * @author The Dragonet Team
 */
package org.dragonet.net;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import org.dragonet.DragonetServer;

public class NonBlockUDPSocket extends Thread {

    private DragonetServer dragonetServer;
    private SocketAddress addr;
    private DatagramSocket socket;
    private Queue<DatagramPacket> receivedPacketQueue = new ArrayDeque<DatagramPacket>();
    private boolean running;

    public NonBlockUDPSocket(DragonetServer udp, SocketAddress address) {
        this.dragonetServer = udp;
        addr = address;
    }

    @Override
    public void run() {
        setName("UDPSocket");
        try {
            socket = new DatagramSocket(null);
            socket.setBroadcast(true);
            socket.setSendBufferSize(1024 * 1024 * 8); // from PocketMine
            socket.setReceiveBufferSize(1024 * 1024); // from PocketMine
            try {
                socket.bind(addr);
            } catch (BindException e) {
                this.dragonetServer.getLogger().error("Unable to bind to %s!", addr.toString());
                throw new RuntimeException(e);
            }
            running = true;
            while (running) {
                DatagramPacket pk = new DatagramPacket(new byte[1024 * 1024], 1024 * 1024);
                socket.receive(pk);
                synchronized (receivedPacketQueue) {
                    receivedPacketQueue.add(pk);
                }
            }
            socket.close();
        } catch (IOException e) {
        }
    }
    
    
    public DatagramPacket receive() {
        if (receivedPacketQueue.isEmpty()) {
            return null;
        }
        synchronized (receivedPacketQueue) {
            return receivedPacketQueue.poll();
        }
    }

    public boolean send(byte[] buffer, SocketAddress addr) {
        /*
         if(buffer.length > 1 && (buffer[0] == (byte)0xC0 || buffer[0] == (byte)0xA0)){
         System.out.println("ACK/NACK: " + ByteUtility.bytesToHexString(buffer));
         }
         */
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, addr));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DragonetServer getServer() {
        return this.dragonetServer;
    }

    public void end() {
        running = false;
        //this.socket.close();
        this.interrupt();
    }
}
