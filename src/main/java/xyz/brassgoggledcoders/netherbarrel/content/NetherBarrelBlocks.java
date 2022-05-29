package xyz.brassgoggledcoders.netherbarrel.content;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brassgoggledcoders.netherbarrel.NetherBarrels;
import xyz.brassgoggledcoders.netherbarrel.block.NetherBarrelBlock;
import xyz.brassgoggledcoders.netherbarrel.blockentity.NetherBarrelBlockEntity;

public class NetherBarrelBlocks {

    public static final BlockEntry<NetherBarrelBlock> NETHER_BARREL = NetherBarrels.getRegistrate()
            .object("nether_barrel")
            .block(NetherBarrelBlock::new)
            .initialProperties(Material.STONE, MaterialColor.NETHER)
            .properties(properties -> properties.strength(2.0F, 1200.0F)
                    .sound(SoundType.NETHER_BRICKS)
            )
            .blockstate((context, provider) -> {
                ModelFile openBarrel = provider.models().cubeBottomTop(
                        "block/nether_barrel_open",
                        provider.modLoc("block/nether_barrel_side"),
                        provider.modLoc("block/nether_barrel_back"),
                        provider.modLoc("block/nether_barrel_front_open")
                );
                ModelFile closedBarrel = provider.models().cubeBottomTop(
                        "block/nether_barrel",
                        provider.modLoc("block/nether_barrel_side"),
                        provider.modLoc("block/nether_barrel_back"),
                        provider.modLoc("block/nether_barrel_front_closed")
                );
                provider.directionalBlock(context.get(), blockState -> {
                    if (blockState.getValue(NetherBarrelBlock.OPEN)) {
                        return openBarrel;
                    } else {
                        return closedBarrel;
                    }
                });
            })
            .item()
            .properties(properties -> properties.tab(CreativeModeTab.TAB_DECORATIONS))
            .recipe((context, provider) -> ShapedRecipeBuilder.shaped(context.get())
                    .pattern("NON")
                    .pattern("GBG")
                    .pattern("NON")
                    .define('N', Tags.Items.INGOTS_NETHER_BRICK)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .define('B', Tags.Items.BARRELS_WOODEN)
                    .define('O', Tags.Items.OBSIDIAN)
                    .unlockedBy("has_item", RegistrateRecipeProvider.has(Tags.Items.INGOTS_NETHER_BRICK))
                    .save(provider)
            )
            .build()
            .blockEntity(NetherBarrelBlockEntity::new)
            .build()
            .register();

    public static final RegistryEntry<BlockEntityType<NetherBarrelBlockEntity>> NETHER_BARREL_ENTITY =
            NETHER_BARREL.getSibling(ForgeRegistries.BLOCK_ENTITIES);

    public static void setup() {

    }
}
