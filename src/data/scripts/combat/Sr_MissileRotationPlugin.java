package data.scripts.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;


public class Sr_MissileRotationPlugin implements OnFireEffectPlugin {

        private static final String explosiveWeaponId = "sr_gregaria_dummy2";	//Id of the HE projectile version of the weapon
	private static final String energyWeaponId = "sr_gregaria_dummy1"; //Id of the BURN projectile version of the weapon
	private static final String fragWeaponId = "sr_gregaria_dummy3";	//Id of the FRAG projectile version of the weapon
	private int fireCounter = 0;
	
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		
		if (fireCounter >= 10 && fireCounter < 25) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, energyWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}		
		} else if (fireCounter >= 25 && fireCounter < 40) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, explosiveWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}			
		} else if (fireCounter >= 40) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, fragWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}
			if (fireCounter >= 60) {
				fireCounter = 0;
			}
		} else fireCounter++;
	}
	
	
}




