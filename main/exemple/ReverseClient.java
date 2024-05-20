
import exemple.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReverseClient {

    private static final Logger logger = Logger.getLogger(ReverseClient.class.getName());

    private final ReverseGrpc.ReverseBlockingStub blockingStub;

    /** Construct client for accessing Reverse server using the existing channel. */
    public ReverseClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = ReverseGrpc.newBlockingStub(channel);
    }

    /** Reverse a message with additional information. */
    public void reverse(String message, String firstName, String cin) {
        logger.info("Will try to reverse message: " + message + ", with firstName: " + firstName + ", and CIN: " + cin);
        ReverseRequest request = ReverseRequest.newBuilder()
                                                .setMessage(message)
                                                .setFirstName(firstName)
                                                .setCin(cin)
                                                .build();
        ReverseReply response;
        try {
            response = blockingStub.reverseString(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Reversed message: " + response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        String message = "Hello";
        String firstName = "John";
        String cin = "12345678";
        String target = "localhost:50051";

        if (args.length > 0 && "--help".equals(args[0])) {
            System.err.println("Usage: [message [firstName [cin [target]]]]");
            System.err.println("");
            System.err.println("  message   The message you wish to reverse. Defaults to \"" + message + "\"");
            System.err.println("  firstName The first name associated with the message. Defaults to \"" + firstName + "\"");
            System.err.println("  cin       The CIN associated with the message. Defaults to \"" + cin + "\"");
            System.err.println("  target    The server to connect to. Defaults to \"" + target + "\"");
            System.exit(1);
        }

        if (args.length > 0) {
            message = args[0];
        }
        if (args.length > 1) {
            firstName = args[1];
        }
        if (args.length > 2) {
            cin = args[2];
        }
        if (args.length > 3) {
            target = args[3];
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        try {
            ReverseClient client = new ReverseClient(channel);
            client.reverse(message, firstName, cin);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
