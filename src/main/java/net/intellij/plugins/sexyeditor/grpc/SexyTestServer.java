package net.intellij.plugins.sexyeditor.grpc;

import com.google.gson.Gson;
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

    private final static Gson gson = new Gson();
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
        public StreamObserver<ImageOuterClass.ImageRequest> listMessages(StreamObserver<ImageOuterClass.ImageResponse> responseObserver) {
            return new StreamObserver<ImageOuterClass.ImageRequest>() {

                @Override
                public void onNext(ImageOuterClass.ImageRequest value) {
                    logger.info(String.format("\nonNext --------------------------normal=%b,poster=%b,sexy=%b,porn=%b",value.getNormal(),value.getPoster(),value.getSexy(),value.getPorn()));

                    if (value.getNormal()) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("http://n.7k7kimg.cn/2013/0316/1363403616970.jpg")
                                .setInfoUrl("http://www.baidu.com")
                                .setType("NORMAL")
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }

                    if (value.getPoster()) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg")
                                .setInfoUrl("http://www.qq.com")
                                .setType("POSTER")
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }

                    if (value.getSexy()) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("http://n.7k7kimg.cn/2013/0316/1363403583271.jpg")
                                .setInfoUrl("http://www.sohu.com")
                                .setType("SEXY")
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }

                    if (value.getPorn()) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("http://www.zjol.com.cn/pic/0/01/35/25/1352581_955017.jpg")
                                .setInfoUrl("http://www.163.com")
                                .setType("PORN")
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            };
        }
    }
}