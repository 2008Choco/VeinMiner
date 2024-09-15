package wtf.choco.veinminer.data;

import com.google.common.base.Enums;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus.Internal;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.language.LanguageFile;
import wtf.choco.veinminer.language.LanguageKeys;
import wtf.choco.veinminer.player.ActivationStrategy;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;

/**
 * A {@link Runnable} task that will read VeinMiner's playerdata directory and import it
 * into a {@link LegacyImportable} type.
 * <p>
 * This is an internal class and not meant for public API.
 */
@Internal
public final class LegacyImportTask implements Runnable {

    private final VeinMinerPlugin plugin;
    private final CommandSender sender;
    private final LegacyImportable importable;
    private final String targetName;

    /**
     * Construct a new {@link LegacyImportTask}.
     *
     * @param plugin the vein miner instance
     * @param sender the sender to which messages should be sent about the task's progress
     * @param importable the data storage into which data should be imported
     * @param targetName the name of the target importable data structure
     */
    public LegacyImportTask(VeinMinerPlugin plugin, CommandSender sender, LegacyImportable importable, String targetName) {
        this.plugin = plugin;
        this.sender = sender;
        this.importable = importable;
        this.targetName = targetName;
    }

    @Override
    public void run() {
        LanguageFile language = plugin.getLanguage();
        language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_SEARCHING);

        File jsonStorageDirectory = plugin.getConfiguration().getJsonStorageDirectory();
        if (jsonStorageDirectory == null || !jsonStorageDirectory.isDirectory()) {
            jsonStorageDirectory = new File(plugin.getDataFolder(), "playerdata");
        }

        if (!jsonStorageDirectory.isDirectory()) {
            language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_NO_DATA);
            return;
        }

        Gson gson = new Gson();

        List<LegacyPlayerData> legacyPlayerData = new ArrayList<>();
        AtomicInteger failed = new AtomicInteger();

        language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_FOUND, jsonStorageDirectory.getName());

        for (File file : jsonStorageDirectory.listFiles((dir, name) -> name.endsWith(".json"))) {
            String fileName = file.getName();
            UUID playerUUID;

            try {
                playerUUID = UUID.fromString(fileName.substring(0, fileName.indexOf('.')));
            } catch (IllegalArgumentException e) {
                failed.incrementAndGet();
                continue;
            }

            try {
                JsonObject object = gson.fromJson(new FileReader(file), JsonObject.class);

                ActivationStrategy activationStrategy = plugin.getConfiguration().getDefaultActivationStrategy();
                if (object.has("activation_strategy")) {
                    activationStrategy = Enums.getIfPresent(ActivationStrategy.class, object.get("activation_strategy").getAsString()).or(activationStrategy);
                }

                List<VeinMinerToolCategory> disabledCategories = new ArrayList<>();
                if (object.has("disabled_categories")) {
                    object.getAsJsonArray("disabled_categories").forEach(categoryElement -> {
                        String categoryId = categoryElement.getAsString();
                        VeinMinerToolCategory category = plugin.getToolCategoryRegistry().get(categoryId);

                        if (category != null) {
                            disabledCategories.add(category);
                        }
                    });
                }

                legacyPlayerData.add(new LegacyPlayerData(playerUUID, activationStrategy, disabledCategories));
            } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
                failed.incrementAndGet();
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_FAIL_PLAYER, playerUUID.toString());
                continue;
            }
        }

        language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_DONE);
        language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_IMPORTING, legacyPlayerData.size());

        this.importable.importLegacyData(legacyPlayerData).whenComplete((succeeded, e) -> {
            if (e != null) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_FAIL_UNKNOWN);
                e.printStackTrace();
                return;
            }

            language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_DONE);
            this.sender.sendMessage("");
            language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_SUCCESS, succeeded, targetName.toLowerCase());
            if (failed.get() > 0) {
                language.send(sender, LanguageKeys.COMMAND_VEINMINER_IMPORT_SUCCESS_FAILURES, failed);
            }
        });
    }

}
