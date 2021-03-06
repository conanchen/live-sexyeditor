package net.intellij.plugins.sexyeditor.grpc;

import com.google.gson.Gson;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.greeter.GreeterGrpc;
import io.grpc.examples.greeter.HelloReply;
import io.grpc.examples.greeter.HelloRequest;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import net.intellij.plugins.livesexyeditor.grpc.ImageGrpc;
import net.intellij.plugins.livesexyeditor.grpc.SubscribeRequest;
import net.intellij.plugins.livesexyeditor.grpc.VisitRequest;
import net.intellij.plugins.livesexyeditor.grpc.VisitResponse;
import org.ditto.sexyimage.common.grpc.ImageResponse;
import org.ditto.sexyimage.common.grpc.ImageType;
import org.ditto.sexyimage.common.grpc.Error;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SexyTestServer {

    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(SexyTestServer.class.getName());

    private int port = 42420;
    private Server server;

    private void start() throws Exception {

        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .addService(new ImageImpl())
                .addService(new HealthGrpc.HealthImplBase() {
                    @Override
                    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
                        logger.info(String.format("check by HealthCheckRequest=[%s]", gson.toJson(request)));
                        responseObserver.onNext(HealthCheckResponse.newBuilder().setStatus(HealthCheckResponse.ServingStatus.SERVING).build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        logger.info("Server started. Listening on PORT " + port);

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
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {


            String text = DateFormat.getInstance().format(new Date());

            HelloReply response = HelloReply.newBuilder()
                    .setMessage("Hello " + request.getName() + " at " + text).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            logger.info(String.format("sayHello response=[%s]", response.getMessage()));
        }
    }

    private class ImageImpl extends ImageGrpc.ImageImplBase {
        List<Image> images = new ArrayList<Image>() {
            {
                for (int i = 0; i < 10; i++) {
                    add(Image.builder()
                            .setUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg?i=" + i)
                            .setInfoUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg")
                            .setType(ImageType.NORMAL)
                            .build());
                }
            }
        };

        @Override
        public void subscribe(SubscribeRequest request, StreamObserver<ImageResponse> responseObserver) {
            Observable.interval(3, TimeUnit.SECONDS).subscribe(aLong -> {
                logger.info(String.format("aLong=%d push images", aLong));
                for (Image im : images) {
                    responseObserver.onNext(ImageResponse.newBuilder()
                            .setUrl(im.url)
                            .setInfoUrl(im.infoUrl)
                            .setType(im.type)
                            .setLastUpdated(System.currentTimeMillis())
                            .build());
                }
            });
        }

        @Override
        public void visit(VisitRequest request, StreamObserver<VisitResponse> responseObserver) {
            logger.info(String.format("VisitRequest.url=[%s]", request.getUrl()));
            responseObserver.onNext(VisitResponse.newBuilder().setError(Error.newBuilder().setCode("IMAGE.VISIT.OK").build()).build());
            responseObserver.onCompleted();
        }
    }
}