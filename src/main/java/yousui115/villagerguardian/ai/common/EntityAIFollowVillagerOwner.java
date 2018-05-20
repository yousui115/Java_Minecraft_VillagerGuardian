package yousui115.villagerguardian.ai.common;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Predicate;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import yousui115.villagerguardian.Utils;

public class EntityAIFollowVillagerOwner extends EntityAIBase
{
    //■
    private EntityCreature taskOwner;

    private EntityVillager owner;

    private final double followSpeed;
    private final PathNavigate dollPathfinder;
    private int timeToRecalcPath;
    float maxDist;
    float minDist;
    private float oldWaterCost;

    /**
     *
     * @param taskOwnerIn
     */
    public EntityAIFollowVillagerOwner(EntityCreature taskOwnerIn, double followSpeedIn, float minDistIn, float maxDistIn)
    {
        taskOwner = taskOwnerIn;
        followSpeed = followSpeedIn;
        dollPathfinder = taskOwnerIn.getNavigator();
        maxDist = maxDistIn;
        minDist = minDistIn;

        setMutexBits(5);
    }

    /**
     *
     */
    @Override
    public boolean shouldExecute()
    {
        if (owner == null)
        {
            UUID ownerUUID = Utils.getPairUUID(taskOwner);

            if (ownerUUID == null) { return false; }

            List<Entity> entities = taskOwner.world.getEntitiesInAABBexcluding(taskOwner, taskOwner.getEntityBoundingBox().grow(32d), new Predicate<Entity>()
            {
                @Override
                public boolean apply(Entity input)
                {
                    return input instanceof EntityVillager;
                }
            });

            for (Entity entity : entities)
            {
                if (entity.getUniqueID().equals(ownerUUID)) { owner = (EntityVillager)entity; }
            }

            //■オーナーが近くにいないので、解除
            if (owner == null)
            {
                Utils.setPairUUID(taskOwner, null);
                return false;
            }
        }

        //■オーナーが死んだ！この人でなし！
        if (owner != null && owner.isEntityAlive() == false)
        {
            Utils.setPairUUID(taskOwner, null);
            return false;
        }

        if (taskOwner.getDistanceSq(owner) < (double)(this.minDist * this.minDist))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        return !this.dollPathfinder.noPath() && this.taskOwner.getDistanceSq(this.owner) > (double)(this.maxDist * this.maxDist);
    }

    @Override
    public void startExecuting()
    {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = taskOwner.getPathPriority(PathNodeType.WATER);
        taskOwner.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void resetTask()
    {
        this.owner = null;
        this.dollPathfinder.clearPath();
        this.taskOwner.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    @Override
    public void updateTask()
    {
        this.taskOwner.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float)this.taskOwner.getVerticalFaceSpeed());

        if (--this.timeToRecalcPath <= 0)
        {
            this.timeToRecalcPath = 10;

            if (!this.dollPathfinder.tryMoveToEntityLiving(this.owner, this.followSpeed))
            {
                if (!this.taskOwner.getLeashed() && !this.taskOwner.isRiding())
                {
                    if (this.taskOwner.getDistanceSq(this.owner) >= 144.0D)
                    {
                        int i = MathHelper.floor(this.owner.posX) - 2;
                        int j = MathHelper.floor(this.owner.posZ) - 2;
                        int k = MathHelper.floor(this.owner.getEntityBoundingBox().minY);

                        for (int l = 0; l <= 4; ++l)
                        {
                            for (int i1 = 0; i1 <= 4; ++i1)
                            {
                                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(i, j, k, l, i1))
                                {
                                    this.taskOwner.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.taskOwner.rotationYaw, this.taskOwner.rotationPitch);
                                    this.dollPathfinder.clearPath();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean isTeleportFriendlyBlock(int x, int p_192381_2_, int y, int p_192381_4_, int p_192381_5_)
    {
        BlockPos blockpos = new BlockPos(x + p_192381_4_, y - 1, p_192381_2_ + p_192381_5_);
        IBlockState iblockstate = taskOwner.world.getBlockState(blockpos);
        return iblockstate.getBlockFaceShape(taskOwner.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(this.taskOwner) && taskOwner.world.isAirBlock(blockpos.up()) && taskOwner.world.isAirBlock(blockpos.up(2));
    }
}
