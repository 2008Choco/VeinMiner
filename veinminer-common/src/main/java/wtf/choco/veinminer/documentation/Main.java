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
import wtf.choco.veinminer.network.PluginMessageProtocol;

/**
 * A program that will generate documentation in markdown format (and HTML tables) for VeinMiner's
 * messaging protocol. The generated markdown is printed to the default output stream, but an argument
 * may be supplied to specify an output file to which the data will be written instead.
 */
public final class Main {

    /**
     * The entry point of the program.
     * <p>
     * Generates a Markdown-formatted string documenting VeinMiner's protocol. Documentation for each
     * individual packet is determined by the {@link Documentation} annotation (see the annotation docs
     * for more information).
     *
     * @param args arguments
     *
     * @throws Exception if an exception occurs
     */
    public static void main(String[] args) throws Exception {
        StringBuilder buffer = new StringBuilder();

        PluginMessageProtocol protocol = VeinMiner.PROTOCOL;

        var serverboundDocumentation = generateProtocolTables(protocol, MessageDirection.SERVERBOUND);
        var clientboundDocumentation = generateProtocolTables(protocol, MessageDirection.CLIENTBOUND);

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

    private static List<ProtocolMessageDocumentation> generateProtocolTables(PluginMessageProtocol protocol, MessageDirection direction) {
        List<ProtocolMessageDocumentation> documentation = new ArrayList<>();

        protocol.getPacketRegistry(direction).getRegisteredMessages().forEach((messageClass, messageId) -> {
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
