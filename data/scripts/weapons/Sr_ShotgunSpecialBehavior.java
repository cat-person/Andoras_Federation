package data.scripts.weapons;

import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class Sr_ShotgunSpecialBehavior implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        projectile.getDamage().setSoftFlux(true);
        Vector2f loc = projectile.getLocation();
        Vector2f vel = projectile.getVelocity();
        vel.x += projectile.getSource().getVelocity().x;
        vel.y += projectile.getSource().getVelocity().y;
        int shotCount = 46;
        for (int j = 0; j < shotCount; j++) {
            Vector2f randomVel = MathUtils.getRandomPointOnLine(new Vector2f(vel.x * (0.80f), vel.y * (0.80f)), new Vector2f(vel.x * (1.15f), vel.y * (1.15f)));
            float angleVariance = MathUtils.getRandomNumberInRange(-8f, 8f);
            randomVel.x -= vel.x;
            randomVel.y -= vel.y;

            engine.spawnProjectile(projectile.getSource(), projectile.getWeapon(), projectile.getProjectileSpecId() + "_clone", loc, projectile.getFacing() + angleVariance, randomVel);
        }
        engine.removeEntity(projectile);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //cricket
    }
}
