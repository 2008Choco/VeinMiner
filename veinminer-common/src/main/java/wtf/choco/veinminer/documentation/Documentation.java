package wtf.choco.veinminer.documentation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import wtf.choco.network.Message;

/**
 * A method annotation to denote the documentation method of a plugin message.
 * <p>
 * Every registered implementation of {@link Message} should have a static annotated method
 * that accepts a {@link ProtocolMessageDocumentation.Builder} argument. The method may be
 * private, but it <strong>must</strong> be static.
 * <p>
 * It is expected for these methods to edit the builder to contain information pertaining to
 * the enclosing class' responsibility in the protocol.
 * <p>
 * An example method may look like the following:
 * <pre>
 * public final class PluginMessageClientboundCustomData implements PluginMessage{@literal<ClientboundPluginMessageListener>} {
 *
 *     // The actual plugin message implementation, etc.
 *
 *     {@literal @Documentation}
 *     private static void document(ProtocolMessageDocumentation.Builder documentation) {
 *         documentation.name("Custom Data Message")
 *             .description("This is the message description. This is what it does.")
 *             .field(MessageField.TYPE_VARINT, "Name of Data", "This is a var int that gets sent because it would be cool")
 *             .field(MessageField.TYPE_BLOCK_POSITION, "The Block Position", "This is a block position that is sent because we need it");
 *     }
 *
 * }
 * </pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Documentation { }
