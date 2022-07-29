package wtf.choco.veinminer.documentation;

import java.util.function.Function;

/**
 * Represents a field in a protocol message.
 *
 * @param expectedType the expected type name (constants found in this class)
 * @param name the name of the field
 * @param description the description of the field
 */
public record MessageField(String expectedType, String name, String description) {

    public static final String TYPE_BOOLEAN = "Boolean";
    public static final String TYPE_BYTE = "Byte";
    public static final String TYPE_VARINT = "VarInt";

    public static final String TYPE_BLOCK_POSITION = "BlockPosition";
    public static final String TYPE_NAMESPACED_KEY = "NamespacedKey";

    public static final Function<String, String> TYPE_ARRAY_OF = value -> "Array of " + value;

    /**
     * Generate an HTML table that represents a simple bitmask.
     * <p>
     * For instance, the following:
     * <pre>
     * MessageField.bitmask(
     *     "The least significant bit description",
     *     "The second bit in the field",
     *     "The third bit in the field",
     *     "The final, most significant bit in the field
     * );
     * </pre>
     * will generate the table below:
     * <table>
     * <thead>
     *     <th>Byte</th>
     *     <th>Meaning</th>
     * </thead>
     * <tbody>
     *     <tr>
     *         <td>0x1</td>
     *         <td>The least significant bit description</td>
     *     </tr>
     *     <tr>
     *         <td>0x2</td>
     *         <td>The second bit in the field</td>
     *     </tr>
     *     <tr>
     *         <td>0x4</td>
     *         <td>The third bit in the field</td>
     *     </tr>
     *     <tr>
     *         <td>0x8</td>
     *         <td>The final, most significant bit in the field</td>
     *     </tr>
     * </tbody>
     * </table>
     *
     * @param descriptions the descriptions in order of least significant bit to
     * most significant bit
     *
     * @return the bitmask table
     */
    public static final String bitmask(String... descriptions) {
        StringBuilder builder = new StringBuilder("""
                <table>
                <thead>
                    <th>Byte</th>
                    <th>Meaning</th>
                </thead>
                <tbody>
                """);

        int position = 1;
        for (int i = 0; i < descriptions.length; i++) {
            builder.append("<tr>".indent(4));
            builder.append("        <td>0x").append(Integer.toHexString(position << i)).append("</td>\n");
            builder.append("        <td>").append(descriptions[i]).append("</td>\n");
            builder.append("</tr>".indent(4));
        }

        builder.append("""
                </tbody>
                </table>""");

        return builder.toString();
    }

}
