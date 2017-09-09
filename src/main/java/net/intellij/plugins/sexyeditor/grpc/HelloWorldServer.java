package net.intellij.plugins.sexyeditor.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.greeter.GreeterGrpc;
import net.intellij.plugins.sexyeditor.greeter.GreeterOuterClass;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public class HelloWorldServer {

    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private int port = 42420;
    private Server server;

    private void start() throws Exception {
        logger.info("Starting the grpc server");

        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .build()
                .start();

        logger.info("Server started. Listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** JVM is shutting down. Turning off grpc server as well ***");
            HelloWorldServer.this.stop();
            System.err.println("*** shutdown complete ***");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }


    public static void main(String[] args) throws Exception {
        logger.info("Server startup. Args = " + Arrays.toString(args));
        final HelloWorldServer helloWorldServer = new HelloWorldServer();

        helloWorldServer.start();
        helloWorldServer.blockUntilShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {


            String text = DateFormat.getInstance().format(  new Date());

            GreeterOuterClass.HelloReply response = GreeterOuterClass.HelloReply.newBuilder()
                    .setMessage("Hello " + request.getName() + " at " + text).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}