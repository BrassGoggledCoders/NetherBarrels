package xyz.brassgoggledcoders.netherbarrel.content;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brassgoggledcoders.netherbarrel.NetherBarrel;
import xyz.brassgoggledcoders.netherbarrel.block.NetherBarrelBlock;
import xyz.brassgoggledcoders.netherbarrel.blockentity.NetherBarrelBlockEntity;

public class NetherBarrelBlocks {

    public static final BlockEntry<NetherBarrelBlock> NETHER_BARREL = NetherBarrel.getRegistrate()
            .object("nether_barrel")
            .block(NetherBarrelBlock::new)
            .initialProperties(Material.STONE, MaterialColor.NETHER)
            .properties(properties -> properties.strength(2.0F, 6.0F)
                    .sound(SoundType.NETHER_BRICKS)
            )
            .item()
            .build()
            .blockEntity(NetherBarrelBlockEntity::new)
            .build()
            .register();

    public static final RegistryEntry<BlockEntityType<NetherBarrelBlockEntity>> NETHER_BARREL_ENTITY =
            NETHER_BARREL.getSibling(ForgeRegistries.BLOCK_ENTITIES);

    public static void setup() {

    }
}
