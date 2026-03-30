/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.safecampus;

/**
 *
 * @author momohein
 */
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import safecampus.incident.IncidentReportServiceGrpc;
import safecampus.incident.IncidentRequest;
import safecampus.incident.IncidentResponse;
import safecampus.lighting.LightingRequest;
import safecampus.lighting.LightingResponse;
import safecampus.lighting.SmartLightingServiceGrpc;
import safecampus.route.LocationUpdate;
import safecampus.route.SafeRouteRequest;
import safecampus.route.SafeRouteResponse;
import safecampus.route.SafeRouteServiceGrpc;
import safecampus.route.SafetyNotification;



public class ClientGUI extends JFrame {

    private JTextArea outputArea;

    private IncidentReportServiceGrpc.IncidentReportServiceStub incidentStub;
    private SmartLightingServiceGrpc.SmartLightingServiceBlockingStub lightingStub;
    private SafeRouteServiceGrpc.SafeRouteServiceBlockingStub routeBlockingStub;
    private SafeRouteServiceGrpc.SafeRouteServiceStub routeAsyncStub;

    public ClientGUI() {
        setTitle("SafeCampus Client GUI");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JButton discoverButton = new JButton("Discover Services");
        JButton incidentButton = new JButton("Submit Incident");
        JButton routeButton = new JButton("Get Safe Routes");
        JButton lightingButton = new JButton("Set Lighting");

        buttonPanel.add(discoverButton);
        buttonPanel.add(incidentButton);
        buttonPanel.add(routeButton);
        buttonPanel.add(lightingButton);

        add(buttonPanel, BorderLayout.NORTH);

        JButton monitorButton = new JButton("Live Safety Monitoring");
        add(monitorButton, BorderLayout.SOUTH);

        discoverButton.addActionListener(e -> discoverServices());
        incidentButton.addActionListener(e -> submitIncident());
        routeButton.addActionListener(e -> getSafeRoutes());
        lightingButton.addActionListener(e -> setLighting());
        monitorButton.addActionListener(e -> startLiveMonitoring());
    }

    private void discoverServices() {
        try {
            outputArea.append("Discovering services...\n");

            ServiceDiscovery discovery = new ServiceDiscovery();

            ServiceDiscovery.DiscoveredService incidentService =
                    discovery.discoverService("_incident._tcp.local.", "IncidentReportService", 5000);

            ServiceDiscovery.DiscoveredService routeService =
                    discovery.discoverService("_route._tcp.local.", "SafeRouteService", 5000);

            ServiceDiscovery.DiscoveredService lightingService =
                    discovery.discoverService("_lighting._tcp.local.", "SmartLightingService", 5000);

            if (incidentService != null) {
                ManagedChannel incidentChannel = ManagedChannelBuilder
                        .forAddress(incidentService.getHost(), incidentService.getPort())
                        .usePlaintext()
                        .build();

                incidentStub = IncidentReportServiceGrpc.newStub(incidentChannel);
                outputArea.append("Connected to Incident Service: " + incidentService + "\n");
            } else {
                outputArea.append("Incident Service not found.\n");
            }

            if (routeService != null) {
                ManagedChannel routeChannel = ManagedChannelBuilder
                        .forAddress(routeService.getHost(), routeService.getPort())
                        .usePlaintext()
                        .build();

                routeBlockingStub = SafeRouteServiceGrpc.newBlockingStub(routeChannel);
                routeAsyncStub = SafeRouteServiceGrpc.newStub(routeChannel);
                outputArea.append("Connected to Route Service: " + routeService + "\n");
            } else {
                outputArea.append("Route Service not found.\n");
            }

            if (lightingService != null) {
                ManagedChannel lightingChannel = ManagedChannelBuilder
                        .forAddress(lightingService.getHost(), lightingService.getPort())
                        .usePlaintext()
                        .build();

                lightingStub = SmartLightingServiceGrpc.newBlockingStub(lightingChannel);
                outputArea.append("Connected to Lighting Service: " + lightingService + "\n");
            } else {
                outputArea.append("Lighting Service not found.\n");
            }

            outputArea.append("Discovery finished.\n\n");

        } catch (Exception e) {
            outputArea.append("Discovery error: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void submitIncident() {
        if (incidentStub == null) {
            outputArea.append("Incident service not connected.\n");
            return;
        }

        StreamObserver<IncidentResponse> responseObserver = new StreamObserver<IncidentResponse>() {
            @Override
            public void onNext(IncidentResponse response) {
                outputArea.append("Incident Response: "
                        + response.getConfirmationMessage()
                        + " | Report ID: "
                        + response.getReportId()
                        + "\n");
            }

            @Override
            public void onError(Throwable t) {
                outputArea.append("Incident error: " + t.getMessage() + "\n");
            }

            @Override
            public void onCompleted() {
                outputArea.append("Incident stream completed.\n");
            }
        };

        StreamObserver<IncidentRequest> requestObserver = incidentStub.submitIncident(responseObserver);

        requestObserver.onNext(IncidentRequest.newBuilder()
                .setIncidentType("Harassment")
                .build());

        requestObserver.onNext(IncidentRequest.newBuilder()
                .setDescription("Suspicious person following student")
                .build());

        requestObserver.onNext(IncidentRequest.newBuilder()
                .setLocation("North Gate")
                .build());

        requestObserver.onNext(IncidentRequest.newBuilder()
                .setDate("2026-03-27")
                .build());

        requestObserver.onNext(IncidentRequest.newBuilder()
                .setTime("18:45")
                .build());

        requestObserver.onNext(IncidentRequest.newBuilder()
                .setOptionalComments("Student requested security escort")
                .build());

        requestObserver.onCompleted();
    }

    private void getSafeRoutes() {
        if (routeBlockingStub == null) {
            outputArea.append("Route service not connected.\n");
            return;
        }

        SafeRouteRequest request = SafeRouteRequest.newBuilder()
                .setStartLatitude(53.3498)
                .setStartLongitude(-6.2603)
                .setEndLatitude(53.3440)
                .setEndLongitude(-6.2672)
                .setMaxAllowedRisk(5.0)
                .build();

        outputArea.append("Safe Routes:\n");

        Iterator<SafeRouteResponse> routes = routeBlockingStub.getSafeRoutes(request);

        while (routes.hasNext()) {
            SafeRouteResponse route = routes.next();

            outputArea.append("- " + route.getRouteSummary()
                    + " | Distance: " + route.getDistanceMeters()
                    + "m | Time: " + route.getEstimatedTimeMinutes()
                    + " mins | Safety: " + route.getSafetyRating() + "\n");
        }

        outputArea.append("\n");
    }

    private void setLighting() {
        if (lightingStub == null) {
            outputArea.append("Lighting service not connected.\n");
            return;
        }

        LightingRequest request = LightingRequest.newBuilder()
                .setZoneId("Zone-A")
                .setExpectedBrightnessLevel("High")
                .setReason("Incident reported in this area")
                .build();

        LightingResponse response = lightingStub.setZoneBrightness(request);

        outputArea.append("Lighting Response: "
                + response.getConfirmationMessage()
                + " | Zone: "
                + response.getZoneId()
                + " | Applied Level: "
                + response.getAppliedBrightnessLevel()
                + "\n\n");
    }

    private void startLiveMonitoring() {
        if (routeAsyncStub == null) {
            outputArea.append("Route service not connected.\n");
            return;
        }

        StreamObserver<SafetyNotification> responseObserver = new StreamObserver<SafetyNotification>() {
            @Override
            public void onNext(SafetyNotification notification) {
                outputArea.append("Live Alert: "
                        + notification.getMessage()
                        + " | Risk: "
                        + notification.getRiskLevel()
                        + " | Support: "
                        + notification.getNearbySupport()
                        + "\n");
            }

            @Override
            public void onError(Throwable t) {
                outputArea.append("Live monitoring error: " + t.getMessage() + "\n");
            }

            @Override
            public void onCompleted() {
                outputArea.append("Live monitoring completed.\n\n");
            }
        };

        StreamObserver<LocationUpdate> requestObserver =
                routeAsyncStub.liveSafetyMonitoring(responseObserver);

        requestObserver.onNext(LocationUpdate.newBuilder()
                .setUserId("Momo01")
                .setCurrentLatitude(53.3498)
                .setCurrentLongitude(-6.2603)
                .setTimestamp("18:46")
                .build());

        requestObserver.onNext(LocationUpdate.newBuilder()
                .setUserId("Momo01")
                .setCurrentLatitude(53.3485)
                .setCurrentLongitude(-6.2610)
                .setTimestamp("18:48")
                .build());

        requestObserver.onNext(LocationUpdate.newBuilder()
                .setUserId("Momo01")
                .setCurrentLatitude(53.3472)
                .setCurrentLongitude(-6.2620)
                .setTimestamp("18:50")
                .build());

        requestObserver.onCompleted();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
        });
    }
}