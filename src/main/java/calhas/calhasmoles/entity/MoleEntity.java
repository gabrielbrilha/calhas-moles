package calhas.calhasmoles.entity;

import calhas.calhasmoles.items.MolesItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class MoleEntity extends Animal {
    private static final EntityDataAccessor<Boolean> BURROWED = SynchedEntityData.defineId(MoleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int DEFAULT_BURROW_TICKS = 80;
    private static final int SCARED_BURROW_TICKS = 140;

    private int burrowTicks;

    public MoleEntity(EntityType<? extends MoleEntity> entityType, Level world) {
        super(entityType, world);
    }

    // Defines life and speed
    public static AttributeSupplier.Builder createMoleAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5)
                .add(Attributes.TEMPT_RANGE, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    // Only spawns near to plantations
    public static boolean canSpawn(EntityType<MoleEntity> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, spawnReason, pos, random) && isNearPlantationOrVillage(level, pos);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BURROWED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BurrowWhenScaredGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.25, Ingredient.of(MolesItems.WORM), false));
        this.goalSelector.addGoal(2, new MoveToCropGoal(this, 1.0, 12));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    // Each tick keeps the mole freeze while is burrowed
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.isBurrowed()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);

            if (this.burrowTicks > 0) {
                this.burrowTicks--;
            }

            if (this.burrowTicks <= 0 && !this.isStartledByNearbyMovement()) {
                this.setBurrowed(false);
            }
        }
    }

    // If is burrowed, the mole is invisible and cannot move.
    public boolean isBurrowed() {
        return this.entityData.get(BURROWED);
    }

    // Hides mole for a certain time
    public void burrow(int ticks) {
        this.burrowTicks = Math.max(ticks, DEFAULT_BURROW_TICKS);
        this.setBurrowed(true);
    }

    private void setBurrowed(boolean burrowed) {
        this.entityData.set(BURROWED, burrowed);
        this.setInvisible(burrowed);

        if (burrowed)
            this.getNavigation().stop();
    }

    // Search for nearby entities that scares the mole
    private boolean isStartledByNearbyMovement() {
        AABB sensingArea = this.getBoundingBox().inflate(6.0D, 2.0D, 6.0D);

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, sensingArea, e -> e != this && e.isAlive())) {
            if (entity instanceof Player player && player.isSprinting() && this.distanceToSqr(player) <= 49.0D)
                return true;

            double speedSq = entity.getDeltaMovement().horizontalDistanceSqr();
            if (speedSq > 0.09D && this.distanceToSqr(entity) <= 16.0D)
                return true;
        }

        return false;
    }

    // Verifies if is a plantation area
    private static boolean isNearPlantationOrVillage(LevelReader level, BlockPos center) {
        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -6; z <= 6; z++) {
                    scanPos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(scanPos);
                    if (isCropOrFarmland(state))
                        return true;
                }
            }
        }
        return false;
    }

    // Farmland e plantas contam como área “boa” para a toupeira.
    private static boolean isCropOrFarmland(BlockState state) {
        return state.is(Blocks.FARMLAND) || state.getBlock() instanceof CropBlock;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return new MoleEntity(ModEntityTypes.MOLE, level);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(MolesItems.WORM);
    }

    // If mole is scared then hide
    private static final class BurrowWhenScaredGoal extends Goal {
        private final MoleEntity mole;

        private BurrowWhenScaredGoal(MoleEntity mole) {
            this.mole = mole;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.mole.isBurrowed() && this.mole.isStartledByNearbyMovement();
        }

        @Override
        public void start() {
            this.mole.burrow(SCARED_BURROW_TICKS);
        }
    }

    // Walks towards crops
    private static final class MoveToCropGoal extends MoveToBlockGoal {
        private final MoleEntity mole;

        private MoveToCropGoal(MoleEntity mole, double speedModifier, int searchRange) {
            super(mole, speedModifier, searchRange, 3);
            this.mole = mole;
        }

        @Override
        public boolean canUse() {
            return !this.mole.isBurrowed() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mole.isBurrowed() && super.canContinueToUse();
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (isCropOrFarmland(state))
                return true;

            return isCropOrFarmland(level.getBlockState(pos.above()));
        }
    }
}
