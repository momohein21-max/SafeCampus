/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.safecampus;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 *
 * @author momohein
 */

public class ServiceRegistration {

    private static JmDNS jmdns;
    private static ServiceRegistration instance;

    private ServiceRegistration() throws IOException {
        InetAddress address = getLocalNetworkAddress();
        jmdns = JmDNS.create(address);
        System.out.println("ServiceRegistration running on: " + address);
    }

    public static ServiceRegistration getInstance() throws IOException {
        if (instance == null) {
            instance = new ServiceRegistration();
        }
        return instance;
    }

    public void registerService(String type, String name, int port, String description) throws IOException {
        ServiceInfo serviceInfo = ServiceInfo.create(type, name, port, description);
        jmdns.registerService(serviceInfo);
        System.out.println("Registered service: " + serviceInfo);
    }

    public void close() throws IOException {
        if (jmdns != null) {
            jmdns.close();
        }
    }

    private InetAddress getLocalNetworkAddress() throws IOException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();

            if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                continue;
            }

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();

                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                    return addr;
                }
            }
        }

        throw new IOException("No valid local network address found.");
    }
}