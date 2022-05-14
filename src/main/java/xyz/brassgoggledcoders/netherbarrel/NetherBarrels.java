package xyz.brassgoggledcoders.netherbarrel;


import com.tterrag.registrate.Registrate;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.Mod;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelBlocks;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelContainers;

@Mod(NetherBarrels.ID)
public class NetherBarrels {
    public static final String ID = "nether_barrels";

    private static final Lazy<Registrate> REGISTRATE = Lazy.of(() -> Registrate.create(ID));

    public NetherBarrels() {
        NetherBarrelBlocks.setup();
        NetherBarrelContainers.setup();
    }

    public static Registrate getRegistrate() {
        return REGISTRATE.get();
    }
}
