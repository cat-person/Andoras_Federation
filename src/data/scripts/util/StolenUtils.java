package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//those methods were copied from source files of other modders like Sundog and Dark.Revenant

public class StolenUtils {
    static final float SAFE_DISTANCE = 600f;
    static final float DEFAULT_DAMAGE_WINDOW = 3f;
    static final Map<HullSize, Float> baseOverloadTimes = new HashMap<HullSize, Float>();

    static {
        baseOverloadTimes.put(HullSize.FIGHTER, 10f);
        baseOverloadTimes.put(HullSize.FRIGATE, 4f);
        baseOverloadTimes.put(HullSize.DESTROYER, 6f);
        baseOverloadTimes.put(HullSize.CRUISER, 8f);
        baseOverloadTimes.put(HullSize.CAPITAL_SHIP, 10f);
        baseOverloadTimes.put(HullSize.DEFAULT, 6f);
    }

    public static Vector2f getMidpoint(Vector2f from, Vector2f to, float d) {
        d *= 2;

        return new Vector2f(
                (from.x * (2 - d) + to.x * d) / 2,
                (from.y * (2 - d) + to.y * d) / 2
        );
    }

    public static Vector2f toRelative(CombatEntityAPI entity, Vector2f point) {
        Vector2f retVal = new Vector2f(point);
        Vector2f.sub(retVal, entity.getLocation(), retVal);
        VectorUtils.rotate(retVal, -entity.getFacing(), retVal);
        return retVal;
    }

    public static Vector2f toAbsolute(CombatEntityAPI entity, Vector2f point) {
        Vector2f retVal = new Vector2f(point);
        VectorUtils.rotate(retVal, entity.getFacing(), retVal);
        Vector2f.add(retVal, entity.getLocation(), retVal);
        return retVal;
    }

    public static void blink(Vector2f at) {
        Global.getCombatEngine().addHitParticle(at, new Vector2f(), 30, 1, 0.1f, Color.RED);
    }

    public static List<ShipAPI> getShipsOnSegment(Vector2f from, Vector2f to) {
        float distance = MathUtils.getDistance(from, to);
        Vector2f center = new Vector2f();
        center.x = (from.x + to.x) / 2;
        center.y = (from.y + to.y) / 2;

        List<ShipAPI> list = new ArrayList<ShipAPI>();

        for (ShipAPI s : CombatUtils.getShipsWithinRange(center, distance / 2)) {
            if (CollisionUtils.getCollisionPoint(from, to, s) != null) list.add(s);
        }

        return list;
    }

    public static ShipAPI getFirstShipOnSegment(Vector2f from, Vector2f to, CombatEntityAPI exception) {
        ShipAPI winner = null;
        float record = Float.MAX_VALUE;

        for (ShipAPI s : getShipsOnSegment(from, to)) {
            if (s == exception) continue;

            float dist2 = MathUtils.getDistanceSquared(s, from);

            if (dist2 < record) {
                record = dist2;
                winner = s;
            }
        }

        return winner;
    }

    public static ShipAPI getFirstNonFighterOnSegment(Vector2f from, Vector2f to, CombatEntityAPI exception) {
        ShipAPI winner = null;
        float record = Float.MAX_VALUE;

        for (ShipAPI s : getShipsOnSegment(from, to)) {
            if (s == exception) continue;
            if (s.isFighter()) continue;
            float dist2 = MathUtils.getDistanceSquared(s, from);

            if (dist2 < record) {
                record = dist2;
                winner = s;
            }
        }

        return winner;
    }

    public static ShipAPI getFirstShipOnSegment(Vector2f from, Vector2f to) {
        return getFirstShipOnSegment(from, to, null);
    }

    public static ShipAPI getShipInLineOfFire(WeaponAPI weapon) {
        Vector2f endPoint = weapon.getLocation();
        endPoint.x += Math.cos(Math.toRadians(weapon.getCurrAngle())) * weapon.getRange();
        endPoint.y += Math.sin(Math.toRadians(weapon.getCurrAngle())) * weapon.getRange();

        return getFirstShipOnSegment(weapon.getLocation(), endPoint, weapon.getShip());
    }

    public static float getArmorPercent(ShipAPI ship) {
        float acc = 0;
        int width = ship.getArmorGrid().getGrid().length;
        int height = ship.getArmorGrid().getGrid()[0].length;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                acc += ship.getArmorGrid().getArmorFraction(x, y);
            }
        }

        return acc / (width * height);
    }

    public static void setArmorPercentage(ShipAPI ship, float armorPercent) {
        ArmorGridAPI armorGrid = ship.getArmorGrid();

        armorPercent = Math.min(1, Math.max(0, armorPercent));

        for (int x = 0; x < armorGrid.getGrid().length; ++x) {
            for (int y = 0; y < armorGrid.getGrid()[0].length; ++y) {
                armorGrid.setArmorValue(x, y, armorGrid.getMaxArmorInCell() * armorPercent);
            }
        }
    }

    public static void destroy(CombatEntityAPI entity) {
        Global.getCombatEngine().applyDamage(entity, entity.getLocation(),
                entity.getMaxHitpoints() * 10f, DamageType.HIGH_EXPLOSIVE, 0,
                true, true, entity);
    }

    public static float estimateIncomingDamage(ShipAPI ship) {
        return estimateIncomingDamage(ship, DEFAULT_DAMAGE_WINDOW);
    }

    public static float estimateIncomingDamage(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0f;

        accumulator += estimateIncomingBeamDamage(ship, damageWindowSeconds);

        for (DamagingProjectileAPI proj : Global.getCombatEngine().getProjectiles()) {

            if (proj.getOwner() == ship.getOwner()) continue; // Ignore friendly projectiles

            Vector2f endPoint = new Vector2f(proj.getVelocity());
            endPoint.scale(damageWindowSeconds);
            Vector2f.add(endPoint, proj.getLocation(), endPoint);

            if ((ship.getShield() != null && ship.getShield().isWithinArc(proj.getLocation()))
                    || !CollisionUtils.getCollides(proj.getLocation(), endPoint,
                    new Vector2f(ship.getLocation()), ship.getCollisionRadius()))
                continue;

            accumulator += proj.getDamageAmount() + proj.getEmpAmount();// * Math.max(0, Math.min(1, Math.pow(1 - MathUtils.getDistance(proj, ship) / safeDistance, 2)));
        }

        return accumulator;
    }

    public static float estimateAllIncomingDamage(ShipAPI ship) {
        return estimateIncomingDamage(ship, DEFAULT_DAMAGE_WINDOW);
    }

    public static float estimateIncomingBeamDamage(ShipAPI ship, float damageWindowSeconds) {
        float accumulator = 0f;

        for (BeamAPI beam : Global.getCombatEngine().getBeams()) {
            if (beam.getDamageTarget() != ship) continue;

            float dps = beam.getWeapon().getDerivedStats().getDamageOver30Sec() / 30;
            float emp = beam.getWeapon().getDerivedStats().getEmpPerSecond();

            accumulator += (dps + emp) * damageWindowSeconds;
        }

        return accumulator;
    }

    public static float estimateIncomingMissileDamage(ShipAPI ship) {
        float accumulator = 0f;
        DamagingProjectileAPI missile;

        for (com.fs.starfarer.api.combat.MissileAPI missileAPI : Global.getCombatEngine().getMissiles()) {
            missile = missileAPI;

            if (missile.getOwner() == ship.getOwner()) continue; // Ignore friendly missiles

            float safeDistance = SAFE_DISTANCE + ship.getCollisionRadius();
            float threat = missile.getDamageAmount() + missile.getEmpAmount();

            if (ship.getShield() != null && ship.getShield().isWithinArc(missile.getLocation()))
                continue;

            accumulator += threat * Math.max(0, Math.min(1, Math.pow(1 - MathUtils.getDistance(missile, ship) / safeDistance, 2)));
        }

        return accumulator;
    }

    public static float getHitChance(DamagingProjectileAPI proj, CombatEntityAPI target) {
        float estTimeTilHit = MathUtils.getDistance(target, proj.getLocation())
                / Math.max(1, proj.getWeapon().getProjectileSpeed());

        Vector2f estTargetPosChange = new Vector2f(
                target.getVelocity().x * estTimeTilHit,
                target.getVelocity().y * estTimeTilHit);

        float estFacingChange = target.getAngularVelocity() * estTimeTilHit;

        Vector2f projVelocity = proj.getVelocity();

        target.setFacing(target.getFacing() + estFacingChange);
        Vector2f.add(target.getLocation(), estTargetPosChange, target.getLocation());

        projVelocity.scale(estTimeTilHit * 3);
        Vector2f.add(projVelocity, proj.getLocation(), projVelocity);
        Vector2f estHitLoc = CollisionUtils.getCollisionPoint(proj.getLocation(),
                projVelocity, target);

        target.setFacing(target.getFacing() - estFacingChange);
        Vector2f.add(target.getLocation(), (Vector2f) estTargetPosChange.scale(-1), target.getLocation());

        if (estHitLoc == null) return 0;

        return 1;
    }

    public static float getHitChance(WeaponAPI weapon, CombatEntityAPI target) {
        float estTimeTilHit = MathUtils.getDistance(target, weapon.getLocation())
                / Math.max(1, weapon.getProjectileSpeed());

        Vector2f estTargetPosChange = new Vector2f(
                target.getVelocity().x * estTimeTilHit,
                target.getVelocity().y * estTimeTilHit);

        float estFacingChange = target.getAngularVelocity() * estTimeTilHit;

        double theta = weapon.getCurrAngle() * (Math.PI / 180);
        Vector2f projVelocity = new Vector2f(
                (float) Math.cos(theta) * weapon.getProjectileSpeed() + weapon.getShip().getVelocity().x,
                (float) Math.sin(theta) * weapon.getProjectileSpeed() + weapon.getShip().getVelocity().y);

        target.setFacing(target.getFacing() + estFacingChange);
        Vector2f.add(target.getLocation(), estTargetPosChange, target.getLocation());

        projVelocity.scale(estTimeTilHit * 3);
        Vector2f.add(projVelocity, weapon.getLocation(), projVelocity);
        Vector2f estHitLoc = CollisionUtils.getCollisionPoint(weapon.getLocation(),
                projVelocity, target);

        target.setFacing(target.getFacing() - estFacingChange);
        Vector2f.add(target.getLocation(), (Vector2f) estTargetPosChange.scale(-1), target.getLocation());

        if (estHitLoc == null) return 0;

        return 1;
    }

    public static float getFPWorthOfSupport(ShipAPI ship, float range) {
        float retVal = 0;

        for (Iterator iter = AIUtils.getNearbyAllies(ship, range).iterator(); iter.hasNext(); ) {
            ShipAPI ally = (ShipAPI) iter.next();
            if (ally == ship) continue;
            float colDist = ship.getCollisionRadius() + ally.getCollisionRadius();
            float distance = Math.max(0, MathUtils.getDistance(ship, ally) - colDist);
            float maxRange = Math.max(1, range - colDist);

            retVal += getFPStrength(ally) * (1 - distance / maxRange);
        }

        return retVal;
    }

    public static float getFPWorthOfHostility(ShipAPI ship, float range) {
        float retVal = 0;

        for (Iterator iter = AIUtils.getNearbyEnemies(ship, range).iterator(); iter.hasNext(); ) {
            ShipAPI enemy = (ShipAPI) iter.next();
            float colDist = ship.getCollisionRadius() + enemy.getCollisionRadius();
            float distance = Math.max(0, MathUtils.getDistance(ship, enemy) - colDist);
            float maxRange = Math.max(1, range - colDist);

            retVal += getFPStrength(enemy) * (1 - distance / maxRange);
        }

        return retVal;
    }

    public static float getStrengthInArea(Vector2f at, float range) {
        float retVal = 0;

        for (ShipAPI ship : CombatUtils.getShipsWithinRange(at, range)) {
            retVal += getFPStrength(ship);
        }

        return retVal;
    }

    public static float getStrengthInArea(Vector2f at, float range, int owner) {
        float retVal = 0;

        for (ShipAPI ship : CombatUtils.getShipsWithinRange(at, range)) {
            if (ship.getOwner() == owner) retVal += getFPStrength(ship);
        }

        return retVal;
    }

    public static float getFPStrength(ShipAPI ship) {
        DeployedFleetMemberAPI member = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship);
        return (member == null || member.getMember() == null)
                ? 0
                : member.getMember().getMemberStrength();
    }

    public static float getFP(ShipAPI ship) {
        DeployedFleetMemberAPI member = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship);
        return (member == null || member.getMember() == null)
                ? 0
                : member.getMember().getFleetPointCost();
    }

    public static float getBaseOverloadDuration(ShipAPI ship) {
        return baseOverloadTimes.get(ship.getHullSize());
    }

    public static float estimateOverloadDurationOnHit(ShipAPI ship, float damage, DamageType type) {
        if (ship.getShield() == null) return 0;

        float fluxDamage = damage * type.getShieldMult()
                * ship.getMutableStats().getShieldAbsorptionMult().getModifiedValue();
        fluxDamage += ship.getFluxTracker().getCurrFlux()
                - ship.getFluxTracker().getMaxFlux();

        if (fluxDamage <= 0) return 0;

        return Math.min(15, getBaseOverloadDuration(ship) + fluxDamage / 25);
    }

    public static float getLifeExpectancy(ShipAPI ship) {
        float damage = estimateIncomingDamage(ship);
        return (damage <= 0) ? 3600 : ship.getHitpoints() / damage;
    }

    // taken from MagicLib because it's deprecated there with no indication of what to do instead.
    public static void createSmoothFlare(CombatEngineAPI engine, ShipAPI origin, Vector2f point, float thickness, float length, float angle, Color fringeColor, Color coreColor) {
        for (int i = 1; (float) i < length / 50.0F; ++i) {
            point.x = (float) ((double) point.x + FastTrig.cos(angle * 3.1415927F / 180.0F));
            point.y = (float) ((double) point.y + FastTrig.sin(angle * 3.1415927F / 180.0F));
            engine.spawnEmpArc(origin, point, null, new SimpleEntity(point), DamageType.FRAGMENTATION, 0.0F, 0.0F, 10.0F, null, 25.0F, new Color(fringeColor.getRed(), fringeColor.getGreen(), fringeColor.getBlue(), Math.min(255, (int) (thickness * (float) fringeColor.getAlpha() / 128.0F))), coreColor);
        }
    }
}