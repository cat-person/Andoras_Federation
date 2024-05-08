package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class Sr_BelladonaVetGunners extends BaseHullMod {

    private final float ROF = 1.05f;
    private final float FluxUsage = 0.95f;
	private final float TURRET_SPEED_BONUS = 1.10f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyMult(id, ROF);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, FluxUsage);
	stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round((1f - FluxUsage) * 100) + "%";
        if (index == 1) return Math.round((1f - TURRET_SPEED_BONUS) * 100) + "%";
        if (index == 2) return Math.round((1f - ROF) * 100) + "%";
        return null;
    }
}