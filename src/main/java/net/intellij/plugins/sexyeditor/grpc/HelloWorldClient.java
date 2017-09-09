package net.intellij.plugins.sexyeditor.grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.intellij.plugins.sexyeditor.greeter.GreeterGrpc;
import net.intellij.plugins.sexyeditor.greeter.GreeterOuterClass;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class HelloWorldClient { 
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());
 
    private final ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub blockingStub;
 
    public HelloWorldClient(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
                .usePlaintext(true) 
                .build(); 
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    } 
 
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } 
 
    public String greet(String name) {
        logger.info("Trying to greet " + name);
        try { 
            GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder().setName(name).build();
            GreeterOuterClass.HelloReply response = blockingStub.sayHello(request);
            logger.info("Response: " + response.getMessage());
            return response.getMessage();
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Request to grpc server failed", e);
        }
        return "error greeting";
    } 
 
 
    public static void main(String[] args) throws Exception {
        HelloWorldClient client = new HelloWorldClient("localhost", 42420);
        String name = args.length > 0 ? args[0] : "unknown";
 
        try { 
            client.greet(name);
        } finally { 
            client.shutdown();
        } 
    } 
} 