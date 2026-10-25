package vn.io.huangnosimp.nautilusserver.server.constants;

import java.util.Map;
import java.util.Set;

public class Tag {
    public static class Feature {
        public static final String POKEMON = "Pokemon";
        public static final String BOSSES = "Bosses";
        public static final String CLASSES = "Classes";
        public static final String CUSTOM_CONTENT = "Custom Content";
        public static final String DUNGEONS = "Dungeons";
        public static final String ECONOMY = "Economy";
        public static final String MEDIA = "Media";
        public static final String OP = "OP";
        public static final String PERSONAL_WORLDS = "Personal Worlds";
        public static final String PLOTS = "Plots";
        public static final String PVE = "PvE";
        public static final String PVP = "PvP";
        public static final String QUESTING = "Questing";
        public static final String TEAMS = "Teams";
    }

    public static class Gameplay {
        public static final String ANARCHY = "Anarchy";
        public static final String BATTLE_ROYALE = "Battle Royale";
        public static final String BED_WARS = "Bed Wars";
        public static final String FACTIONS = "Factions";
        public static final String GENS = "Gens";
        public static final String KIT_PVP = "Kit PvP";
        public static final String LIFESTEAL = "Lifesteal";
        public static final String MICROGAMES = "Microgames";
        public static final String MINIGAMES = "Minigames";
        public static final String ONE_BLOCK = "One Block";
        public static final String PARKOUR = "Parkour";
        public static final String PRISON = "Prison";
        public static final String RACING = "Racing";
        public static final String RPG = "RPG";
        public static final String SKYBLOCK = "Skyblock";
        public static final String TOWNS = "Towns";
        public static final String VANILLA_LIKE = "Vanilla Like";
    }

    public static class Meta {
        public static final String ADVENTURE_MODE = "Adventure Mode";
        public static final String CREATIVE_MODE = "Creative Mode";
        public static final String CROSSPLAY = "Crossplay";
        public static final String HARDCORE_MODE = "Hardcore Mode";
        public static final String KEEP_INVENTORY = "Keep Inventory";
        public static final String NETWORK = "Network";
        public static final String OFFLINE_MODE = "Offline Mode";
        public static final String SURVIVAL_MODE = "Survival Mode";
        public static final String WHITELISTED = "Whitelisted";
        public static final String WORLD_RESETS = "World Resets";
    }

    public static class Community {
        public static final String COMPETITIVE = "Competitive";
        public static final String CREATOR_COMMUNITY = "Creator Community";
        public static final String MMO = "MMO";
        public static final String RECORDING_SMP = "Recording SMP";
        public static final String ROLEPLAY = "Roleplay";
        public static final String SMP = "SMP";
        public static final String SOCIAL = "Social";
        public static final String TECHNICAL = "Technical";
    }

    private static final Map<TagCategory, Set<String>> tagCategorySetMap =
            Map.of(
                    TagCategory.FEATURES,
                            Set.of(
                                    Feature.POKEMON,
                                    Feature.BOSSES,
                                    Feature.CLASSES,
                                    Feature.CUSTOM_CONTENT,
                                    Feature.DUNGEONS,
                                    Feature.ECONOMY,
                                    Feature.MEDIA,
                                    Feature.OP,
                                    Feature.PERSONAL_WORLDS,
                                    Feature.PLOTS,
                                    Feature.PVE,
                                    Feature.PVP,
                                    Feature.QUESTING,
                                    Feature.TEAMS),
                    TagCategory.GAMEPLAY,
                            Set.of(
                                    Gameplay.ANARCHY,
                                    Gameplay.BATTLE_ROYALE,
                                    Gameplay.BED_WARS,
                                    Gameplay.FACTIONS,
                                    Gameplay.GENS,
                                    Gameplay.KIT_PVP,
                                    Gameplay.LIFESTEAL,
                                    Gameplay.MICROGAMES,
                                    Gameplay.MINIGAMES,
                                    Gameplay.ONE_BLOCK,
                                    Gameplay.PARKOUR,
                                    Gameplay.PRISON,
                                    Gameplay.RACING,
                                    Gameplay.RPG,
                                    Gameplay.SKYBLOCK,
                                    Gameplay.TOWNS,
                                    Gameplay.VANILLA_LIKE),
                    TagCategory.META,
                            Set.of(
                                    Meta.ADVENTURE_MODE,
                                    Meta.CREATIVE_MODE,
                                    Meta.CROSSPLAY,
                                    Meta.HARDCORE_MODE,
                                    Meta.KEEP_INVENTORY,
                                    Meta.NETWORK,
                                    Meta.OFFLINE_MODE,
                                    Meta.SURVIVAL_MODE,
                                    Meta.WHITELISTED,
                                    Meta.WORLD_RESETS),
                    TagCategory.COMMUNITY,
                            Set.of(
                                    Community.COMPETITIVE,
                                    Community.CREATOR_COMMUNITY,
                                    Community.MMO,
                                    Community.RECORDING_SMP,
                                    Community.ROLEPLAY,
                                    Community.SMP,
                                    Community.SOCIAL,
                                    Community.TECHNICAL));

    public static Map<TagCategory, Set<String>> getTagMap() {
        return tagCategorySetMap;
    }
}
