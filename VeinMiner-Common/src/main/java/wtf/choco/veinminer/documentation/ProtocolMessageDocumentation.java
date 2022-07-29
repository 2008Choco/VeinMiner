package wtf.choco.veinminer.documentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wtf.choco.veinminer.network.MessageDirection;

/**
 * Represents documentation for a protocol message.
 */
public final class ProtocolMessageDocumentation {

    private final MessageDirection direction;

    private final String name;
    private final String description;
    private final int id;
    private final List<MessageField> fields;

    private ProtocolMessageDocumentation(MessageDirection direction, int id, String name, String description, List<MessageField> fields) {
        this.direction = direction;
        this.id = id;
        this.name = name;
        this.description = description;
        this.fields = Collections.unmodifiableList(fields);
    }

    /**
     * Get the {@link MessageDirection} of this message.
     *
     * @return the direction
     */
    public MessageDirection getDirection() {
        return direction;
    }

    /**
     * Get the unique id of this message.
     *
     * @return the unique id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the name of this message.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of this message.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get a list of all {@link MessageField fields} in this message.
     *
     * @return the fields
     */
    public List<MessageField> getFields() {
        return fields;
    }

    /**
     * Generate a Markdown-friendly formatted string containing the name, description, and
     * {@link #generateMessageHTMLTable() HTML table} of this message.
     *
     * @return the message Markdown
     */
    public String generateMessageMarkdown() {
        StringBuilder builder = new StringBuilder("### ");

        builder.append(name).append("\n\n");
        builder.append(description).append("\n");
        builder.append(generateMessageHTMLTable());

        return builder.toString();
    }

    /**
     * Generate an HTML table containing all the fields in this message and their information.
     *
     * @return the message HTML table
     */
    public String generateMessageHTMLTable() {
        StringBuilder builder = new StringBuilder();

        int fieldCount = fields.size();

        builder.append("""
               <table>
               <thead>
                   <tr>
                       <th>Packet ID</th>
                       <th>Bound To</th>
               """);

        // If there are any fields, we need more table headers to declare their names, types, and notes
        if (fieldCount > 0) {
            builder.append("""
                    <th>Field Name</th>
                    <th>Field Type</th>
                    <th>Notes</th>
                    """.indent(8));
        }

        builder.append("""
                   </tr>
               </thead>
               <tbody>
                   <tr>
                """);

        /*
         * Generate the first two columns of <td>, the message id and direction. These are only defined once.
         *
         * The "rowspan" will depend on how many fields are present in the table, because for each field, a new row
         * is generated. If there is only one field (or none), we do not need to set the rowspan because it defaults to 1
         */
        builder.append("        <td").append(fieldCount > 1 ? " rowspan=" + fieldCount + ">" : ">").append("0x").append(Integer.toHexString(id)).append("</td>\n");
        builder.append("        <td").append(fieldCount > 1 ? " rowspan=" + fieldCount + ">" : ">").append(getDirectionName(direction)).append("</td>\n");

        if (fieldCount == 0) {
            // If there are no fields, we'll just close the table row and continue
            builder.append("</tr>".indent(4));
        } else {
            for (int i = 0; i < fields.size(); i++) {
                // The first field is handled specially because it resides in the already open <tr> block and therefore does not need to be opened
                if (i > 0) {
                    builder.append("<tr>".indent(4));
                }

                MessageField field = fields.get(i);
                builder.append("        <td>").append(field.name()).append("</td>\n");
                builder.append("        <td>").append(field.expectedType()).append("</td>\n");
                builder.append("        <td>").append(field.description()).append("</td>\n");

                builder.append("</tr>".indent(4));
            }
        }

        builder.append("""
                </tbody>
                </table>
                """);

        return builder.toString();
    }

    private String getDirectionName(MessageDirection direction) {
        return switch (direction) {
            case CLIENTBOUND -> "Client";
            case SERVERBOUND -> "Server";
        };
    }

    /**
     * Create a new {@link ProtocolMessageDocumentation.Builder}.
     *
     * @param direction the direction of the message being documented
     * @param messageId the id of the message being documented
     *
     * @return the builder instance
     */
    public static Builder builder(MessageDirection direction, int messageId) {
        return new ProtocolMessageDocumentation.Builder(direction, messageId);
    }

    /**
     * A builder for {@link ProtocolMessageDocumentation} instances.
     */
    public static final class Builder {

        private String name;
        private String description;
        private List<MessageField> fields = new ArrayList<>();

        private final MessageDirection direction;
        private final int id;

        private Builder(MessageDirection direction, int id) {
            this.direction = direction;
            this.id = id;
        }

        /**
         * Set the name of the protocol message.
         *
         * @param name the name to set
         *
         * @return this instance. Allows for chained method calls
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the description of the protocol message.
         *
         * @param description the description to set
         *
         * @return this instance. Allows for chained method calls
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Add a new field to the protocol message.
         * <p>
         * The order of invocation of this method is significant. Fields added first will be
         * listed first in the table.
         *
         * @param expectedType the expected field type (see {@link MessageField} for constants)
         * @param name the name of the field
         * @param description the description of the field
         *
         * @return this instance. Allows for chained method calls
         */
        public Builder field(String expectedType, String name, String description) {
            this.fields.add(new MessageField(expectedType, name, description));
            return this;
        }

        /**
         * Construct a new {@link ProtocolMessageDocumentation} from the information supplied
         * by this builder.
         *
         * @return the built documentation
         */
        ProtocolMessageDocumentation build() {
            return new ProtocolMessageDocumentation(direction, id, name, description, fields);
        }

    }

}
