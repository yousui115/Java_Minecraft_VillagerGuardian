package yousui115.villagerguardian;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.EntityDataManager.DataEntry;
import net.minecraft.pathfinding.PathNavigateGround;
import yousui115.villagerguardian.ai.common.EntityAIDefendVillage2;
import yousui115.villagerguardian.ai.common.EntityAIFollowPlayerToVillage;
import yousui115.villagerguardian.ai.common.EntityAIFollowVillagerOwner;
import yousui115.villagerguardian.ai.witch.EntityAIHealRanged;
import yousui115.villagerguardian.ai.witch.EntityAINearestHealTarget;

public class Utils
{
    /**
     * ■再教育
     */
    public static void reeducationAI(@Nonnull EntityCreature mobIn)
    {
        //■行動AIの再教育（共通）
        tameAI(mobIn);

        if (mobIn instanceof EntityWitch)
        {
            //■ウィッチだけのスペシャルメニュー
            tameHealerWitch((EntityWitch)mobIn);
        }
        else
        {
            //■敵選びの再教育
            removeTargetAI(mobIn);
            tameTargetAI(mobIn);
        }

        //■オーナーなんて居ませんでした。
        if (mobIn instanceof EntityTameable)
        {
            EntityTameable tameable = (EntityTameable)mobIn;
            tameable.setOwnerId(null);
            tameable.setTamed(false);
//            tameable.getAISit().setMutexBits(0x10000);
        }

        //■ルート検索の設定（扉を無視した探索）（共通）
        if (Utils.canDoorThrough(mobIn) == true)
        {
            ((PathNavigateGround)mobIn.getNavigator()).setBreakDoors(true);
            mobIn.setCanPickUpLoot(true);

            mobIn.tasks.addTask(6, new EntityAIOpenDoor(mobIn, true));
        }

        //■あまりにも消えるの早いから、デフォ名をカスタム名に登録しとくよ！
        mobIn.setCustomNameTag(mobIn.getName());
        //■デスポーン禁止！
        mobIn.enablePersistence();

        //■とりあえず落ち着いて。
        mobIn.setAttackTarget(null);
        mobIn.setLastAttackedEntity(null);
        mobIn.setRevengeTarget(null);
    }

    /**
     * ■共通AI
     * @param mobIn
     */
    public static void tameAI(@Nonnull EntityCreature mobIn)
    {

        //■プレイヤーさん、村までお願い。（共通）
        mobIn.tasks.addTask(1, new EntityAIFollowPlayerToVillage(mobIn));

        //■村人に雇われましたー。
        mobIn.tasks.addTask(3, new EntityAIFollowVillagerOwner(mobIn, 1.0D, 5.0F, 2.0F));

        //TODO:オーナーがいる場合もやっちゃうから、自作して止めるのも手。
        //■制限ありのランダムウォーク
        mobIn.tasks.addTask(7, new EntityAIMoveTowardsRestriction(mobIn, 1.0d));
    }

    /**
     * ■ウィッチは特別。
     * @param witch
     */
    public static void tameHealerWitch(@Nonnull EntityWitch witchIn)
    {
        //■既存の行動AIを忘れてもらおう。
        Object[] sets = witchIn.tasks.taskEntries.toArray();
        for (int i = 0; i < sets.length; i++)
        {
            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = (EntityAITasks.EntityAITaskEntry)sets[i];
            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;
            witchIn.tasks.removeTask(entityaibase);
        }

        //■攻撃系ポーション -> 回復系ポーション
        for (int i = 0; i < sets.length; i++)
        {
            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = (EntityAITasks.EntityAITaskEntry)sets[i];

            int priority = entityaitasks$entityaitaskentry.priority;
            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;

            //■挿げ替える
            if (entityaibase instanceof EntityAIAttackRanged)
            {
                entityaibase = new EntityAIHealRanged(witchIn, 1.0D, 60, 10.0F);
            }

            witchIn.tasks.addTask(priority, entityaibase);
        }

        //■既存の敵選びAIを（ｒｙ
        removeTargetAI(witchIn);

//        witchIn.targetTasks.addTask(1, new EntityAINearestHealTarget<EntityVillager>(witchIn, EntityVillager.class, true));
        witchIn.targetTasks.addTask(1, new EntityAINearestHealTarget<EntityCreature>(witchIn, EntityCreature.class, 10, true, false, new Predicate<EntityCreature>()
        {
            @Override
            public boolean apply(EntityCreature mobIn)
            {
                boolean flag1 = mobIn instanceof EntityVillager;
                boolean flag2 = Utils.isGuardian(mobIn);
                return flag1 || flag2;
            }
        }));

    }

    /**
     * ■TargetAIの消去
     * @param mobIn
     */
    private static void removeTargetAI(@Nonnull EntityCreature mobIn)
    {
        Object[] sets = mobIn.targetTasks.taskEntries.toArray();

        for (int i = 0; i < sets.length; i++)
        {
            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = (EntityAITasks.EntityAITaskEntry)sets[i];
            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;
            mobIn.targetTasks.removeTask(entityaibase);
        }
    }

    /**
     * ■調教
     * @param mobIn
     */
    private static void tameTargetAI(@Nonnull EntityCreature mobIn)
    {
        //■村人護り隊の一員
        if (Utils.getPairUUID(mobIn) == null)
        {
            //■村全体を護るぜ！
            mobIn.targetTasks.addTask(1, new EntityAIDefendVillage2(mobIn));
        }
        else
        {
            //TODO
            //■護衛対象を護るぜ！
        }

        //■あ いったー！
        mobIn.targetTasks.addTask(2, new EntityAIHurtByTarget(mobIn, false, new Class[0]));
        //■透明化していないIMob・・・クリーパーは爆発して怖いから、ちょっと。
        mobIn.targetTasks.addTask(3, new EntityAINearestAttackableTarget(mobIn, EntityLiving.class, 30, false, true, new Predicate<EntityLiving>()
        {
            public boolean apply(@Nullable EntityLiving target)
            {
                //■ターゲットが居る
                if (target != null)
                {
                    //■クリーパーは避ける
                    if (target instanceof EntityCreeper) { return false; }

                    //■他の村人護り隊は攻撃しない
                    if (target instanceof EntityCreature && Utils.isGuardian((EntityCreature)target)) { return false; }

                    //■透明化してないMOBを攻撃するよ
                    if (IMob.VISIBLE_MOB_SELECTOR.apply(target) == true) { return true; }

                    //■それ以外はしらん
                }

                //■攻撃しませーん
                return false;
            }
        }));
    }


    /**
     * ■村人護り隊 入隊の条件
     * @param mobIn
     * @return
     */
    public static boolean canChangeVillagerDefender(@Nonnull EntityCreature mobIn)
    {
        //TODO:現状、直接攻撃できるMOB と ネコ と ウィッチ だけ入隊出来る。

        //■ウィッチはヒーラーとなるのだ！
        if (mobIn instanceof EntityWitch) { return true; }

        //■ねこー（EntityAIOcelotAttackがMeleeを継承してないのが憎い。下の処理で纏められない）
        if (mobIn instanceof EntityOcelot) { return true; }

        //■関節攻撃は村人に当たるやも。お祈り申し上げます。
        boolean canAttacker = false;
        for (EntityAITasks.EntityAITaskEntry entry : mobIn.tasks.taskEntries)
        {
            if (entry.action instanceof EntityAIAttackMelee)
            {
                canAttacker = true;
                break;
            }
        }

        return canAttacker;
    }

    /**
     *
     * @param mobIn
     * @return
     */
    public static boolean canDoorThrough(@Nonnull EntityCreature mobIn)
    {
        if (mobIn.width <= 0.8F && mobIn.height <= 1.99F)
        {
            return true;
        }

        return false;
    }




    //===========================================NBT/DataParam==============================================================================


    //■NBTきー
    public final static String NBT_KEY_VG = "VillageGuardian";

    public final static String NBT_KEY_PAIR = "Pair";

    public final static String NBT_KEY_IS_GUARDIAN= "IsGuardian";

    //■DataParam
//    public final static Map < Class <? extends Entity > , List<DataParameter> > ENTITY_PARAM_MAP = Maps. < Class <? extends Entity > , List<DataParameter> > newHashMap();
    public final static Map < String , List<DataParameter> > ENTITY_PARAM_MAP = Maps. < String , List<DataParameter> > newHashMap();

    /**
     * ■DataParamの登録
     */
    public static void registerParam(@Nonnull EntityCreature mobIn)
    {
        UUID uuid = null;
        boolean isGuardian = false;

        if (mobIn.getEntityData().hasKey(NBT_KEY_VG) == true)
        {
            //■
            NBTTagCompound nbtVG = mobIn.getEntityData().getCompoundTag(NBT_KEY_VG);
            String strUUID = nbtVG.getString(Utils.NBT_KEY_PAIR);
            uuid = strUUID.compareTo("null") == 0 ? null : UUID.fromString(strUUID);

            //■
            isGuardian = nbtVG.getBoolean(NBT_KEY_IS_GUARDIAN);
        }

        Utils.registerDataParameter(mobIn, uuid, isGuardian);
    }

    /**
     *
     * @param mobIn
     * @param uuidIn
     * @param isGuardianIn
     */
    private static void registerDataParameter(@Nonnull EntityCreature mobIn, @Nullable UUID uuidIn, boolean isGuardianIn)
    {
        //DataManagerのバニラ構成が完了している時点で走らせること。

        //■クラス（フルパス）
        Class clazz = mobIn.getClass();

        //■登録するDataParameter
        List<DataParameter> list = null;

        //■未登録のEntity
        if (ENTITY_PARAM_MAP.containsKey(clazz.getName()) == false)
        {
            list = Lists.<DataParameter>newArrayList();

            //TODO:念のため、全EntityCreatureを対象にパラメータを作ってる。対象者のみに絞るとPCに優しいか。
            //■
            DataParameter<Optional<UUID>> param_pair = EntityDataManager.<Optional<UUID>>createKey(clazz, DataSerializers.OPTIONAL_UNIQUE_ID);
            DataParameter<Boolean> param_is_guardian = EntityDataManager.<Boolean>createKey(clazz, DataSerializers.BOOLEAN);

            list.add(param_pair);
            list.add(param_is_guardian);

            ENTITY_PARAM_MAP.put(clazz.getName(), list);

            //Debug
            System.out.println("DataParameter register : " + clazz.getName() + " : " + param_pair.getId() + " : " + param_is_guardian.getId());

        }
        else
        {
            list = ENTITY_PARAM_MAP.get(clazz.getName());
        }

        //TODO:型確認した方がいいのだけれど。
        //■登録
        try
        {
            //■対象MOBのデータパラメータを取得
            List< EntityDataManager.DataEntry<?>> listEntry = mobIn.getDataManager().getAll();

            //■対象Mobへ追加する、DataParamのID
            int id = list.get(0).getId();

            //■対象MobのDataParamリストから、上記IDに設定されているEntryを取得。なければNull
            EntityDataManager.DataEntry entry = null;//listEntry.size() <= id ? null : listEntry.get(id);

            boolean alreadySetting = false;

            for (java.util.Iterator<DataEntry<?>> itr = listEntry.iterator(); itr.hasNext();)
            {
                entry = itr.next();

                if (entry.getKey().getId() == id)
                {
                    alreadySetting = true;
                    break;
                }
            }

            //TODO:なぜ登録済みなのか：チャンク凍結とかそこら辺のからみ？
            if (alreadySetting == true)
            {
                if (entry.getKey() == list.get(0))
                {
                    System.out.println("Already registered. : " + clazz.getName());
                }
                else
                {
                    System.out.println("Already registered. But unknown DataEntry");
                }
            }
            else
            {
                mobIn.getDataManager().register(list.get(0), Optional.fromNullable(uuidIn));
                mobIn.getDataManager().register(list.get(1), isGuardianIn);
            }
        }
        catch (IllegalArgumentException e)
        {
            VillagerGuardian.logger.error("Mob : " + mobIn.getClass().toString());

            throw e;
        }
    }

    @Nullable
    private static List<DataParameter> getDataParameterList(@Nonnull EntityCreature mobIn)
    {
        if (ENTITY_PARAM_MAP.containsKey(mobIn.getClass().getName()) == false)
        {
//            //TODO:nullやらを返さないのは、なんとなくそれでハマりそう、と思ったんだけど杞憂かしらん。
//            throw new RuntimeException("not registered in ENTITY_PARAM_MAP! : " + mobIn.getClass());
            VillagerGuardian.logger.log(Level.WARN, "not registered in ENTITY_PARAM_MAP! : " + mobIn.getClass());
            return null;
        }

        return ENTITY_PARAM_MAP.get(mobIn.getClass().getName());
    }

    @Nullable
    public static DataParameter getParameter_PAIR(@Nonnull EntityCreature mobIn)
    {
        List<DataParameter> list = getDataParameterList(mobIn);
        return list == null ? null :list.get(0);
    }
    @Nullable
    public static DataParameter getParameter_IS_GUARDIAN(@Nonnull EntityCreature mobIn)
    {
        List<DataParameter> list = getDataParameterList(mobIn);
        return list == null ? null : list.get(1);
    }


    private static NBTTagCompound getNBT_VG(@Nonnull EntityCreature mobIn)
    {
        NBTTagCompound nbtVG = null;

        if (mobIn.getEntityData().hasKey(NBT_KEY_VG) == false)
        {
            nbtVG = new NBTTagCompound();
            nbtVG.setString(Utils.NBT_KEY_PAIR, "null");
            nbtVG.setBoolean(Utils.NBT_KEY_IS_GUARDIAN, false);
            mobIn.getEntityData().setTag(NBT_KEY_VG, nbtVG);
        }
        else
        {
            nbtVG = mobIn.getEntityData().getCompoundTag(NBT_KEY_VG);
        }

        return nbtVG;
    }
    @Nullable
    private static UUID getNBT_PairUUID(@Nonnull EntityCreature mobIn)
    {
        if (mobIn.getEntityData().hasKey(NBT_KEY_VG) == true)
        {
            NBTTagCompound nbtVG = getNBT_VG(mobIn);
            String strUUID = nbtVG.getString(Utils.NBT_KEY_PAIR);
            UUID uuid = strUUID.compareTo("null") == 0 ? null : UUID.fromString(strUUID);

            return uuid;
        }

        return null;
    }
    @Nullable
    private static boolean getNBT_IsGuardian(@Nonnull EntityCreature mobIn)
    {
        if (mobIn.getEntityData().hasKey(NBT_KEY_VG) == true)
        {
            NBTTagCompound nbtVG = getNBT_VG(mobIn);
            boolean isGuardian = nbtVG.getBoolean(Utils.NBT_KEY_IS_GUARDIAN);

            return isGuardian;
        }

        return false;
    }


    /**
     * ■あいぼー！
     */
    public static void setPairUUID(@Nonnull EntityCreature mobIn, @Nullable UUID uuidIn)
    {
        NBTTagCompound nbtVG = Utils.getNBT_VG(mobIn);

        //■カスタムデータ
        nbtVG.setString(NBT_KEY_PAIR, uuidIn == null ? "null" : uuidIn.toString());

        //■データパラメータ
        mobIn.getDataManager().set(getParameter_PAIR(mobIn), Optional.fromNullable(uuidIn));
    }

    public static UUID getPairUUID(@Nonnull EntityCreature mobIn)
    {
        DataParameter param = Utils.getParameter_PAIR(mobIn);
        return param == null ? Utils.getNBT_PairUUID(mobIn) : (UUID)((Optional)mobIn.getDataManager().get(getParameter_PAIR(mobIn))).orNull();
    }

    /**
     * ■
     */
    public static void setIsGuardian(@Nonnull EntityCreature mobIn, boolean isGuardianIn)
    {
        NBTTagCompound nbtVG = Utils.getNBT_VG(mobIn);

        //■カスタムデータ
        nbtVG.setBoolean(NBT_KEY_IS_GUARDIAN, isGuardianIn);

        //■データパラメータ
        mobIn.getDataManager().set(getParameter_IS_GUARDIAN(mobIn), isGuardianIn);
    }

    public static boolean isGuardian(@Nonnull EntityCreature mobIn)
    {
        DataParameter param = Utils.getParameter_IS_GUARDIAN(mobIn);
        return param == null ? Utils.getNBT_IsGuardian(mobIn) : (boolean) mobIn.getDataManager().get(param);
    }
}
