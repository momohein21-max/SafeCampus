/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.safecampus;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.UUID;
import safecampus.incident.IncidentReportServiceGrpc;
import safecampus.incident.IncidentRequest;
import safecampus.incident.IncidentResponse;

/**
 *
 * @author momohein
 */
public class IncidentReportServer extends IncidentReportServiceGrpc.IncidentReportServiceImplBase {

    private Server server;
    private static final int PORT = 50051;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(this)
                .build()
                .start();

        System.out.println("IncidentReportServer started on port " + PORT);

        ServiceRegistration.getInstance().registerService(
                "_incident._tcp.local.",
                "IncidentReportService",
                PORT,
                "Incident reporting gRPC service"
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down IncidentReportServer...");
            stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public StreamObserver<IncidentRequest> submitIncident(StreamObserver<IncidentResponse> responseObserver) {

        return new StreamObserver<IncidentRequest>() {
            String incidentType = "";
            String description = "";
            String location = "";
            String date = "";
            String time = "";
            String comments = "";

            @Override
            public void onNext(IncidentRequest request) {
                if (!request.getIncidentType().isBlank()) {
                    incidentType = request.getIncidentType();
                }
                if (!request.getDescription().isBlank()) {
                    description = request.getDescription();
                }
                if (!request.getLocation().isBlank()) {
                    location = request.getLocation();
                }
                if (!request.getDate().isBlank()) {
                    date = request.getDate();
                }
                if (!request.getTime().isBlank()) {
                    time = request.getTime();
                }
                if (!request.getOptionalComments().isBlank()) {
                    comments = request.getOptionalComments();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error receiving incident stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                String reportId = "IR-" + UUID.randomUUID().toString().substring(0, 8);

                IncidentResponse response = IncidentResponse.newBuilder()
                        .setConfirmationMessage("Incident report received: " + incidentType + " at " + location)
                        .setReportId(reportId)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();

                System.out.println("Incident saved: " + reportId);
                System.out.println(description + " | " + date + " " + time + " | " + comments);
            }
        };
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        IncidentReportServer server = new IncidentReportServer();
        server.start();
        server.blockUntilShutdown();
    }
}
