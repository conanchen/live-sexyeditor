package net.intellij.plugins.sexyeditor.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.greeter.GreeterGrpc;
import net.intellij.plugins.sexyeditor.greeter.GreeterOuterClass;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public class SexyTestServer {

    private static final Logger logger = Logger.getLogger(SexyTestServer.class.getName());

    private int port = 42420;
    private Server server;

    private void start() throws Exception {
        logger.info("Starting the grpc server");

        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .addService(new ImageImpl())
                .build()
                .start();

        logger.info("Server started. Listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** JVM is shutting down. Turning off grpc server as well ***");
            SexyTestServer.this.stop();
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
        final SexyTestServer sexyTestServer = new SexyTestServer();

        sexyTestServer.start();
        sexyTestServer.blockUntilShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {


            String text = DateFormat.getInstance().format(new Date());

            GreeterOuterClass.HelloReply response = GreeterOuterClass.HelloReply.newBuilder()
                    .setMessage("Hello " + request.getName() + " at " + text).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info(String.format("sayHello response=[%s]", response.getMessage()));
        }
    }

    private class ImageImpl extends ImageGrpc.ImageImplBase {

        @Override
        public void listImages(ImageOuterClass.ImageRequest request,
                               StreamObserver<ImageOuterClass.ImageResponse> responseObserver) {
            Random r = new Random();
            int n = r.nextInt(3);
            if (n == 0) {
                ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                        .newBuilder()
                        .setUuid(UUID.randomUUID().toString())
                        .setUrl("http://n.7k7kimg.cn/2013/0316/1363403616970.jpg")
                        .setType("NORMAL")
                        .build();
                responseObserver.onNext(response);
                logger.info(String.format("listImages response.url=[%s]", response.getUrl()));
            } else if (n == 1) {

                ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                        .newBuilder()
                        .setUuid(UUID.randomUUID().toString())
                        .setUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg")
                        .setType("NORMAL")
                        .build();
                responseObserver.onNext(response);
                logger.info(String.format("listImages response.url=[%s]", response.getUrl()));
            } else {
                ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                        .newBuilder()
                        .setUuid(UUID.randomUUID().toString())
                        .setUrl("http://n.7k7kimg.cn/2013/0316/1363403583271.jpg")
                        .setType("NORMAL")
                        .build();
                responseObserver.onNext(response);
                logger.info(String.format("listImages response.url=[%s]", response.getUrl()));
            }
            responseObserver.onCompleted();
//
//            string uuid = 1;
//            string url = 2;
//            string title = 3;
//            string desc = 4;
//            string type = 5;//NORMAL,SEXY,PORN
//
//            int64 lastUpdated = 6;
//            bool active = 7;
        }
    }
}