package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

import static data.scripts.shipsystems.blood_missileAbsorption.accumulationCap;

public class blood_MissileAbsorptionAI implements ShipSystemAIScript {

    ShipAPI ship;
    ShipSystemAPI system;

    float absorptionDamageThreshold = 1500;

    IntervalUtil timer = new IntervalUtil(0.5f, 0.5f);


    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (system.getState().equals(ShipSystemAPI.SystemState.IDLE)) {
            timer.advance(amount);
            if (timer.intervalElapsed()) {
                boolean activateSystem = false;
                if (!ship.isPhased()) {
                    List<MissileAPI> missiles = AIUtils.getNearbyEnemyMissiles(ship, 600);
                    float totalDamage = 0;
                    for (MissileAPI missile : missiles) {
                        float damage = missile.getDamageAmount();
                        float flux = missile.getDamageAmount();
                        if (missile.getDamageType().equals(DamageType.HIGH_EXPLOSIVE)) {
                            damage *= 2;
                        } else if (missile.getDamageType().equals(DamageType.KINETIC)) {
                            damage *= 0.5f;
                            flux *= 1f;
                        } else if (missile.getDamageType().equals(DamageType.FRAGMENTATION)) {
                            damage *= 0.25f;
                            flux *= 0.25f;
                        }
                        totalDamage += damage;
                    }
                    if (totalDamage >= absorptionDamageThreshold) {
                        activateSystem = true;
                    }
                } else {
                    float totalDP = 0;
                    for (ShipAPI Juiced : AIUtils.getNearbyEnemies(ship, ship.getCollisionRadius() + 1000)) {
                        totalDP += this.ship.getDeployCost();
                    }
                    float absorbed = 0;
                    if (ship.getCustomData().get("blood_missileAbsorption") instanceof Float)

                        absorbed = (float) ship.getCustomData().get("blood_missileAbsorption");
                    if (absorbed >= accumulationCap * 0.25f && totalDP >= ship.getDeployCost() * 0.25f) {
                        activateSystem = true;
                    }

                }
                if (activateSystem) {
                    ship.useSystem();
                }
            }
        }
    }
}
