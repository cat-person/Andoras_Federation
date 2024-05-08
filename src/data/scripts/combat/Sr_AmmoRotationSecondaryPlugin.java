package src.data.scripts.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;


public class Sr_AmmoRotationSecondaryPlugin implements OnFireEffectPlugin {

        private static final String explosiveWeaponId = "sr_sanitizer_dummy1";	//Id of the HE projectile version of the weapon
	private static final String energyWeaponId = "sr_sanitizer_dummy2"; //Id of the ENERGY projectile version of the weapon
	private static final String fragWeaponId = "sr_sanitizer_dummy3";	//Id of the HE projectile version of the weapon
	private int fireCounter = 0;
	
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		
		if (fireCounter >= 3 && fireCounter < 6) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, explosiveWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}		
		} else if (fireCounter >= 6 && fireCounter < 9) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, energyWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}			
		} else if (fireCounter >= 9) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, explosiveWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}
			if (fireCounter >= 12) {
				fireCounter = 0;
			}
		} else fireCounter++;
	}
	
	
}




