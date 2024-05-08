package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;

public class Sr_PlasmadumpStats extends BaseShipSystemScript {
    
    public static final float WEAPON_RANGE_PERCENT = 2f;
    public static final float MAX_TIME_MULT = 1.75f;
    public static final float MIN_TIME_MULT = 0.23f;
//    public static final float DAM_MULT = 0.1f;
    public static final float BALLISTIC_BUFF = 0.60f;
    public final float BALLISTIC_ROF = 5.0f;
    public static final float FLUX_REDUCTION = 0.30f;
    public final float ENGINE_BUFF = 3.5f;
//    public static final float SPEED_DEBUFF = 0.33f;
    public final int EXTRA_AMMO = 1;

    public static final Color JITTER_COLOR = new Color(255,90,220,85);
    public static final Color JITTER_UNDER_COLOR = new Color(255,10,250,105);


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
	
        float weaponRangePercent = WEAPON_RANGE_PERCENT * effectLevel;  

        stats.getBallisticWeaponRangeBonus().modifyPercent(id, 10*WEAPON_RANGE_PERCENT*effectLevel);
      
        if(!stats.getBallisticRoFMult().getPercentMods().containsKey(id)){
            ShipAPI ship = (ShipAPI)stats.getEntity();
            for(WeaponAPI w : ship.getAllWeapons()){
                if(w.getSlot().getSlotSize()== WeaponSize.LARGE){
                    w.setAmmo(w.getSpec().getMaxAmmo()*EXTRA_AMMO);
                    break;
                }
            }
        }
                ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
                        for (WeaponAPI w : ship.getAllWeapons()) {
			if (state == State.IN) {
					w.setForceNoFireOneFrame(true);
				}
			}
		}

                float jitterLevel = effectLevel;
		float jitterRangeBonus = 0;
		float maxRangeBonus = 10f;
		if (state == State.IN) {
			jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
			if (jitterLevel > 1) {
				jitterLevel = 1f;
			}
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		} else if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
		} else if (state == State.OUT) {
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		effectLevel *= effectLevel;
		
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		
	
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			if (ship.areAnyEnemiesInRange()) {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
//			} else {
//				Global.getCombatEngine().getTimeMult().modifyMult(id, 2f / shipTimeMult);
//			}
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);

        stats.getFluxDissipation().modifyMult(id, 1 - FLUX_REDUCTION * effectLevel);
        
        stats.getBallisticRoFMult().modifyPercent(id, 105*BALLISTIC_ROF*effectLevel);
            
        stats.getMaxSpeed().modifyPercent(id, 75*ENGINE_BUFF*effectLevel);
        stats.getAcceleration().modifyPercent(id, -100*ENGINE_BUFF*effectLevel);
        stats.getDeceleration().modifyPercent(id, 20*ENGINE_BUFF*effectLevel);
//        stats.getTurnAcceleration().modifyPercent(id, 50*ENGINE_BUFF*effectLevel);
//        stats.getMaxTurnRate().modifyPercent(id, 50*ENGINE_BUFF*effectLevel);
            
          stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 - BALLISTIC_BUFF*effectLevel);
//          stats.getBallisticRoFMult().modifyMult(id, 1+BALLISTIC_ROF);
//        } else {
//            stats.getBallisticWeaponFluxCostMod().unmodify(id);
//            stats.getBallisticRoFMult().modifyMult(id, 1-BALLISTIC_ROF);
//        }
    }



    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getFluxDissipation().unmodify(id);

        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTimeMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
    }
}
