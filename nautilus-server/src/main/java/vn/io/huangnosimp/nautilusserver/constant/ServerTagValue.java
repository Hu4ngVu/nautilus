package vn.io.huangnosimp.nautilusserver.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ServerTagValue {
    // ----- FEATURES -----
    POKEMON(TagCategory.FEATURES, "Pokémon"),
    BOSSES(TagCategory.FEATURES, "Bosses"),
    CLASSES(TagCategory.FEATURES, "Classes"),
    CUSTOM_CONTENT(TagCategory.FEATURES, "Custom Content"),
    DUNGEONS(TagCategory.FEATURES, "Dungeons"),
    ECONOMY(TagCategory.FEATURES, "Economy"),
    MEDIA(TagCategory.FEATURES, "Media"),
    OP(TagCategory.FEATURES, "OP"),
    PERSONAL_WORLDS(TagCategory.FEATURES, "Personal Worlds"),
    PLOTS(TagCategory.FEATURES, "Plots"),
    PVE(TagCategory.FEATURES, "PvE"),
    PVP(TagCategory.FEATURES, "PvP"),
    QUESTING(TagCategory.FEATURES, "Questing"),
    TEAMS(TagCategory.FEATURES, "Teams"),

    // ----- GAMEPLAY -----
    ANARCHY(TagCategory.GAMEPLAY, "Anarchy"),
    BATTLE_ROYALE(TagCategory.GAMEPLAY, "Battle Royale"),
    BED_WARS(TagCategory.GAMEPLAY, "Bed Wars"),
    FACTIONS(TagCategory.GAMEPLAY, "Factions"),
    GENS(TagCategory.GAMEPLAY, "Gens"),
    KIT_PVP(TagCategory.GAMEPLAY, "Kit PvP"),
    LIFESTEAL(TagCategory.GAMEPLAY, "Lifesteal"),
    MICROGAMES(TagCategory.GAMEPLAY, "Microgames"),
    MINIGAMES(TagCategory.GAMEPLAY, "Minigames"),
    ONE_BLOCK(TagCategory.GAMEPLAY, "One Block"),
    PARKOUR(TagCategory.GAMEPLAY, "Parkour"),
    PRISON(TagCategory.GAMEPLAY, "Prison"),
    RACING(TagCategory.GAMEPLAY, "Racing"),
    RPG(TagCategory.GAMEPLAY, "RPG"),
    SKYBLOCK(TagCategory.GAMEPLAY, "Skyblock"),
    TOWNS(TagCategory.GAMEPLAY, "Towns"),
    VANILLA_LIKE(TagCategory.GAMEPLAY, "Vanilla Like"),

    // ----- META -----
    ADVENTURE_MODE(TagCategory.META, "Adventure Mode"),
    CREATIVE_MODE(TagCategory.META, "Creative Mode"),
    CROSSPLAY(TagCategory.META, "Crossplay"),
    HARDCORE_MODE(TagCategory.META, "Hardcore Mode"),
    KEEP_INVENTORY(TagCategory.META, "Keep Inventory"),
    NETWORK(TagCategory.META, "Network"),
    OFFLINE_MODE(TagCategory.META, "Offline Mode"),
    SURVIVAL_MODE(TagCategory.META, "Survival Mode"),
    WHITELISTED(TagCategory.META, "Whitelisted"),
    WORLD_RESETS(TagCategory.META, "World Resets"),

    // ----- COMMUNITY -----
    COMPETITIVE(TagCategory.COMMUNITY, "Competitive"),
    CREATOR_COMMUNITY(TagCategory.COMMUNITY, "Creator Community"),
    MMO(TagCategory.COMMUNITY, "MMO"),
    RECORDING_SMP(TagCategory.COMMUNITY, "Recording SMP"),
    ROLEPLAY(TagCategory.COMMUNITY, "Roleplay"),
    SMP(TagCategory.COMMUNITY, "SMP"),
    SOCIAL(TagCategory.COMMUNITY, "Social"),
    TECHNICAL(TagCategory.COMMUNITY, "Technical");

    private final TagCategory category;
    private final String displayName;

    public static Map<TagCategory, List<ServerTagValue>> groupedByCategory() {
        return Arrays.stream(values())
                .collect(Collectors.groupingBy(ServerTagValue::getCategory));
    }

}
