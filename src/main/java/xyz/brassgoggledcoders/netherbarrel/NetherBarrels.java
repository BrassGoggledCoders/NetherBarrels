package xyz.brassgoggledcoders.netherbarrel;


import com.tterrag.registrate.Registrate;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.Mod;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelBlocks;

@Mod(NetherBarrels.ID)
public class NetherBarrels {
    public static final String ID = "nether_barrels";

    private static final Lazy<Registrate> REGISTRATE = Lazy.of(() -> Registrate.create(ID));

    public NetherBarrels() {
        NetherBarrelBlocks.setup();
    }

    public static Registrate getRegistrate() {
        return REGISTRATE.get();
    }
}
