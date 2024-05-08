package data.dcr.andorasfederation;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides methods that callers can use to offer hints to the Detailed Combat Results mod for how
 * damages should be attributed.
 * Serializes DamageReport data onto the data bus that is `Global.getCombatEngine().getCustomData()`
 * No actual custom objects are shared between mods to avoid strange dependency issues.
 *
 * DamageReportManager is a facade over the data bus for ease of use.
 */
public class DamageReportManagerV1 {

    private static final String DamageReportManagerKey = "DamageReportManagerV1";

    //only log if dcr is enabled
    private static final boolean dcrEnabled =  Global.getSettings().getModManager().isModEnabled("DetailedCombatResults");

    private final static Logger log = Global.getLogger(DamageReportManagerV1.class);

    private static List<Object[]> getDamageReportStream(){
        Map<String, Object> customData = Global.getCombatEngine().getCustomData();
        Object raw = customData.get(DamageReportManagerKey);

        if(raw == null){
            raw = new ArrayList<Object[]>(200);
            customData.put(DamageReportManagerKey, raw);
        }

        if(!(raw instanceof List)) {
            throw new RuntimeException("Unknown class for CustomDataKey: '"+ DamageReportManagerKey +"' class: '"+raw.getClass() +"'");
        }

        List<Object[]> ret = (List<Object[]>) raw;

        // if there's more than 1k objects in this, something has gone wrong so clear it to prevent memory issues
        if(ret.size() > 1000){
            ret.clear();
        }

        return ret;
    }

    /**
     * Gives additional "clarifying" information to the Detailed Combat Results damage processor.  Specifically
     * the information necessary to determine exactly how much damage was done by a given weapon.  Use this method
     * in conjunction with calls that instruct the game engine to apply damage to a ship, such as:
     * * CombatEngineAPI.spawnEmpArc
     * * CombatEngineAPI.spawnEmpArcPierceShields
     * * CombatEngineAPI.applyDamage
     * @param shipDamage The amount of damage done to the ship without regard to it being armor or hull
     * @param empDamage How much EMP damage was done
     * @param damageType The damage type
     * @param source What ship originated the damage
     * @param target Who was the damage done to
     * @param weaponName What was the name of the weapon that did the damage (this will be used in the UI)
     */
    public static void addDamageClarification(float shipDamage, float empDamage, DamageType damageType, CombatEntityAPI source, CombatEntityAPI target, String weaponName){
        try {
            if(dcrEnabled) {
                getDamageReportStream().add(new Object[]{shipDamage, empDamage, damageType, source, target, weaponName});
            }
        } catch (Exception e){
            log.warn("Error adding damage report", e);
        }
    }

    public static void addDamageClarification(float shipDamage, float empDamage, DamagingProjectileAPI projectile){
        try {
            addDamageClarification(shipDamage, empDamage, projectile.getDamageType(), projectile.getSource(), projectile.getDamageTarget(), projectile.getWeapon().getDisplayName());
        } catch (Exception e){
            log.warn("Error adding damage report", e);
        }
    }

    /**
     * Used when ship health related values are altered outside the context of the CombatEngine
     * (things done in the context of the combat engine are caught by the listener and processed differently)
     * Specifically things like directly apply EMP/Armor/Hull damage
     * @param armorDamage
     * @param hullDamage
     * @param empDamage
     * @param shieldDamage
     * @param damageType The damage type
     * @param source What ship originated the damage
     * @param target Who was the damage done to
     * @param weaponName What was the name of the weapon that did the damage (this will be used in the UI)
     */
    public static void addDamageReport(float armorDamage, float hullDamage, float empDamage, float shieldDamage, DamageType damageType, CombatEntityAPI source, CombatEntityAPI target, String weaponName) {
        try{
            if(dcrEnabled) {
                getDamageReportStream().add(new Object[]{armorDamage, hullDamage, empDamage, shieldDamage, damageType, source, target, weaponName});
            }
        } catch (Exception e){
            log.warn("Error adding damage report", e);
        }
    }

    public static void addDamageReport(float armorDamage, float hullDamage, float empDamage, float shieldDamage, DamagingProjectileAPI projectile) {
        try {
            addDamageReport(armorDamage, hullDamage, empDamage, shieldDamage, projectile.getDamageType(), projectile.getSource(), projectile.getDamageTarget(), projectile.getWeapon().getDisplayName());
        } catch (Exception e){
            log.warn("Error adding damage report", e);
        }
    }

    public static void addDamageReport(float armorDamage, float hullDamage, float empDamage, float shieldDamage, BeamAPI beam) {
        try{
            addDamageReport(armorDamage, hullDamage, empDamage, shieldDamage, beam.getDamage().getType(), beam.getSource(), beam.getDamageTarget(), beam.getWeapon().getDisplayName());
        } catch (Exception e){
            log.warn("Error adding damage report", e);
        }
    }
}
