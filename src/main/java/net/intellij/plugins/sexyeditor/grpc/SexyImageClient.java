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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SexyImageClient {
    public interface Callback {
        void onImagemetaReceived(Image image);
    }

    private static final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(SexyImageClient.class.getName());

    private final Callback callback;
    private final ManagedChannel channel;
    private final ImageGrpc.ImageStub asyncStub;
    private StreamObserver<ImageOuterClass.ToprankImageRequest> imageRequestStreamObserver = null;

    public SexyImageClient(String hostname, int port, SexyImageClient.Callback callback) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
                .usePlaintext(true)
                .build();
        asyncStub = ImageGrpc.newStub(channel);
        this.callback = callback;
    }

    private StreamObserver<ImageOuterClass.ToprankImageRequest> getImageRequestStreamObserver() {
        if(imageRequestStreamObserver == null) {
            imageRequestStreamObserver =
                    asyncStub.withWaitForReady().listToprankImages(
                            new StreamObserver<ImageOuterClass.ImageResponse>() {
                        @Override
                        public void onNext(ImageOuterClass.ImageResponse response) {
                          Image image =  Image
                                    .builder()
                                    .setUuid(response.getUuid())
                                    .setType(response.getType())
                                    .setUrl(response.getUrl())
                                    .setInfoUrl(response.getInfoUrl())
                                    .build();
                            callback.onImagemetaReceived(image);
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.severe(t.getMessage());
                            imageRequestStreamObserver = null;
                        }

                        @Override
                        public void onCompleted() {
                            logger.info("Completed");
                        }
                    });
        }
        return imageRequestStreamObserver;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public void refreshImages(boolean normal, boolean poster, boolean sexy, boolean porn) {
        List<ImageOuterClass.ImageType> typeList = new ArrayList<ImageOuterClass.ImageType>(){{
            if(normal)this.add(ImageOuterClass.ImageType.NORMAL);
            if(poster)this.add(ImageOuterClass.ImageType.POSTER);
            if(sexy)this.add(ImageOuterClass.ImageType.SEXY);
            if(porn)this.add(ImageOuterClass.ImageType.PORN);
        }};
        ImageOuterClass.ToprankImageRequest request = ImageOuterClass.ToprankImageRequest
                .newBuilder()
                .addAllTypes(typeList)
                .build();

        getImageRequestStreamObserver().onNext(request);

    }

    public boolean isHealth(){
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
        final CountDownLatch finishLatch = new CountDownLatch(1);
        SexyImageClient.Callback callback = new Callback() {
            @Override
            public void onImagemetaReceived(Image imageVo) {
                logger.info(String.format("onImagemetaReceived imageVo=[%s]", gson.toJson(imageVo)));
            }
        };
        SexyImageClient client = new SexyImageClient("localhost", 42420, callback);

        if(client.isHealth()) {
            client.refreshImages(true, false, false, false);
            Thread.sleep(3000);
            client.refreshImages(true, true, false, false);
            Thread.sleep(3000);
            client.refreshImages(true, true, true, false);
            Thread.sleep(3000);
            client.refreshImages(true, true, true, true);

            for (int i = 0; i > -1; i++) {
                logger.info(String.format("\n%d testing reconnect....=============================", i));
                client.refreshImages(true, false, false, false);
                Thread.sleep(5000);
            }
        }
        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);
        client.shutdown();
    }
} 