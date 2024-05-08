package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import data.dcr.andorasfederation.DamageReportManagerV1;
import data.scripts.util.StolenUtils;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.Random;

//based on code from Sundog's ICE repair drone and Dark.Revenant's Imperium Titan

public class RepairDrone_AI extends BaseShipAI {
    private static final float REPAIR_RANGE = 55f;
    private static final float REPAIR_HULL = 8f;
    private static final float REPAIR_ARMOR = 0.35f;
    private static final float FLUX_PER_MX_PERFORMED = 2.3f;

    private final Color SPARK_COLOR = new Color(255, 225, 150, 100);
    //private final Color SPARK_COLOR_CORE = new Color(255, 175, 25, 100);
    private static final String SPARK_SOUND_ID = "ed_shock";
    //private final float SPARK_DURATION = 0.2f;
    //private final float SPARK_BRIGHTNESS = 1.0f;
    //private final float SPARK_MAX_RADIUS = 7f;
    private static final float SPARK_CHANCE = 0.67f;
    private static final float SPARK_SPEED_MULTIPLIER = 500.0f;
    private static final float SPARK_VOLUME = 1.0f;
    private static final float SPARK_PITCH = 1.0f;

    private final ShipwideAIFlags flags = new ShipwideAIFlags();
    private final ShipAIConfig config = new ShipAIConfig();
    private ShipAPI carrier;
    ShipAPI target;
    Vector2f targetOffset;
    Random rng = new Random();
    ArmorGridAPI armorGrid;
    float maxArmorInCell, cellSize;
    int gridWidth, gridHeight, cellCount;
    boolean shouldRepair = false;
    boolean returning = false;
    boolean spark = false;
    float targetFacingOffset = Float.MIN_VALUE;
    float range = 4000f;

    private final IntervalUtil interval = new IntervalUtil(0.23f, 0.37f);
    private final IntervalUtil countdown = new IntervalUtil(4f, 4f);

    public RepairDrone_AI(ShipAPI ship) {
        super(ship);
    }

    @Override
    public void advance(float amount) {

        if (carrier == null) {
            init();
            //if(carrier == null){
            //delete
            //}
        }

        if (ship.isLanding()) {
            countdown.advance(amount);
            if (countdown.intervalElapsed()) {
                ship.getWing().getSource().land(ship);
                return;
            }
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            super.advance(amount);

            if (target == null) return;

            if (shouldRepair) {
                repairArmorAndHull();
            } else if (returning && !ship.isLanding() && MathUtils.getDistance(ship, carrier) < carrier.getCollisionRadius() / 3f) {
                ship.beginLandingAnimation(carrier);
            }

        }
        goToDestination();

    }

    @Override
    public boolean needsRefit() {
        return ship.getFluxTracker().getFluxLevel() >= 1f;
    }

    @Override
    public void cancelCurrentManeuver() {
    }

    @Override
    public void evaluateCircumstances() {
        if (carrier == null || !carrier.isAlive()) {
            StolenUtils.destroy(ship);
            return;
        }

        setTarget(chooseTarget());

        if (returning) {
            targetOffset = StolenUtils.toRelative(target, ship.getWing().getSource().getLandingLocation(ship));
        } else {
            do {
                targetOffset = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
            } while (!CollisionUtils.isPointWithinBounds(targetOffset, target));

            targetOffset = StolenUtils.toRelative(target, targetOffset);

            armorGrid = target.getArmorGrid();
            maxArmorInCell = armorGrid.getMaxArmorInCell();
            cellSize = armorGrid.getCellSize();
            gridWidth = armorGrid.getGrid().length;
            gridHeight = armorGrid.getGrid()[0].length;
            cellCount = gridWidth * gridHeight;
        }

        // not cloaked, and there's stuff to repair
        if ((target.getPhaseCloak() == null || !target.getPhaseCloak().isOn())
                && !returning
                && (StolenUtils.getArmorPercent(target) < 1f || target.getHullLevel() < .98f)
                && MathUtils.getDistance(ship, target) < REPAIR_RANGE) {
            shouldRepair = true;
        } else {
            shouldRepair = false;
        }
    }

    Point computeArmorCellToRepair() {
        if (gridWidth <= 0 || gridHeight <= 0) {
            return null;
        }
        for (int i = 0; i < (1 + cellCount / 5); ++i) {
            int x = rng.nextInt(gridWidth);
            int y = rng.nextInt(gridHeight);

            if (armorGrid.getArmorValue(x, y) < maxArmorInCell){
                return new Point(x, y);
            }
        }

        return null;
    }

    void repairArmorAndHull() {
        float totalHullRepaired = 0;
        spark = true;
        ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux() + FLUX_PER_MX_PERFORMED); // TODO: DON'T HEAL ABOVE COMBAT START FOR ARMOR OR HULL

        // there's no skill bonus for armor repair because there's no obvious fighter skill that should affect it.
        // also repair drones are already buffed by improvements to their speed
        float bonus = 1f;
        if (target.getHullSize() == HullSize.CAPITAL_SHIP) {
            bonus = 4;
        } else if (target.getHullSize() == HullSize.CRUISER) {
            bonus = 3;
        } else if (target.getHullSize() == HullSize.DESTROYER) {
            bonus = 2;
        }

        if (target.getHullLevel() < .99f) {
            // repair the hull
            totalHullRepaired = (REPAIR_HULL * bonus);
            target.setHitpoints(target.getHitpoints() + totalHullRepaired);

            // don't repair beyond our max
            float overage = target.getHitpoints() - target.getMaxHitpoints();
            if(overage > 0){
                totalHullRepaired -= overage;
                target.setHitpoints(target.getMaxHitpoints());
            }
        }

        float totalArmorRepaired = 0;
        Point cellToFix = computeArmorCellToRepair();
        if (cellToFix != null) {
            for (int x = cellToFix.getX() - 1; x <= cellToFix.getX() + 1; ++x) {
                if (x < 0 || x >= gridWidth) continue;

                for (int y = cellToFix.getY() - 1; y <= cellToFix.getY() + 1; ++y) {
                    if (y < 0 || y >= gridHeight) continue;

                    if (armorGrid.getArmorValue(x, y) < maxArmorInCell) {
                        // don't repair beyond our max
                        float cellValue = armorGrid.getArmorValue(x, y);
                        float armorRepairAmount = (REPAIR_ARMOR * bonus);
                        armorGrid.setArmorValue(x, y, cellValue + armorRepairAmount);

                        float overage = armorGrid.getArmorValue(x, y) - maxArmorInCell;
                        if (overage > 0) {
                            armorRepairAmount -= overage;
                            armorGrid.setArmorValue(x, y, maxArmorInCell);
                        }

                        // track how much we actually repaired
                        totalArmorRepaired += armorRepairAmount;
                    }
                }
            }
        }

        if(totalArmorRepaired + totalHullRepaired > 1) {
            Global.getCombatEngine().addFloatingDamageText(target.getLocation(), totalArmorRepaired + totalHullRepaired, Color.GREEN, target, ship.getWing().getSourceShip());
            DamageReportManagerV1.addDamageReport(-totalArmorRepaired, -totalHullRepaired, 0f, 0f, DamageType.OTHER, ship.getWing().getSourceShip(), target, "Silvestris Hull Repair & Armor Welder");
        }
    }

    ShipAPI chooseTarget() {
        if (needsRefit()) {
            returning = true;
            //ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getMaxFlux());
            return carrier;
        } else returning = false;

        if (carrier.getShipTarget() != null
                && carrier.getOwner() == carrier.getShipTarget().getOwner()
                && !carrier.getShipTarget().isDrone()
                && !carrier.getShipTarget().isFighter()) {
            return carrier.getShipTarget();
        }

        ShipAPI mostWounded = carrier;
        float mostDamage = 4f;

        for (ShipAPI s : Global.getCombatEngine().getShips()) {
            float d = MathUtils.getDistance(carrier, s);
            if (!(s.isFighter() || s.isDrone() || s.isHulk() || d > range)) {
                float currDamage = StolenUtils.getArmorPercent(s) + s.getHullLevel();
                float priority = d / 1000f;
                if (s.isStationModule()) {
                    priority++;
                }
                if (s.getOwner() == carrier.getOwner() && currDamage < 1.98f) {
                    if (mostDamage > (currDamage + (priority / 2f))) {
                        mostDamage = (currDamage + (priority / 2f));
                        mostWounded = s;
                    }
                }
            }
        }

        return mostWounded;
    }

    void setTarget(ShipAPI t) {
        if (target == t) return;
        target = t;
        this.ship.setShipTarget(t);
    }

    void goToDestination() {
        Vector2f to = StolenUtils.toAbsolute(target, targetOffset);
        float distance = MathUtils.getDistance(ship, to);

        if (shouldRepair) {
            if (distance < 100) {
                float f = (1 - distance / 100) * 0.2f;
                ship.getLocation().x = (to.x * f + ship.getLocation().x * (2 - f)) / 2;
                ship.getLocation().y = (to.y * f + ship.getLocation().y * (2 - f)) / 2;
                ship.getVelocity().x = (target.getVelocity().x * f + ship.getVelocity().x * (2 - f)) / 2;
                ship.getVelocity().y = (target.getVelocity().y * f + ship.getVelocity().y * (2 - f)) / 2;
            }
        }

        if (shouldRepair && distance < REPAIR_RANGE) {
            if (spark) {
                Global.getSoundPlayer().playLoop(SPARK_SOUND_ID, ship, SPARK_PITCH,
                        SPARK_VOLUME, ship.getLocation(), ship.getVelocity());

                if (targetFacingOffset == Float.MIN_VALUE) {
                    targetFacingOffset = ship.getFacing() - target.getFacing();
                } else {
                    ship.setFacing(MathUtils.clampAngle(targetFacingOffset + target.getFacing()));
                }

                if (Math.random() < SPARK_CHANCE) {
                    Vector2f loc = new Vector2f(ship.getLocation());
                    loc.x += cellSize * 0.5f - cellSize * (float) Math.random();
                    loc.y += cellSize * 0.5f - cellSize * (float) Math.random();

                    Vector2f vel = new Vector2f(ship.getVelocity());
                    vel.x += (Math.random() - 0.5f) * SPARK_SPEED_MULTIPLIER;
                    vel.y += (Math.random() - 0.5f) * SPARK_SPEED_MULTIPLIER;

                    MagicLensFlare.createSharpFlare(
                            Global.getCombatEngine(),
                            ship,
                            loc,
                            5,
                            100,
                            0,
                            SPARK_COLOR,
                            Color.white
                    );
                }
            }
            spark = false;
        } else {
            float distToCarrier = (float) (MathUtils.getDistanceSquared(carrier.getLocation(), ship.getLocation()) / Math.pow(target.getCollisionRadius(), 2));
            if (target == carrier && distToCarrier < 1.0f || ship.isLanding()) {
                float f = 1.0f - Math.min(1, distToCarrier);
                if (!returning){
                    f = f * 0.1f;
                }
                turnToward(target.getFacing());
                ship.getLocation().x = (to.x * (f * 0.1f) + ship.getLocation().x * (2 - f * 0.1f)) / 2;
                ship.getLocation().y = (to.y * (f * 0.1f) + ship.getLocation().y * (2 - f * 0.1f)) / 2;
                ship.getVelocity().x = (target.getVelocity().x * f + ship.getVelocity().x * (2 - f)) / 2;
                ship.getVelocity().y = (target.getVelocity().y * f + ship.getVelocity().y * (2 - f)) / 2;
            } else {
                targetFacingOffset = Float.MIN_VALUE;
                float angleDif = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), to));

                if (Math.abs(angleDif) < 30) {
                    accelerate();
                } else {
                    turnToward(to);
                    decelerate();
                }
                strafeToward(to);
            }
        }
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public void setDoNotFireDelay(float amount) {
    }

    @Override
    public ShipAIConfig getConfig() {
        return config;
    }

    public void init() {
        carrier = ship.getWing().getSourceShip();
        target = carrier;
        targetOffset = StolenUtils.toRelative(carrier, carrier.getLocation());
        range = ship.getWing().getRange();

    }
}
