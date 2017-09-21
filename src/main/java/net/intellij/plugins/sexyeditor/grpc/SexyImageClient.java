package net.intellij.plugins.sexyeditor.grpc;

import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.Image;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SexyImageClient {
    public interface Callback {
        void onImagemetaReceived(Image image);
    }

    private static final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(SexyImageClient.class.getName());

    public final ManagedChannel channel;
    public final ImageGrpc.ImageStub asyncStub;

    public SexyImageClient(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
                .usePlaintext(true)
                .build();
        asyncStub = ImageGrpc.newStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    AtomicBoolean subscribingToprankImages = new AtomicBoolean(false);

    class ToprankImagesStreamObserver implements StreamObserver<ImageOuterClass.ImageResponse> {
        Callback callback;

        public ToprankImagesStreamObserver(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onNext(ImageOuterClass.ImageResponse response) {
            Image image = Image
                    .builder()
                    .setUrl(response.getUrl())
                    .setInfoUrl(response.getInfoUrl())
                    .setType(response.getType())
                    .build();
            callback.onImagemetaReceived(image);
        }

        @Override
        public void onError(Throwable t) {
            subscribingToprankImages.set(false);
        }

        @Override
        public void onCompleted() {
            subscribingToprankImages.set(false);
        }
    }

    public boolean isSubscribingToprankImages() {
        return subscribingToprankImages.get();
    }

    public void subscribeToprankImages(boolean normal, boolean poster, boolean sexy, boolean porn, Callback callback) {
        List<ImageOuterClass.ImageType> typeList = getImageTypes(normal, poster, sexy, porn);
        if (typeList.size() > 0) {
            logger.info(String.format("subscribeToprankImages normal=%b,poster=%b,sexy=%b,porn=%b", normal, poster, sexy, porn));
            ImageOuterClass.ImageRequest request = ImageOuterClass.ImageRequest
                    .newBuilder()
                    .addAllTypes(typeList)
                    .build();
            subscribingToprankImages.set(true);
            asyncStub.withWaitForReady().subscribeImages(request, new ToprankImagesStreamObserver(callback));
        }
    }

    @NotNull
    private List<ImageOuterClass.ImageType> getImageTypes(boolean normal, boolean poster, boolean sexy, boolean porn) {
        return new ArrayList<ImageOuterClass.ImageType>() {{
            if (normal) this.add(ImageOuterClass.ImageType.NORMAL);
            if (poster) this.add(ImageOuterClass.ImageType.POSTER);
            if (sexy) this.add(ImageOuterClass.ImageType.SEXY);
            if (porn) this.add(ImageOuterClass.ImageType.PORN);
        }};
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

    private static int port = 8980;
    public static void main(String[] args) throws Exception {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicInteger msgCount = new AtomicInteger(0);

        SexyImageClient.Callback subscribeCallback = (Image imageVo) -> logger.info(
                String.format("subscribeCallback.onImagemetaReceived i=%d imageVo=[%s]",
                        msgCount.addAndGet(1), gson.toJson(imageVo))
        );


        SexyImageClient client = new SexyImageClient("localhost", port);

        if (client.isHealth()) {

            client.subscribeToprankImages(true, false, false, false, subscribeCallback);
            for (int i = 0; i > -1; i++) {
                client.visit(String.format("%s?%d", "http://images6.fanpop.com/image/photos/36800000/Game-of-Thrones-Season-4-game-of-thrones-36858892-2832-4256.jpg", i));
                Thread.sleep(30000);

                if (!client.isSubscribingToprankImages()) {
                    client.subscribeToprankImages(true, false, false, false, subscribeCallback);
                }
            }
        }
        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);
        client.shutdown();
    }
} 