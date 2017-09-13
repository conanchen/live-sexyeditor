package net.intellij.plugins.sexyeditor.grpc;

import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.intellij.plugins.sexyeditor.database.ImageVo;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SexyImageClient {

    private static final Gson gson = new Gson();

    private static final Logger logger = Logger.getLogger(SexyImageClient.class.getName());

    private final ManagedChannel channel;
    private final ImageGrpc.ImageBlockingStub blockingStub;

    public SexyImageClient(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
                .usePlaintext(true)
                .build();
        blockingStub = ImageGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public List<ImageVo> listImages(String type) {
        List<ImageVo> imageVos = new ArrayList<>();
        logger.info("Trying to get images of  " + type);
        try {
            ImageOuterClass.ImageRequest request = ImageOuterClass.ImageRequest
                    .newBuilder()
                    .setAfterLastUpdated(System.currentTimeMillis()).setPageSize(10)
                    .build();
            Iterator<ImageOuterClass.ImageResponse> responses = blockingStub.listImages(request);
            if (responses != null) {
                while (responses.hasNext()) {
                    ImageOuterClass.ImageResponse response = responses.next();
                    ImageVo imageVo = ImageVo
                            .builder()
                            .setUuid(response.getUuid())
                            .setType(response.getType())
                            .setUrl(response.getUrl())
                            .setEditGroup("*")
                            .build();
                    imageVos.add(imageVo);
                }
            }

        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Request to grpc server failed", e);
        }
        return imageVos;
    }


    public static void main(String[] args) throws Exception {
        SexyImageClient client = new SexyImageClient("localhost", 42420);
        String type = args.length > 0 ? args[0] : "NORMAL";

        try {
            List<ImageVo> responses = client.listImages(type);
            for (int i = 0; i < responses.size(); i++) {
                System.out.println(String.format("%d : imageVo=%s", i, gson.toJson(responses.get(i))));
            }
        } finally {
            client.shutdown();
        }
    }
} 