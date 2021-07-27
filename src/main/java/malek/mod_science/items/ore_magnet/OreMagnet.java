package malek.mod_science.items.ore_magnet;

import malek.mod_science.items.ModItems;
import malek.mod_science.items.item_nbt.IOreMagnet;
import malek.mod_science.tags.ModScienceTags;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class OreMagnet extends Item implements ModScienceItemRegistrar.ChildItem {
    public static final int TIME = 20;
    public static final int X_RANGE = 20;
    public static final int Y_RANGE = 20;
    public static final int Z_RANGE = 20;
    public OreMagnet(Settings settings) {
        super(settings);
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient) {
            return ActionResult.PASS;
        }
        ServerWorld world = (ServerWorld) context.getWorld();
        ServerPlayerEntity playerEntity = (ServerPlayerEntity) context.getPlayer();
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        if(stack.getCooldown() == 0) {
            Temp temp1 = new Temp();
            BlockPos.iterateOutwards(pos, X_RANGE, Y_RANGE, Z_RANGE).forEach((blockPos -> {
                if (temp1.thing && matches(world.getBlockState(blockPos), stack)) {
                    switchBlockStates(world, blockPos.add(new BlockPos(Math.min(pos.getX() - blockPos.getX(), 1), Math.min(pos.getY() - blockPos.getY(), 1), Math.min(pos.getZ() - blockPos.getZ(), 1))), blockPos);
                    playerEntity.getItemCooldownManager().set(this, 20);
                    temp1.thing = false;
                }
            }));
        }
        return ActionResult.PASS;
    }

    public static boolean matches(BlockState state, ItemStack stack) {
        if(!IOreMagnet.hasString(stack)) {
            return state.isIn(ModScienceTags.ORES);
        } else {
            return state.getBlock() == Registry.BLOCK.get(new Identifier(IOreMagnet.getString(stack)));
        }
    }

    public static void switchBlockStates(World world, BlockPos pos1, BlockPos pos2) {
        BlockState state1 = world.getBlockState(pos1);
        BlockState state2 = world.getBlockState(pos2);
        world.setBlockState(pos2, state1);
        world.setBlockState(pos1, state2);
    }
    public static BlockPos getClosestPos(BlockPos dest, BlockPos source) {
        int y = absPos(dest.getY(), source.getY());
        int x = absPos(dest.getX(), source.getX());
        int z = absPos(dest.getZ(), source.getZ());
        BlockPos value = source.mutableCopy();
//        if(y <= x && y <= z) {
//            //y is smallest
//            value.add(0, dest.getY()-source.getY(), 0);
//        }
//        else if (x <= z) {
//            //x is smallest
//            value.add(dest.getX() - source.getX(), 0, 0);
//        } else {
//            //z is smallest
//            value.add(0, 0, dest.getZ() - source.getZ());
//        }
        value.add(dest.getX()-source.getX(), dest.getY()-source.getY(), dest.getZ()-source.getZ());
        return value.toImmutable();
    }
    public static int absPos(int a, int b) {
        return Math.abs(a-b);
    }

    @Override
    public Item getOriginalItem() {
        return ModItems.DEFAULT_ORE_MAGNET;
    }

    @Override
    public void transform(MatrixStack matrices) {
        ModScienceItemRegistrar.ChildItem.super.transform(matrices);
    }

    class Temp {
        public boolean thing = true;
    }
}