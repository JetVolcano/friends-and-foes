package com.faboslav.friendsandfoes.common.events.client;

import com.faboslav.friendsandfoes.common.events.base.EventHandler;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Event related is code based on The Bumblezone/Resourceful Lib mods with permissions from the authors
 *
 * @author TelepathicGrunt
 * <a href="https://github.com/TelepathicGrunt/Bumblezone">https://github.com/TelepathicGrunt/Bumblezone</a>
 * @author ThatGravyBoat
 * <a href="https://github.com/Team-Resourceful/ResourcefulLib">https://github.com/Team-Resourceful/ResourcefulLib</a>
 */
public record RegisterEntityRenderersEvent(Registrar registrar)
{
	public static final EventHandler<RegisterEntityRenderersEvent> EVENT = new EventHandler<>();

	public <T extends Entity> void register(EntityType<T> type, EntityRendererProvider<T> factory) {
		registrar.register(type, factory);
	}

	@FunctionalInterface
	public interface Registrar
	{
		<T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> factory);
	}
}
