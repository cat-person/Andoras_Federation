package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import org.magiclib.util.MagicIncompatibleHullmods;
import java.util.HashSet;
import java.util.Set;

public class Sr_ArmorBlocker extends BaseHullMod {

    // Set to store heavy armor hullmods
    private static final Set<String> HEAVY_ARMOR_HULLMODS = new HashSet<>();

    static {
        // Add all heavy armor hullmods to the set
        HEAVY_ARMOR_HULLMODS.add("ugh_phasearmor");
        HEAVY_ARMOR_HULLMODS.add("sikr_armor");
        HEAVY_ARMOR_HULLMODS.add("ablative_armor");
        HEAVY_ARMOR_HULLMODS.add("roider_heavyarmor");
        HEAVY_ARMOR_HULLMODS.add("CJHM_reflectivearmor");
        HEAVY_ARMOR_HULLMODS.add("BT_AntiEmpArmor");
        HEAVY_ARMOR_HULLMODS.add("ass_AshbornApplique");
        HEAVY_ARMOR_HULLMODS.add("arswp_turtleback_armor");
        HEAVY_ARMOR_HULLMODS.add("tahlan_outcast_engineering");
        HEAVY_ARMOR_HULLMODS.add("tahlan_stealthplating");
        HEAVY_ARMOR_HULLMODS.add("tahlan_daemonplating");
        HEAVY_ARMOR_HULLMODS.add("tahlan_daemonarmor");
        HEAVY_ARMOR_HULLMODS.add("TADA_lightArmor");
        HEAVY_ARMOR_HULLMODS.add("uaf_disposable_armor");
        HEAVY_ARMOR_HULLMODS.add("SKR_ancientArmor");
        HEAVY_ARMOR_HULLMODS.add("SCY_armorplating");
        HEAVY_ARMOR_HULLMODS.add("bc_ferroplating");
        HEAVY_ARMOR_HULLMODS.add("istl_bbdefense");
        HEAVY_ARMOR_HULLMODS.add("istl_DMEbalericarmor");
        HEAVY_ARMOR_HULLMODS.add("hmp_traxymiumarmor");
        HEAVY_ARMOR_HULLMODS.add("hmp_crystalizedarmor");
        HEAVY_ARMOR_HULLMODS.add("hmp_regenerativearmor");
        HEAVY_ARMOR_HULLMODS.add("ugh_spongearmor");
        HEAVY_ARMOR_HULLMODS.add("apex_armor");
        HEAVY_ARMOR_HULLMODS.add("nskr_criticalArmor");
        HEAVY_ARMOR_HULLMODS.add("aux_ablative_armor");
        HEAVY_ARMOR_HULLMODS.add("eis_damperhull");
        HEAVY_ARMOR_HULLMODS.add("mhmods_integratedarmor");
        HEAVY_ARMOR_HULLMODS.add("CJHM_beltarmor");
        HEAVY_ARMOR_HULLMODS.add("CJHM_salvagedarmor");
        HEAVY_ARMOR_HULLMODS.add("CJHM_ablativearmor");
        HEAVY_ARMOR_HULLMODS.add("heavyarmor");
        HEAVY_ARMOR_HULLMODS.add("armoredweapons");
        HEAVY_ARMOR_HULLMODS.add("shield_shunt");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        // Iterate over each heavy armor hullmod and remove it if present
        for (String hullMod : HEAVY_ARMOR_HULLMODS) {
            if (stats.getVariant().getHullMods().contains(hullMod)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), hullMod, "sr_ablative_armor");
            }
        }
    }
}
