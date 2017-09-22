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
import net.intellij.plugins.sexyeditor.Image;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SexyImageClient {

    public interface SubcribeImageCallback {
        void onImageReceived(Image image);
    }

    private static final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(SexyImageClient.class.getName());

    private final ManagedChannel channel;
    private final ImageGrpc.ImageStub asyncStub;
    private final AtomicBoolean isSubscribingImages = new AtomicBoolean(false);
    private final Set<ImageOuterClass.ImageType> currentSubscribeImageTypes = new HashSet<>();


    public SexyImageClient(String hostname, int port) {
        channel = NettyChannelBuilder
                .forAddress(hostname, port)
                .usePlaintext(true)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .build();

        asyncStub = ImageGrpc.newStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    ClientCallStreamObserver<ImageOuterClass.ImageRequest> subscribeStream;

    class SubscribeImagesStreamObserver implements ClientResponseObserver<ImageOuterClass.ImageRequest, ImageOuterClass.ImageResponse> {


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
        public void onNext(ImageOuterClass.ImageResponse response) {
            Image image = Image
                    .builder()
                    .setUrl(response.getUrl())
                    .setInfoUrl(response.getInfoUrl())
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


    public void startSubscribeIfNeed(Set<ImageOuterClass.ImageType> imageTypes, SubcribeImageCallback subcribeImageCallback) {
        assert imageTypes != null && imageTypes.size() > 0;
        if (needToStart(imageTypes)) {
            currentSubscribeImageTypes.clear();
            currentSubscribeImageTypes.addAll(imageTypes);
            asyncStub.withWaitForReady()
                    .subscribeImages(ImageOuterClass.ImageRequest
                                    .newBuilder()
                                    .addAllTypes(imageTypes)
                                    .build()
                            , new SubscribeImagesStreamObserver(subcribeImageCallback)
                    );
            logger.info(String.format("startSubscribeIfNeed imageTypes=[%s]", gson.toJson(imageTypes.toArray())));
        }

    }

    private boolean needToStart(Set<ImageOuterClass.ImageType> imageTypes) {
        boolean result = Sets.difference(currentSubscribeImageTypes, imageTypes).size() > 0 || !isSubscribingImages.get();
        logger.info(String.format("needToStart is %b", result));
        return result;
    }


    public void visit(String imageUrl) {
        ImageOuterClass.VisitRequest visitRequest = ImageOuterClass.VisitRequest.newBuilder().setUrl(imageUrl).build();
        asyncStub.visit(visitRequest, new StreamObserver<ImageOuterClass.VisitResponse>() {
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

    public boolean isHealth() {
        final HealthCheckRequest healthCheckRequest = HealthCheckRequest.newBuilder().setService(ImageGrpc.getServiceDescriptor().getName()).build();
        final HealthGrpc.HealthFutureStub healthFutureStub = HealthGrpc.newFutureStub(channel);
        final HealthCheckResponse.ServingStatus servingStatus;
        try {
            servingStatus = healthFutureStub.check(healthCheckRequest).get().getStatus();
            return HealthCheckResponse.ServingStatus.SERVING.equals(servingStatus);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        int port = 8980;
//        int port = 42420;

        final CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicInteger msgCount = new AtomicInteger(0);

        SubcribeImageCallback subscribeSubcribeImageCallback = (Image imageVo) -> logger.info(
                String.format("subscribeCallback.onImageReceived i=%d imageVo=[%s]",
                        msgCount.addAndGet(1), gson.toJson(imageVo))
        );


        SexyImageClient client = new SexyImageClient("localhost", port);

        if (client.isHealth()) {

            client.startSubscribeIfNeed(getImageTypes(true, false, false, false), subscribeSubcribeImageCallback);
            for (int i = 0; i > -1; i++) {
                client.visit(String.format("%s?%d", "http://images6.fanpop.com/image/photos/36800000/Game-of-Thrones-Season-4-game-of-thrones-36858892-2832-4256.jpg", i));
                Thread.sleep(30000);
                client.startSubscribeIfNeed(getImageTypes(true, false, false, false), subscribeSubcribeImageCallback);
            }
        }
        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);

        client.shutdown();
    }


    @NotNull
    public static Set<ImageOuterClass.ImageType> getImageTypes(boolean normal, boolean poster, boolean sexy, boolean porn) {
        return new HashSet<ImageOuterClass.ImageType>() {
            {
                if (normal) add(ImageOuterClass.ImageType.NORMAL);
                if (poster) add(ImageOuterClass.ImageType.POSTER);
                if (sexy) add(ImageOuterClass.ImageType.SEXY);
                if (porn) add(ImageOuterClass.ImageType.PORN);
            }
        };
    }


} 