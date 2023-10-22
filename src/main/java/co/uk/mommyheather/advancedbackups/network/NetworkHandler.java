package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler {

	public static final SimpleNetworkWrapper HANDLER = new SimpleNetworkWrapper(AdvancedBackups.MODID);

	public static void init()
	{
		HANDLER.registerMessage(new PacketBackupStatus.Handler(), PacketBackupStatus.class, 1, Side.CLIENT);
	}
    
}

