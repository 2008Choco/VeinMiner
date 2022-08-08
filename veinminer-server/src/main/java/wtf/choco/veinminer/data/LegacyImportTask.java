package wtf.choco.veinminer.data;

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

import org.jetbrains.annotations.ApiStatus.Internal;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.platform.PlatformCommandSender;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.EnumUtil;

/**
 * A {@link Runnable} task that will read VeinMiner's playerdata directory and import it
 * into a {@link PersistentDataStorageSQL}.
 * <p>
 * This is an internal class and not meant for public API.
 */
@Internal
public final class LegacyImportTask implements Runnable {

    private final VeinMinerServer veinMiner;
    private final PlatformCommandSender sender;
    private final PersistentDataStorageSQL dataStorage;

    /**
     * Construct a new {@link LegacyImportTask}.
     *
     * @param veinMiner the vein miner instance
     * @param sender the sender to which messages should be sent about the task's progress
     * @param dataStorage the data storage into which data should be imported
     */
    public LegacyImportTask(VeinMinerServer veinMiner, PlatformCommandSender sender, PersistentDataStorageSQL dataStorage) {
        this.veinMiner = veinMiner;
        this.sender = sender;
        this.dataStorage = dataStorage;
    }

    @Override
    public void run() {
        this.sender.sendMessage("Looking for data folder...");

        File jsonStorageDirectory = veinMiner.getPlatform().getConfig().getJsonStorageDirectory();
        if (jsonStorageDirectory == null || !jsonStorageDirectory.isDirectory()) {
            jsonStorageDirectory = new File(veinMiner.getPlatform().getVeinMinerPluginDirectory(), "playerdata");
        }

        if (!jsonStorageDirectory.isDirectory()) {
            this.sender.sendMessage("No data to import.");
            return;
        }

        Gson gson = new Gson();

        List<LegacyPlayerData> legacyPlayerData = new ArrayList<>();
        AtomicInteger failed = new AtomicInteger();

        this.sender.sendMessage("Found legacy data directory (" + jsonStorageDirectory.getName() + "), reading all player data... This might take some time.");

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

                ActivationStrategy activationStrategy = veinMiner.getDefaultActivationStrategy();
                if (object.has("activation_strategy")) {
                    activationStrategy = EnumUtil.get(ActivationStrategy.class, object.get("activation_strategy").getAsString()).orElse(veinMiner.getDefaultActivationStrategy());
                }

                List<VeinMinerToolCategory> disabledCategories = new ArrayList<>();
                if (object.has("disabled_categories")) {
                    object.getAsJsonArray("disabled_categories").forEach(categoryElement -> {
                        String categoryId = categoryElement.getAsString();
                        VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(categoryId);

                        if (category != null) {
                            disabledCategories.add(category);
                        }
                    });
                }

                legacyPlayerData.add(new LegacyPlayerData(playerUUID, activationStrategy, disabledCategories));
            } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
                failed.incrementAndGet();
                this.sender.sendMessage("Could not import the data of " + playerUUID);
                continue;
            }
        }

        this.sender.sendMessage("Done!");
        this.sender.sendMessage("Importing (" + legacyPlayerData.size() + ") players into the database... This might take some time.");

        this.dataStorage.importLegacyData(legacyPlayerData).whenComplete((succeeded, e) -> {
            if (e != null) {
                this.sender.sendMessage("Something went wrong during the import. Check the console for more information.");
                e.printStackTrace();
                return;
            }

            this.sender.sendMessage("Done!");
            this.sender.sendMessage("");
            this.sender.sendMessage("Successfully imported (" + succeeded + ") players into the " + dataStorage.getType().name().toLowerCase() + " database.");
            if (failed.get() > 0) {
                this.sender.sendMessage("(" + failed + ") users failed to import correctly. These users cannot be imported automatically.");
            }
        });
    }

}
