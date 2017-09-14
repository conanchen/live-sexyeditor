package net.intellij.plugins.sexyeditor.grpc;

import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.ImageVo;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SexyImageClient {
    public interface Callback {
        void onImagemetaReceived(ImageVo imageVo);
    }

    private static final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(SexyImageClient.class.getName());

    private final Callback callback;
    private final ManagedChannel channel;
    private final ImageGrpc.ImageBlockingStub blockingStub;
    private final ImageGrpc.ImageStub asyncStub;
    private StreamObserver<ImageOuterClass.ImageRequest> imageRequestStreamObserver = null;

    public SexyImageClient(String hostname, int port, SexyImageClient.Callback callback) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
                .usePlaintext(true)
                .build();
        blockingStub = ImageGrpc.newBlockingStub(channel);
        asyncStub = ImageGrpc.newStub(channel);
        this.callback = callback;
    }

    private StreamObserver<ImageOuterClass.ImageRequest> getImageRequestStreamObserver() {
        if(imageRequestStreamObserver == null) {
            imageRequestStreamObserver =
                    asyncStub.withWaitForReady().listMessages(new StreamObserver<ImageOuterClass.ImageResponse>() {
                        @Override
                        public void onNext(ImageOuterClass.ImageResponse response) {
                          ImageVo imageVo =  ImageVo
                                    .builder()
                                    .setUuid(response.getUuid())
                                    .setType(response.getType())
                                    .setUrl(response.getUrl())
                                    .setInfoUrl(response.getInfoUrl())
                                    .build();
                            callback.onImagemetaReceived(imageVo);
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
        logger.info(String.format("normal=%b,poster=%b,sexy=%b,porn=%b", normal, poster, sexy, porn));
        ImageOuterClass.ImageRequest request = ImageOuterClass.ImageRequest
                .newBuilder()
                .setNormal(normal)
                .setPoster(poster)
                .setSexy(sexy)
                .setPorn(porn)
                .build();

        getImageRequestStreamObserver().onNext(request);

    }

    public static void main(String[] args) throws Exception {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        SexyImageClient.Callback callback = new Callback() {
            @Override
            public void onImagemetaReceived(ImageVo imageVo) {
                logger.info(String.format("onImagemetaReceived imageVo=[%s]", gson.toJson(imageVo)));
            }
        };
        SexyImageClient client = new SexyImageClient("localhost", 42420, callback);

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
        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);
        client.shutdown();
    }
} 