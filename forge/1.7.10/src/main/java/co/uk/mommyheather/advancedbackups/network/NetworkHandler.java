package computer.heather.advancedbackups.network;

import computer.heather.advancedbackups.AdvancedBackups;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler {

	public static final SimpleNetworkWrapper HANDLER = new SimpleNetworkWrapper(AdvancedBackups.MODID);

	public static void init()
	{
		HANDLER.registerMessage(new PacketBackupStatus.Handler(), PacketBackupStatus.class, 1, Side.CLIENT);
        HANDLER.registerMessage(new PacketToastSubscribe.Handler(), PacketToastSubscribe.class, 2, Side.SERVER);
        HANDLER.registerMessage(new PacketClientReload.Handler(), PacketClientReload.class, 3, Side.CLIENT);
	}
    
}

