/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.safecampus;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import safecampus.route.LocationUpdate;
import safecampus.route.SafeRouteRequest;
import safecampus.route.SafeRouteResponse;
import safecampus.route.SafeRouteServiceGrpc;
import safecampus.route.SafetyNotification;

/**
 *
 * @author momohein
 */
public class SafeRouteServer extends SafeRouteServiceGrpc.SafeRouteServiceImplBase {

    private Server server;
    private static final int PORT = 50052;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(this)
                .build()
                .start();

        System.out.println("SafeRouteServer started on port " + PORT);

        ServiceRegistration.getInstance().registerService(
                "_route._tcp.local.",
                "SafeRouteService",
                PORT,
                "Safe route gRPC service"
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down SafeRouteServer...");
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
    public void getSafeRoutes(SafeRouteRequest request, StreamObserver<SafeRouteResponse> responseObserver) {

        SafeRouteResponse route1 = SafeRouteResponse.newBuilder()
                .setDistanceMeters(850)
                .setEstimatedTimeMinutes(11)
                .setSafetyRating(4.7)
                .setRouteSummary("Route A: Main gate to library through well-lit road")
                .build();

        SafeRouteResponse route2 = SafeRouteResponse.newBuilder()
                .setDistanceMeters(950)
                .setEstimatedTimeMinutes(13)
                .setSafetyRating(4.9)
                .setRouteSummary("Route B: Path beside security office with CCTV coverage")
                .build();

        SafeRouteResponse route3 = SafeRouteResponse.newBuilder()
                .setDistanceMeters(780)
                .setEstimatedTimeMinutes(10)
                .setSafetyRating(4.2)
                .setRouteSummary("Route C: Shortest route but lower lighting in one section")
                .build();

        responseObserver.onNext(route1);
        responseObserver.onNext(route2);
        responseObserver.onNext(route3);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<LocationUpdate> liveSafetyMonitoring(
            StreamObserver<SafetyNotification> responseObserver) {

        return new StreamObserver<LocationUpdate>() {
            @Override
            public void onNext(LocationUpdate update) {
                SafetyNotification notification = SafetyNotification.newBuilder()
                        .setMessage("Location received for user " + update.getUserId()
                                + ". Stay on the well-lit path.")
                        .setRiskLevel(2.0)
                        .setNearbySupport("Nearest support point: Security Desk A")
                        .build();

                responseObserver.onNext(notification);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Live monitoring error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SafeRouteServer server = new SafeRouteServer();
        server.start();
        server.blockUntilShutdown();
    }
}
