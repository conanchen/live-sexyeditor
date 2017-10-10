package net.intellij.plugins.sexyeditor.grpc;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;
import org.ditto.sexyimage.grpc.Common;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SexyImageClient {
    public final static String HOSTNAME = "localhost";
    public final static int PORT = 8980;
    public final static int IMAGE_QUEUE_CAPACITY = 30;
    public final static int IMAGE_QUEUE_ADD_BACK_LEAST_CAPACITY = IMAGE_QUEUE_CAPACITY / 5;
    public final static int IMAGE_QUEUE_REFRESH_INTERVAL_SECONDS = IMAGE_QUEUE_CAPACITY * 10; //300
    private static SexyImageClient instance;

    public interface SubcribeImageCallback {
        void onImageReceived(Image image);
    }

    private static final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(SexyImageClient.class.getName());

    private final ManagedChannel channel;
    private final ImageGrpc.ImageStub imageStub;
    private final HealthGrpc.HealthStub healthStub;
    private final HealthGrpc.HealthFutureStub healthFutureStub;
    private final HealthCheckRequest healthCheckRequest = HealthCheckRequest
            .newBuilder()
            .setService(ImageGrpc.getServiceDescriptor().getName())
            .build();


    private final AtomicBoolean isSubscribingImages = new AtomicBoolean(false);
    private final Set<Common.ImageType> currentSubscribeImageTypes = new HashSet<>();

    public static SexyImageClient getInstance() {
        if (instance == null) {
            instance = new SexyImageClient();
        }
        return instance;
    }

    SexyImageClient() {
        this(HOSTNAME, PORT);
    }

    public SexyImageClient(String HOSTNAME, int PORT) {
        channel = NettyChannelBuilder
                .forAddress(HOSTNAME, PORT)
                .usePlaintext(true)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .build();

        imageStub = ImageGrpc.newStub(channel);
        healthStub = HealthGrpc.newStub(channel);
        healthFutureStub = HealthGrpc.newFutureStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private ClientCallStreamObserver<ImageOuterClass.SubscribeRequest> subscribeStream;

    private class SubscribeImagesStreamObserver implements ClientResponseObserver<
            ImageOuterClass.SubscribeRequest, Common.ImageResponse> {


        SubcribeImageCallback subcribeImageCallback;

        public SubscribeImagesStreamObserver(SubcribeImageCallback subcribeImageCallback) {
            this.subcribeImageCallback = subcribeImageCallback;
        }


        @Override
        public void beforeStart(ClientCallStreamObserver requestStream) {
            subscribeStream = requestStream;
            subscribeStream.disableAutoInboundFlowControl();
            subscribeStream.setOnReadyHandler(() -> {
                isSubscribingImages.set(true);
                logger.info(String.format("%s", "setOnReadyHandler isSubscribingImages.set(true)"));
            });
        }

        @Override
        public void onNext(Common.ImageResponse response) {
            Image image = Image
                    .builder()
                    .setUrl(response.getUrl())
                    .setInfoUrl(response.getInfoUrl())
                    .setTitle(response.getTitle())
                    .setType(response.getType())
                    .build();
            subcribeImageCallback.onImageReceived(image);
            subscribeStream.request(1);
            logger.info(String.format("%s", "onNext subscribeStream.request(1)"));
        }


        @Override
        public void onError(Throwable t) {
            isSubscribingImages.set(false);
            logger.info(String.format("%s", "onError isSubscribingImages.set(false)"));
        }

        @Override
        public void onCompleted() {
            isSubscribingImages.set(false);
            logger.info(String.format("%s", "onCompleted isSubscribingImages.set(false)"));
        }
    }


    public void startSubscribeIfNeed(Set<Common.ImageType> imageTypes, SubcribeImageCallback subcribeImageCallback) {
        assert imageTypes != null && imageTypes.size() > 0;
        if (needToStart(imageTypes)) {
            currentSubscribeImageTypes.clear();
            currentSubscribeImageTypes.addAll(imageTypes);
            healthStub.check(healthCheckRequest,
                    new StreamObserver<HealthCheckResponse>() {
                        @Override
                        public void onNext(HealthCheckResponse value) {

                            imageStub.withWaitForReady()
                                    .subscribe(ImageOuterClass.SubscribeRequest
                                                    .newBuilder()
                                                    .addAllTypes(imageTypes)
                                                    .build()
                                            , new SubscribeImagesStreamObserver(subcribeImageCallback)
                                    );
                            logger.info(String.format("health.onNext startSubscribeIfNeed imageTypes=[%s]", gson.toJson(imageTypes.toArray())));
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.info(String.format("health.onError grpc service check health\n%s", t.getMessage()));
                        }

                        @Override
                        public void onCompleted() {
                            logger.info(String.format("health.onCompleted grpc service check health\n%s", ""));
                        }
                    });
        }

    }

    private boolean needToStart(Set<Common.ImageType> imageTypes) {
        boolean result = Sets.difference(currentSubscribeImageTypes, imageTypes).size() > 0 || !isSubscribingImages.get();
        logger.info(String.format("needToStart is %b", result));
        return result;
    }


    public void visit(String imageUrl) {
        ImageOuterClass.VisitRequest visitRequest = ImageOuterClass.VisitRequest.newBuilder().setUrl(imageUrl).build();
        imageStub.visit(visitRequest, new StreamObserver<ImageOuterClass.VisitResponse>() {
            @Override
            public void onNext(ImageOuterClass.VisitResponse value) {
                logger.info(String.format("imageUrl=%s, VisitResponse=%s", imageUrl, gson.toJson(value)));
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public static void main(String[] args) throws Exception {
        boolean normal = false, poster = false, sexy = false, porn = false;
        for (int i = 0; i < args.length; i++) {
            String subscribeType = args[i];
            normal = normal ? normal : "normal" .equals(subscribeType);
            poster = poster ? poster : "poster" .equals(subscribeType);
            sexy = sexy ? sexy : "sexy" .equals(subscribeType);
            porn = porn ? porn : "porn" .equals(subscribeType);
        }

        int port = 8980;
//        int PORT = 42420;

        final CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicInteger msgCount = new AtomicInteger(0);

        SubcribeImageCallback subscribeSubcribeImageCallback = (Image imageVo) -> logger.info(
                String.format("subscribeCallback.onImageReceived i=%d imageVo=[%s]",
                        msgCount.addAndGet(1), gson.toJson(imageVo))
        );


        SexyImageClient client = new SexyImageClient("localhost", port);


        client.startSubscribeIfNeed(getImageTypes(normal, poster, sexy, porn), subscribeSubcribeImageCallback);
        for (int i = 0; i > -1; i++) {
            client.visit(String.format("%s?%d", "http://images6.fanpop.com/image/photos/36800000/Game-of-Thrones-Season-4-game-of-thrones-36858892-2832-4256.jpg", i));
            Thread.sleep(30000);
            client.startSubscribeIfNeed(getImageTypes(normal, poster, sexy, porn), subscribeSubcribeImageCallback);
        }
        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);

        client.shutdown();
    }


    @NotNull
    public static Set<Common.ImageType> getImageTypes(boolean normal, boolean poster, boolean sexy, boolean porn) {
        return new HashSet<Common.ImageType>() {
            {
                if (normal) add(Common.ImageType.NORMAL);
                if (poster) add(Common.ImageType.POSTER);
                if (sexy) add(Common.ImageType.SEXY);
                if (porn) add(Common.ImageType.PORN);
            }
        };
    }
} 