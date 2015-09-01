package nova.core.wrapper.mc18.launcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import nova.core.entity.Entity;
import nova.core.entity.EntityFactory;
import nova.core.loader.Loadable;
import nova.core.wrapper.mc18.wrapper.block.forward.FWBlock;
import nova.core.wrapper.mc18.wrapper.block.forward.FWTile;
import nova.core.wrapper.mc18.wrapper.block.forward.FWTileUpdater;
import nova.core.wrapper.mc18.wrapper.entity.forward.FWEntity;
import nova.core.wrapper.mc18.wrapper.item.FWItem;

import java.util.Set;

/**
 * @author Calclavia
 */
public class CommonProxy implements Loadable {
	@Override
	public void preInit() {
		GameRegistry.registerTileEntity(FWTile.class, "novaTile");
		GameRegistry.registerTileEntity(FWTileUpdater.class, "novaTileUpdater");
		int globalUniqueEntityId = EntityRegistry.findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(FWEntity.class, "novaEntity", globalUniqueEntityId);
		EntityRegistry.registerModEntity(FWEntity.class, "novaEntity", globalUniqueEntityId, NovaMinecraft.instance, 64, 20, true);
	}

	public void registerResourcePacks(Set<Class<?>> modClasses) {

	}

	public void registerItem(FWItem item) {

	}

	public void postRegisterBlock(FWBlock block) {

	}

	public Entity spawnParticle(net.minecraft.world.World world, EntityFactory factory) {
		return null;
	}

	public Entity spawnParticle(net.minecraft.world.World world, Entity entity) {
		return null;
	}

	public boolean isPaused() {
		return false;
	}

	public EntityPlayer getClientPlayer() {
		return null;
	}
}