package net.joefoxe.hexerei.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class HexereiSupporterBenefits {

    public static UUID getUuid(String uuid) {
        UUID id = UUID.fromString(uuid);
        return id;
    }

    public static final UUID JOE = getUuid("1241d572-92a5-4b6b-a650-a5272139e52a");
    public static final UUID DEV = getUuid("380df991-f603-344c-a090-369bad2a924a");
    public static final UUID KAUPENJOE = getUuid("665ad10f-5548-4985-986b-b642732ddce0");
    public static final UUID WILLTARAX = getUuid("7c78529d-d1a6-4564-ba45-35d8892de403");
    public static final UUID ALEC = getUuid("490255a6-f8e3-402a-8e22-22ee95c4b9f6");
    public static final UUID SAPHRYM = getUuid("71b78099-ad3d-42bd-ae79-7cc51fd51860");
    public static final UUID GWEN_WAV = getUuid("476facfc-a8eb-452d-a8a2-7ccd50eae6cd");
    public static final UUID HOWL = getUuid("af47ed24-0e01-43e4-9c8b-b9cec037a333");
    public static final UUID SHY = getUuid("7135da42-d327-47bb-bb04-5ba4e212fb32");
    public static final UUID HERBAL_FAT_CAT = getUuid("77a460e2-1a01-4798-ba76-5eeecad471b2");
    public static final UUID CYROIL = getUuid("4ce55e73-a07e-4b7e-aec8-39e5e9d4d72a");
    public static final UUID WAVECLAW = getUuid("02320102-6fc4-408b-8873-afe54b2dcae6");
    public static final UUID KAIJA1624 = getUuid("2c67d443-185e-422b-8d1b-f6e3d3c393c1");
    public static final UUID SLEEPY = getUuid("fbe0ad6b-f68c-408b-b4ef-343a48ff4b20");
    public static final UUID MIZU = getUuid("8bbf2330-78d1-4ea7-945f-89b7a99298c2");

    public static final Collection<UUID> supporters = new ArrayList<>(Arrays.asList(JOE, DEV, KAUPENJOE, WILLTARAX, ALEC, SAPHRYM, GWEN_WAV, HOWL, SHY, HERBAL_FAT_CAT, CYROIL, WAVECLAW, KAIJA1624, SLEEPY, MIZU));

    public static boolean matchesSupporterUUID(UUID uuid){

        for(UUID supporter_uuid : supporters){
            if(uuid.equals(supporter_uuid))
                return true;
        }

        return false;
    }

}
