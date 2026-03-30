/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.safecampus;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import safecampus.lighting.LightingRequest;
import safecampus.lighting.LightingResponse;
import safecampus.lighting.SmartLightingServiceGrpc;

/**
 *
 * @author momohein
 */
public class SmartLightingServer extends SmartLightingServiceGrpc.SmartLightingServiceImplBase {

    private Server server;
    private static final int PORT = 50053;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(this)
                .build()
                .start();

        System.out.println("SmartLightingServer started on port " + PORT);

        ServiceRegistration.getInstance().registerService(
                "_lighting._tcp.local.",
                "SmartLightingService",
                PORT,
                "Smart lighting gRPC service"
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down SmartLightingServer...");
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
    public void setZoneBrightness(LightingRequest request, StreamObserver<LightingResponse> responseObserver) {

        LightingResponse response = LightingResponse.newBuilder()
                .setConfirmationMessage("Lighting updated successfully for zone " + request.getZoneId())
                .setZoneId(request.getZoneId())
                .setAppliedBrightnessLevel(request.getExpectedBrightnessLevel())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SmartLightingServer server = new SmartLightingServer();
        server.start();
        server.blockUntilShutdown();
    }
}
