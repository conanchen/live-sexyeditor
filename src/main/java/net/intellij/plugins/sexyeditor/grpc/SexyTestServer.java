package net.intellij.plugins.sexyeditor.grpc;

import com.google.gson.Gson;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
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
                .addService(new HealthGrpc.HealthImplBase() {
                    @Override
                    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
                        logger.info(String.format("check by HealthCheckRequest=[%s]",gson.toJson(request)));
                       responseObserver.onNext(HealthCheckResponse.newBuilder().setStatus(HealthCheckResponse.ServingStatus.SERVING).build());
                       responseObserver.onCompleted();
                    }
                })
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
        public StreamObserver<ImageOuterClass.ToprankImageRequest> listToprankImages(StreamObserver<ImageOuterClass.ImageResponse> responseObserver) {
            return new StreamObserver<ImageOuterClass.ToprankImageRequest>() {

                @Override
                public void onNext(ImageOuterClass.ToprankImageRequest toprankImageRequest) {
                    logger.info(String.format("\nonNext --------------------------toprankImageRequest.getTypesList())=[%s]"
                            ,gson.toJson(toprankImageRequest.getTypesList())));

                    if (toprankImageRequest.getTypesList().contains(ImageOuterClass.ImageType.NORMAL)) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("http://n.7k7kimg.cn/2013/0316/1363403616970.jpg")
                                .setInfoUrl("http://www.baidu.com")
                                .setType(ImageOuterClass.ImageType.NORMAL)
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }

                    if (toprankImageRequest.getTypesList().contains(ImageOuterClass.ImageType.POSTER)) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg")
                                .setInfoUrl("http://www.qq.com")
                                .setType(ImageOuterClass.ImageType.POSTER)
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }

                    if (toprankImageRequest.getTypesList().contains(ImageOuterClass.ImageType.SEXY)) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("http://n.7k7kimg.cn/2013/0316/1363403583271.jpg")
                                .setInfoUrl("http://www.sohu.com")
                                .setType(ImageOuterClass.ImageType.SEXY)
                                .build();
                        responseObserver.onNext(response);
                        logger.info(String.format("onNext url=[%s]", response.getUrl()));
                    }

                    if (toprankImageRequest.getTypesList().contains(ImageOuterClass.ImageType.PORN)) {
                        ImageOuterClass.ImageResponse response = ImageOuterClass.ImageResponse
                                .newBuilder()
                                .setUuid(UUID.randomUUID().toString())
                                .setUrl("http://www.zjol.com.cn/pic/0/01/35/25/1352581_955017.jpg")
                                .setInfoUrl("http://www.163.com")
                                .setType(ImageOuterClass.ImageType.PORN)
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