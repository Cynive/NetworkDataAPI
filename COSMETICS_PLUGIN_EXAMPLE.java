package com.astroid.cosmetics;

import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * VOLLEDIG WERKEND VOORBEELD van een Cosmetics Plugin
 * die NetworkDataAPI gebruikt voor database connecties.
 *
 * VOORDELEN:
 * - Geen eigen MongoDB connectie nodig
 * - Gebruikt de gedeelde connection pool van NetworkDataAPI
 * - Automatische caching van NetworkDataAPI wordt ook gebruikt
 * - Thread-safe operaties
 * - Minder overhead en betere performance
 */
public class CosmeticsPlugin extends JavaPlugin {

    private NetworkDataAPIProvider api;
    private MongoDatabase database;
    private MongoCollection<Document> cosmeticsCollection;
    private MongoCollection<Document> playerCosmeticsCollection;

    @Override
    public void onEnable() {
        // Controleer of NetworkDataAPI beschikbaar is
        if (!APIRegistry.isAvailable()) {
            getLogger().severe("╔════════════════════════════════════════════╗");
            getLogger().severe("║   NetworkDataAPI niet gevonden!           ║");
            getLogger().severe("║   Cosmetics Plugin vereist NetworkDataAPI ║");
            getLogger().severe("╚════════════════════════════════════════════╝");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Haal de API instance op
        api = APIRegistry.getAPI();

        // Verkrijg toegang tot de GEDEELDE database connectie
        // Deze connectie wordt gedeeld met alle andere plugins die NetworkDataAPI gebruiken!
        database = getDatabaseFromAPI();

        // Maak je eigen collections aan voor cosmetics
        cosmeticsCollection = database.getCollection("cosmetics");
        playerCosmeticsCollection = database.getCollection("player_cosmetics");

        getLogger().info("╔════════════════════════════════════════════╗");
        getLogger().info("║   Cosmetics Plugin succesvol gestart!     ║");
        getLogger().info("║   Database: " + database.getName() + "              ║");
        getLogger().info("║   Gebruikt GEDEELDE NetworkDataAPI pool   ║");
        getLogger().info("╚════════════════════════════════════════════╝");

        // Initialiseer standaard cosmetics
        initializeDefaultCosmetics();
    }

    /**
     * Krijg de database via NetworkDataAPI
     * Dit is de GEDEELDE connectie die alle plugins gebruiken!
     */
    private MongoDatabase getDatabaseFromAPI() {
        // Super simpel! Gewoon de getDatabase() method gebruiken
        // Deze database connectie is GEDEELD met alle andere plugins
        // die NetworkDataAPI gebruiken = efficiënt!
        return api.getDatabase();
    }

    /**
     * Initialiseer standaard cosmetics in de database
     */
    private void initializeDefaultCosmetics() {
        // Check of we al cosmetics hebben
        if (cosmeticsCollection.countDocuments() > 0) {
            getLogger().info("Cosmetics database al geïnitialiseerd");
            return;
        }

        // Voeg standaard cosmetics toe
        List<Document> defaultCosmetics = Arrays.asList(
            // Hats
            new Document("_id", "party_hat")
                .append("type", "HAT")
                .append("name", "Party Hat")
                .append("rarity", "RARE")
                .append("price", 1000),

            new Document("_id", "crown")
                .append("type", "HAT")
                .append("name", "Royal Crown")
                .append("rarity", "LEGENDARY")
                .append("price", 5000),

            new Document("_id", "santa_hat")
                .append("type", "HAT")
                .append("name", "Santa Hat")
                .append("rarity", "EPIC")
                .append("price", 2500),

            // Trails
            new Document("_id", "hearts")
                .append("type", "TRAIL")
                .append("name", "Heart Trail")
                .append("rarity", "UNCOMMON")
                .append("price", 500),

            new Document("_id", "flames")
                .append("type", "TRAIL")
                .append("name", "Flame Trail")
                .append("rarity", "RARE")
                .append("price", 1500),

            // Pets
            new Document("_id", "dog")
                .append("type", "PET")
                .append("name", "Loyal Dog")
                .append("rarity", "COMMON")
                .append("price", 300),

            new Document("_id", "dragon")
                .append("type", "PET")
                .append("name", "Baby Dragon")
                .append("rarity", "LEGENDARY")
                .append("price", 10000)
        );

        cosmeticsCollection.insertMany(defaultCosmetics);
        getLogger().info("Standaard cosmetics toegevoegd aan database!");
    }

    /**
     * Haal alle cosmetics van een speler op
     */
    public CompletableFuture<PlayerCosmetics> getPlayerCosmetics(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            Document doc = playerCosmeticsCollection
                .find(Filters.eq("_id", playerUUID.toString()))
                .first();

            if (doc == null) {
                // Maak nieuw profiel voor nieuwe speler
                return createNewPlayerCosmetics(playerUUID);
            }

            return new PlayerCosmetics(doc);
        });
    }

    /**
     * Maak nieuw cosmetics profiel voor speler
     */
    private PlayerCosmetics createNewPlayerCosmetics(UUID playerUUID) {
        Document newProfile = new Document("_id", playerUUID.toString())
            .append("owned", new ArrayList<String>()) // Lijst van owned cosmetic IDs
            .append("equipped", new Document()
                .append("HAT", null)
                .append("TRAIL", null)
                .append("PET", null)
            )
            .append("created", System.currentTimeMillis());

        playerCosmeticsCollection.insertOne(newProfile);
        return new PlayerCosmetics(newProfile);
    }

    /**
     * Koop een cosmetic voor een speler
     */
    public CompletableFuture<PurchaseResult> purchaseCosmetic(Player player, String cosmeticId) {
        return CompletableFuture.supplyAsync(() -> {
            // Haal cosmetic info op
            Document cosmetic = cosmeticsCollection
                .find(Filters.eq("_id", cosmeticId))
                .first();

            if (cosmetic == null) {
                return new PurchaseResult(false, "Cosmetic bestaat niet!");
            }

            int price = cosmetic.getInteger("price", 0);

            // Check of speler genoeg coins heeft (via NetworkDataAPI!)
            // Hier gebruiken we de GEDEELDE PlayerDataService
            var playerData = api.getPlayerDataService();
            Document playerDoc = playerData.getPlayerData(player.getUniqueId());
            int coins = playerDoc.getInteger("coins", 0);

            if (coins < price) {
                return new PurchaseResult(false, "Niet genoeg coins! Je hebt " + price + " coins nodig.");
            }

            // Check of speler cosmetic al heeft
            Document playerCosmetics = playerCosmeticsCollection
                .find(Filters.eq("_id", player.getUniqueId().toString()))
                .first();

            if (playerCosmetics == null) {
                playerCosmetics = createNewPlayerCosmetics(player.getUniqueId()).toDocument();
            }

            @SuppressWarnings("unchecked")
            List<String> owned = (List<String>) playerCosmetics.get("owned");

            if (owned.contains(cosmeticId)) {
                return new PurchaseResult(false, "Je hebt deze cosmetic al!");
            }

            // Voer aankoop uit
            // 1. Trek coins af (via NetworkDataAPI shared connection!)
            playerData.incrementField(player.getUniqueId(), "coins", -price);

            // 2. Voeg cosmetic toe aan owned lijst
            playerCosmeticsCollection.updateOne(
                Filters.eq("_id", player.getUniqueId().toString()),
                Updates.push("owned", cosmeticId)
            );

            return new PurchaseResult(true, "Cosmetic gekocht voor " + price + " coins!");
        });
    }

    /**
     * Equip een cosmetic
     */
    public CompletableFuture<Boolean> equipCosmetic(UUID playerUUID, String cosmeticId) {
        return CompletableFuture.supplyAsync(() -> {
            // Haal cosmetic type op
            Document cosmetic = cosmeticsCollection
                .find(Filters.eq("_id", cosmeticId))
                .first();

            if (cosmetic == null) return false;

            String type = cosmetic.getString("type");

            // Update equipped cosmetic
            playerCosmeticsCollection.updateOne(
                Filters.eq("_id", playerUUID.toString()),
                Updates.set("equipped." + type, cosmeticId)
            );

            return true;
        });
    }

    /**
     * Haal alle beschikbare cosmetics op
     */
    public CompletableFuture<List<Document>> getAllCosmetics() {
        return CompletableFuture.supplyAsync(() -> {
            List<Document> cosmetics = new ArrayList<>();
            cosmeticsCollection.find().forEach(cosmetics::add);
            return cosmetics;
        });
    }

    /**
     * Haal cosmetics op per type
     */
    public CompletableFuture<List<Document>> getCosmeticsByType(String type) {
        return CompletableFuture.supplyAsync(() -> {
            List<Document> cosmetics = new ArrayList<>();
            cosmeticsCollection
                .find(Filters.eq("type", type))
                .forEach(cosmetics::add);
            return cosmetics;
        });
    }

    // Helper classes

    public static class PlayerCosmetics {
        private final Document document;

        public PlayerCosmetics(Document document) {
            this.document = document;
        }

        @SuppressWarnings("unchecked")
        public List<String> getOwnedCosmetics() {
            return (List<String>) document.getOrDefault("owned", new ArrayList<String>());
        }

        public Document getEquipped() {
            return (Document) document.getOrDefault("equipped", new Document());
        }

        public String getEquippedHat() {
            return getEquipped().getString("HAT");
        }

        public String getEquippedTrail() {
            return getEquipped().getString("TRAIL");
        }

        public String getEquippedPet() {
            return getEquipped().getString("PET");
        }

        public boolean owns(String cosmeticId) {
            return getOwnedCosmetics().contains(cosmeticId);
        }

        public Document toDocument() {
            return document;
        }
    }

    public record PurchaseResult(boolean success, String message) {}
}

