package xyz.brassgoggledcoders.netherbarrel;


import com.tterrag.registrate.Registrate;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.Mod;
import xyz.brassgoggledcoders.netherbarrel.content.NetherBarrelBlocks;

@Mod(NetherBarrel.ID)
public class NetherBarrel {
    public static final String ID = "nether_barrel";

    private static final Lazy<Registrate> REGISTRATE = Lazy.of(() -> Registrate.create(ID));

    public NetherBarrel() {
        NetherBarrelBlocks.setup();
    }

    public static Registrate getRegistrate() {
        return REGISTRATE.get();
    }
}
