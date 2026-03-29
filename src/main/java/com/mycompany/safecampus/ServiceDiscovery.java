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

public class ServiceDiscovery {

    public static class DiscoveredService {
        private final String host;
        private final int port;
        private final String name;
        private final String type;

        public DiscoveredService(String host, int port, String name, String type) {
            this.host = host;
            this.port = port;
            this.name = name;
            this.type = type;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return name + " -> " + host + ":" + port + " (" + type + ")";
        }
    }

    public DiscoveredService discoverService(String serviceType, String serviceName, int timeout)
            throws IOException {

        InetAddress address = getLocalNetworkAddress();
        JmDNS jmdns = JmDNS.create(address);

        System.out.println("Looking up service: " + serviceName + "." + serviceType + " using " + address);

        ServiceInfo serviceInfo = jmdns.getServiceInfo(serviceType, serviceName, timeout);

        if (serviceInfo != null) {
            String host = serviceInfo.getHostAddresses()[0];
            int port = serviceInfo.getPort();

            DiscoveredService discovered =
                    new DiscoveredService(host, port, serviceInfo.getName(), serviceType);

            System.out.println("Discovered: " + discovered);
            jmdns.close();
            return discovered;
        }

        System.out.println("Service not found: " + serviceName + "." + serviceType);
        jmdns.close();
        return null;
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