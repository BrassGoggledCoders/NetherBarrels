package xyz.brassgoggledcoders.netherbarrel.content;

import com.tterrag.registrate.util.entry.MenuEntry;
import xyz.brassgoggledcoders.netherbarrel.NetherBarrels;
import xyz.brassgoggledcoders.netherbarrel.menu.NetherBarrelMenu;
import xyz.brassgoggledcoders.netherbarrel.screen.NetherBarrelScreen;

public class NetherBarrelContainers {

    public static MenuEntry<NetherBarrelMenu> NETHER_BARREL_CONTAINER = NetherBarrels.getRegistrate()
            .object("nether_barrel")
            .menu(NetherBarrelMenu::create, () -> NetherBarrelScreen::new)
            .register();

    public static void setup() {

    }
}
