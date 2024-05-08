package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sr_burstBooster extends BaseShipSystemScript {

    String engineDecoID = "sr_vernierEngine_white";
    List<vernierEngine> decos = new ArrayList<>();

    final Map<ShipAPI.HullSize, Float> strafeMulti = new HashMap<>();

    {
        strafeMulti.put(ShipAPI.HullSize.FIGHTER, 1f);
        strafeMulti.put(ShipAPI.HullSize.FRIGATE, 1f);
        strafeMulti.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        strafeMulti.put(ShipAPI.HullSize.CRUISER, 0.5f);
        strafeMulti.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }

    Vector2f burstVel = new Vector2f();
    float velAngle = 0;

    boolean doOnce = true;

    IntervalUtil timer = new IntervalUtil(0.05f, 0.05f);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) return;
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) amount = 0;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (doOnce) {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getSpec().getWeaponId().equals(engineDecoID)) {
                    decos.add(new vernierEngine(weapon));
                }
            }
        }
        if (state.equals(State.IN)) {
            //stats.getMaxSpeed().modifyFlat(id, 600);
            Vector2f newVector = new Vector2f();
            if (ship.getEngineController().isAccelerating()) {
                newVector.y += 1 * ship.getAcceleration();
            }
            if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
                newVector.y -= 1 * ship.getDeceleration();
            }
            if (ship.getEngineController().isStrafingLeft()) {
                newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
            }
            if (ship.getEngineController().isStrafingRight()) {
                newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
            }
            VectorUtils.rotate(newVector, ship.getFacing() - 90);
            Vector2f NewSpeed;
            if (VectorUtils.isZeroVector(newVector)) {
                NewSpeed = (Vector2f) new Vector2f(ship.getVelocity()).normalise(newVector).scale(650);
            }
            else NewSpeed = (Vector2f) newVector.normalise(newVector).scale(650f);
            burstVel = new Vector2f(NewSpeed);
            velAngle = Misc.getAngleInDegrees(burstVel) - 180;
            ship.getVelocity().set(NewSpeed);
            stats.getAcceleration().modifyFlat(id, 0f);
            stats.getDeceleration().modifyFlat(id, 0f);
            ship.getSystem().forceState(ShipSystemAPI.SystemState.ACTIVE, 0);
        } else if (state.equals(State.ACTIVE)) {
            timer.advance(amount);
            if (timer.intervalElapsed()) {
                for (vernierEngine deco : decos) {
                    if (Math.abs(MathUtils.getShortestRotation(deco.weapon.getCurrAngle(), velAngle)) <= 50f){
                        thrust(deco, 3);
                    } else {
                        thrust(deco, 0);
                    }
                }
            }
        } else if (state.equals(State.OUT)) {
            for (vernierEngine deco : decos) {
                thrust(deco, 0);
            }
            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            if (ship.getVelocity().lengthSquared() > Math.pow(ship.getMaxSpeed(), 2)) {
                ship.getVelocity().scale(Math.max(0.1f, 1 - 2f * amount));
                ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT, 0);
            } else {
                ship.getSystem().forceState(ShipSystemAPI.SystemState.OUT, 1);
            }
        }

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        for (vernierEngine deco : decos) {
            deco.weapon.getSprite().setSize(0,0);
            deco.previousThrust = 0;
        }
    }

    private void thrust(vernierEngine data, float thrust) {
        Vector2f size = new Vector2f(15, 80);
        float smooth = 0.35f;
        WeaponAPI weapon = data.weapon;
        if (data.engine.isDisabled()) thrust = 0f;

        //random sprite

        int frame = MathUtils.getRandomNumberInRange(1, data.frames - 1);
        if (frame == weapon.getAnimation().getNumFrames()) {
            frame = 1;
        }
        weapon.getAnimation().setFrame(frame);
        SpriteAPI sprite = weapon.getSprite();


        //target angle
        float length = thrust;


        //thrust is reduced while the engine isn't facing the target angle, then smoothed
        length -= data.previousThrust;
        length *= smooth;
        length += data.previousThrust;
        data.previousThrust = length;


        //finally the actual sprite manipulation
        float width = length * size.x / 2 + size.x / 2;
        float height = length * size.y + (float) Math.random() * 3 + 3;
        sprite.setSize(width, height);
        sprite.setCenter(width / 2, height / 2);

        //clamp the thrust then color stuff
        length = Math.max(0, Math.min(1, length));

        Color engineColor = data.engine.getEngineColor();
        Color shift = weapon.getShip().getEngineController().getFlameColorShifter().getCurr();
        float ratio = shift.getAlpha() / 255f;
        int Red = Math.min(255, Math.round(engineColor.getRed() * (1f - ratio) + shift.getRed() * ratio));
        int Green = Math.min(255, Math.round((engineColor.getGreen() * (1f - ratio) + shift.getGreen() * ratio) * (0.5f + length / 2)));
        int Blue = Math.min(255, Math.round((engineColor.getBlue() * (1f - ratio) + shift.getBlue() * ratio) * (0.75f + length / 4)));

        sprite.setColor(new Color(Red, Green, Blue));
    }

    private class vernierEngine {

        vernierEngine(WeaponAPI weapon) {
            this.weapon = weapon;
            this.frames = weapon.getAnimation().getNumFrames();
            this.engine = weapon.getShip().getEngineController().getShipEngines().get(0);
        }

        WeaponAPI weapon;
        ShipEngineControllerAPI.ShipEngineAPI engine;
        int frames;
        float previousThrust;

    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        Vector2f newVector = new Vector2f();
        if (ship.getEngineController().isAccelerating()) {
            newVector.y += 1 * ship.getAcceleration();
        }
        if (ship.getEngineController().isAcceleratingBackwards() || ship.getEngineController().isDecelerating()) {
            newVector.y -= 1 * ship.getDeceleration();
        }
        if (ship.getEngineController().isStrafingLeft()) {
            newVector.x -= 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        if (ship.getEngineController().isStrafingRight()) {
            newVector.x += 1 * ship.getAcceleration() * strafeMulti.get(ship.getHullSize());
        }
        return !(VectorUtils.isZeroVector(newVector) && VectorUtils.isZeroVector(ship.getVelocity()));
    }
}

