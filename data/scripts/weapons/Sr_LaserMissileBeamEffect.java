package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Sr_LaserMissileBeamEffect implements BeamEffectPlugin {

    IntervalUtil fireInterval = new IntervalUtil(0.15f, 0.65f);
    boolean wasZero = true;
    //need weapon ID NOT projectile ID
    String MissileID = "sr_plasmissile";
    boolean aimMissileTOBeamTarget = true;
    Vector2f offset = new Vector2f(5,20);


    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
            if (hitShield) return;
            float dur = beam.getDamage().getDpsDuration();
            if (!wasZero) dur = 0;
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            fireInterval.advance(dur);
            if (fireInterval.intervalElapsed()) {
                Vector2f spwanLoc = VectorUtils.rotate(new Vector2f(offset), beam.getWeapon().getCurrAngle() - 90);
                spwanLoc = new Vector2f(spwanLoc.x + beam.getWeapon().getLocation().x, spwanLoc.y + beam.getWeapon().getLocation().y);
                DamagingProjectileAPI missile = (DamagingProjectileAPI) engine.spawnProjectile(beam.getSource(), beam.getWeapon(), MissileID,spwanLoc,beam.getWeapon().getCurrAngle(),beam.getSource().getVelocity());
                if (aimMissileTOBeamTarget && missile instanceof MissileAPI && ((MissileAPI) missile).getMissileAI() instanceof GuidedMissileAI){
                    ((GuidedMissileAI) ((MissileAPI) missile).getMissileAI()).setTarget(beam.getDamageTarget());
                }
            }
        }
    }
}
