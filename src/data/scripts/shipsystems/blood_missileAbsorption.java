package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class blood_missileAbsorption extends BaseShipSystemScript {

    String missileId = "harpoon";
    static public float damagePerMissile = 250,
            refireInterval = 0.25f,
            radius = 300,
            expandTime = 1,
            damageAbsorptionMulti = 0.5f,
            fluxPerDamage = 0.5f,
            absorptionRate = 500,
            absorptionBufferMax = 2000,
            accumulationCap = 10000;

    IntervalUtil timer = new IntervalUtil(0.1f, 0.1f);

    float accumulatedDamage = 0,
            absorptionBuffer = 0,
            refireTimer = 0,
            ringSizeMulti = 0;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        absorptionRate = 2000;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        float amount = engine.getElapsedInLastFrame();
        engine.maintainStatusForPlayerShip("blood_missileAbsorption", null, "damage abs", accumulatedDamage + "", false);
        engine.maintainStatusForPlayerShip("blood_missileAbsorption2", null, "buffer", absorptionBuffer / absorptionRate + "", false);
        if (absorptionBuffer > 0) {
            absorptionBuffer -= absorptionRate * amount * effectLevel;
        }
        if (!ship.isPhased()) {
            if (ringSizeMulti < 1) ringSizeMulti += Math.min(1, (1 / expandTime) * amount);
            timer.advance(amount);
            MagicRender.singleframe(Global.getSettings().getSprite("graphics/hud/holo_status.png"),
                    ship.getLocation(),
                    new Vector2f((ship.getCollisionRadius() + radius * ringSizeMulti) * 2, (ship.getCollisionRadius() + radius * ringSizeMulti) * 2),
                    0,
                    new Color(255, 255, 255, 255),
                    false, CombatEngineLayers.BELOW_SHIPS_LAYER);
            if (timer.intervalElapsed()) {
                MissileAPI missile = AIUtils.getNearestEnemyMissile(ship);
                if (missile != null && MathUtils.isWithinRange(ship, missile, radius * ringSizeMulti)) {
                    float fluxCost = missile.getDamageAmount() * fluxPerDamage * (missile.getDamageType().equals(DamageType.FRAGMENTATION) ? 0.25f : 1f);
                    if (absorptionBuffer <= 0 && ship.getCurrFlux() + fluxCost <= ship.getMaxFlux()) {
                        absorptionBuffer += fluxCost;
                        accumulatedDamage += missile.getDamageAmount() * damageAbsorptionMulti * (missile.getDamageType().equals(DamageType.FRAGMENTATION) ? 0.25f : 1f);
                        ship.getFluxTracker().increaseFlux(fluxCost, false);
                        engine.applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 1000, DamageType.ENERGY, 0, true, false, null);

                    }
                    float size = 50f;
                    float fxDuration = MathUtils.getRandomNumberInRange(0.3f, 0.4f);
                    int nebulaCount = MathUtils.getRandomNumberInRange(3, 4);
                    for (int i = 0; i < nebulaCount; i++) {
                        Vector2f particleVel = (Vector2f) normalise(missile.getVelocity()).scale(250);
                        Vector2f tempSpeed = new Vector2f(particleVel);
                        Color nebulaColor = randomizeColor(new Color(166, 20, 171, 255), 0.3f);
                        engine.addNebulaParticle(missile.getLocation(),
                                (Vector2f) VectorUtils.rotate(tempSpeed, MathUtils.getRandomNumberInRange(-10, 10)).scale(MathUtils.getRandomNumberInRange(0.7f, 1f)),
                                MathUtils.getRandomNumberInRange(0.8f, 1.2f) * size,
                                MathUtils.getRandomNumberInRange(1.3f, 1.5f),
                                0,
                                0.3f,
                                fxDuration + MathUtils.getRandomNumberInRange(-0.1f, 0.1f),
                                nebulaColor,
                                true);
                    }
                }
                if (accumulatedDamage > accumulationCap) accumulatedDamage = accumulationCap;
            }
        } else {
            ringSizeMulti = 0;
            refireTimer += amount * effectLevel;
            while (refireTimer >= refireInterval) {
                refireTimer -= refireInterval;
                if (accumulatedDamage >= damagePerMissile) {
                    accumulatedDamage -= damagePerMissile;
                    float angleOffset = MathUtils.getRandomNumberInRange(-90, 90);
                    Vector2f missileLoc = Vector2f.add(ship.getLocation(), (Vector2f) Misc.getUnitVectorAtDegreeAngle(ship.getFacing() + angleOffset).scale(ship.getCollisionRadius()), null);
                    DamagingProjectileAPI missile = (DamagingProjectileAPI) engine.spawnProjectile(ship, null, missileId, missileLoc, ship.getFacing() + angleOffset, ship.getVelocity());
                    float size = 50f;
                    float fxDuration = MathUtils.getRandomNumberInRange(0.3f, 0.4f);
                    int nebulaCount = MathUtils.getRandomNumberInRange(3, 4);
                    for (int i = 0; i < nebulaCount; i++) {
                        Vector2f particleVel = (Vector2f) normalise(Misc.getUnitVectorAtDegreeAngle(missile.getFacing())).scale(250);
                        Vector2f tempSpeed = new Vector2f(particleVel);
                        Color nebulaColor = randomizeColor(new Color(92, 9, 238, 255), 0.3f);
                        engine.addNebulaParticle(missile.getLocation(),
                                (Vector2f) VectorUtils.rotate(tempSpeed, MathUtils.getRandomNumberInRange(-10, 10)).scale(MathUtils.getRandomNumberInRange(0.7f, 1f)),
                                MathUtils.getRandomNumberInRange(0.8f, 1.2f) * size,
                                MathUtils.getRandomNumberInRange(1.3f, 1.5f),
                                0,
                                0.3f,
                                fxDuration + MathUtils.getRandomNumberInRange(-0.1f, 0.1f),
                                nebulaColor,
                                true);
                    }
                    if (ship.getShipTarget() != null && missile instanceof MissileAPI && ((MissileAPI) missile).getMissileAI() instanceof GuidedMissileAI) {
                        ((GuidedMissileAI) ((MissileAPI) missile).getMissileAI()).setTarget(ship.getShipTarget());
                    }
                }
            }
        }

        stats.getEntity().getCustomData().put("blood_missileAbsorption", accumulatedDamage);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        absorptionBuffer = 0;
        ringSizeMulti = 0;
    }

    public static Color randomizeColor(Color color, float magnitude) {
        float colorMax = 1 + magnitude;
        float colorMin = 1 - magnitude;
        int red = MathUtils.clamp(Math.round(MathUtils.getRandomNumberInRange(color.getRed() * colorMin, color.getRed() * colorMax)), 0, 255);
        int green = MathUtils.clamp(Math.round(MathUtils.getRandomNumberInRange(color.getGreen() * colorMin, color.getGreen() * colorMax)), 0, 255);
        int blue = MathUtils.clamp(Math.round(MathUtils.getRandomNumberInRange(color.getBlue() * colorMin, color.getBlue() * colorMax)), 0, 255);

        return new Color(red, green, blue);
    }

    public final Vector normalise(Vector2f vector) {
        float len = vector.length();
        if (len != 0.0F) {
            float l = 1.0F / len;
            return vector.scale(l);
        } else {
            return vector;
        }
    }
}
