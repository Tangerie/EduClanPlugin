package xyz.tangerie.educlan;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Beacon;
import org.bukkit.persistence.PersistentDataType;

public class Util {
    public static final NamespacedKey BEACON_KEY = new NamespacedKey(EduClan.getPlugin(), "clan");

    public static boolean isBeaconOwned(Beacon bea) {
        return bea.getPersistentDataContainer().has(BEACON_KEY, PersistentDataType.STRING);
    }
}
