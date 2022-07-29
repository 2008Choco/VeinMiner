package wtf.choco.veinminer.documentation;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import wtf.choco.veinminer.VeinMiner;
import wtf.choco.veinminer.network.MessageDirection;

/**
 * A program that will generate documentation in markdown format (and HTML tables) for VeinMiner's
 * messaging protocol. The generated markdown is printed to the default output stream, but an argument
 * may be supplied to specify an output file to which the data will be written instead.
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        StringBuilder buffer = new StringBuilder();

        var serverboundDocumentation = generateProtocolTables(MessageDirection.SERVERBOUND);
        var clientboundDocumentation = generateProtocolTables(MessageDirection.CLIENTBOUND);

        buffer.append("""
                # Messaging Protocol

                VeinMiner takes advantage of Minecraft's [custom payload packet](https://wiki.vg/Protocol#Plugin_Message_.28clientbound.29) to send messages to and from the client to interact with an optional cient sided mod. To do this, VeinMiner makes use of a specific sequence of bytes to identify specific messages and the data they contain. The following outlines the communications between client and server for VeinMiner's payloads. Every message is prefixed with a series of bytes representing a [VarInt](https://wiki.vg/Protocol#VarInt_and_VarLong) (as per Minecraft's protocol specification) identifying the message's id, followed by the data displayed in the tables below.

                The messages are sent along the `veinminer:veinminer` channel under current protocol version `%protocol_version%`.

                VeinMiner does take advantage of types similar to that of Minecraft's default protocol which you may read about [here](https://wiki.vg/Protocol#Data_types).

                """
                .replace("%protocol_version%", String.valueOf(VeinMiner.PROTOCOL_VERSION)));

        buffer.append("""
                ## Serverbound

                These are messages sent from the client to the server.

                """);

        serverboundDocumentation.forEach(documentation -> buffer.append(documentation.generateMessageMarkdown() + "\n"));

        buffer.append("""
                ## Clientbound

                These are messages sent by the server to the client.

                """);

        clientboundDocumentation.forEach(documentation -> buffer.append(documentation.generateMessageMarkdown() + "\n"));

        if (args.length >= 1) {
            File outputFile = new File(args[0]);

            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            try (PrintWriter writer = new PrintWriter(outputFile)) {
                writer.write(buffer.toString());
            }
        } else {
            System.out.println(buffer);
        }
    }

    private static List<ProtocolMessageDocumentation> generateProtocolTables(MessageDirection direction) {
        List<ProtocolMessageDocumentation> documentation = new ArrayList<>();

        VeinMiner.PROTOCOL.getPacketRegistry(direction).getRegisteredMessages().forEach((messageClass, messageId) -> {
            ProtocolMessageDocumentation.Builder documentationNodeBuilder = ProtocolMessageDocumentation.builder(direction, messageId);

            for (Method method : messageClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Documentation.class)) {
                    continue;
                }

                if (method.getParameterCount() != 1) {
                    String moreThanOrLessThan = (method.getParameterCount() == 0) ? "less than" : "more than";
                    throw new UnsupportedOperationException("Method " + method.getName() + " in class " + messageClass.getName() + " is annotated with @Documentation but has " + moreThanOrLessThan + " one parameter");
                }

                if (method.getParameterTypes()[0] != ProtocolMessageDocumentation.Builder.class) {
                    throw new UnsupportedOperationException("Method " + method.getName() + " in class " + messageClass.getName() + " is annotated with @Documentation but does not accept parameter of type ProtocolMessageDocumentation.Builder");
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new UnsupportedOperationException("Method " + method.getName() + " in class " + messageClass.getName() + " is annotated with @Documentation but is not static");
                }

                try {
                    method.setAccessible(true);
                    method.invoke(null, documentationNodeBuilder);
                } catch (ReflectiveOperationException e) {
                    throw new UnsupportedOperationException("Something went wrong while invoking " + method.getName() + " in class " + messageClass.getName(), e);
                }

                documentation.add(documentationNodeBuilder.build());
                return; // Return now that we've run the method
            }

            throw new IllegalStateException("Missing documentation method in " + messageClass.getName() + ". Must be static, annotated with @Documentation, and accept one argument of type ProtocolMessageDocumentation.Builder");
        });

        documentation.sort(Comparator.comparing(ProtocolMessageDocumentation::getId));
        return documentation;
    }

}
