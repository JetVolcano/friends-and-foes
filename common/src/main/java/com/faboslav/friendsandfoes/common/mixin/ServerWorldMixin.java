package com.faboslav.friendsandfoes.common.mixin;

import com.faboslav.friendsandfoes.common.FriendsAndFoes;
import com.faboslav.friendsandfoes.common.entity.ZombieHorseEntityAccess;
import com.faboslav.friendsandfoes.common.tag.FriendsAndFoesTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

//? >=1.21.3 {
import net.minecraft.util.profiling.Profiler;
import com.faboslav.friendsandfoes.common.versions.VersionedEntitySpawnReason;
//?} else {
/*import java.util.function.Supplier;
import net.minecraft.util.profiling.ProfilerFiller;
*///?}


@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level implements WorldGenLevel
{
	//? >=1.21.3 {
	protected ServerWorldMixin(
		WritableLevelData levelData,
		ResourceKey<Level> dimension,
		RegistryAccess registryAccess,
		Holder<DimensionType> dimensionTypeRegistration,
		boolean isClientSide,
		boolean isDebug,
		long biomeZoomSeed,
		int maxChainedNeighborUpdates
	) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}
	//?} else {
	/*protected ServerWorldMixin(
		WritableLevelData properties,
		ResourceKey<Level> registryRef,
		RegistryAccess registryManager,
		Holder<DimensionType> dimensionEntry,
		Supplier<ProfilerFiller> profiler,
		boolean isClient,
		boolean debugWorld,
		long biomeAccess,
		int maxChainedNeighborUpdates
	) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
	}
	*///?}

	@Shadow
	protected abstract BlockPos findLightningTargetAround(BlockPos pos);

	@Inject(
		method = "tickChunk",
		at = @At("TAIL")
	)
	public void friendsandfoes_addZombieHorseSpawnEvent(
		LevelChunk chunk, int randomTickSpeed, CallbackInfo ci
	) {
		if (FriendsAndFoes.getConfig().enableZombieHorseTrap) {
			BlockPos blockPos;
			ChunkPos chunkPos = chunk.getPos();
			int i = chunkPos.getMinBlockX();
			int j = chunkPos.getMinBlockZ();
			//? >=1.21.3 {
			var profiler = Profiler.get();
			//?} else {
			/*var profiler = this.getProfiler();
			*///?}
			profiler.push("thunder2");

			if (
				this.isRaining()
				&& this.isThundering()
				&& this.getRandom().nextInt(100000) == 0
				&& this.isRainingAt(blockPos = this.findLightningTargetAround(this.getBlockRandomPos(i, 0, j, 15)))
			) {
				DifficultyInstance localDifficulty = this.getCurrentDifficultyAt(blockPos);
				boolean canZombieHorseSpawn = ((ServerLevel)(Object)this).getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.random.nextDouble() < (double) localDifficulty.getEffectiveDifficulty() * 0.01 && !this.getBlockState(blockPos.below()).is(FriendsAndFoesTags.LIGHTNING_RODS);

				if (canZombieHorseSpawn) {
					ZombieHorse zombieHorse = EntityType.ZOMBIE_HORSE.create(this/*? >=1.21.3 {*/, VersionedEntitySpawnReason.EVENT/*?}*/);
					((ZombieHorseEntityAccess) zombieHorse).friendsandfoes_setTrapped(true);
					zombieHorse.setAge(0);
					zombieHorse.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
					this.addFreshEntity(zombieHorse);
				}

				LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this/*? >=1.21.3 {*/, VersionedEntitySpawnReason.EVENT/*?}*/);
				//? >= 1.21.5 {
				lightningBolt.snapTo(Vec3.atBottomCenterOf(blockPos));
				//?} else {
				/*lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
				*///?}
				lightningBolt.setVisualOnly(canZombieHorseSpawn);

				this.addFreshEntity(lightningBolt);
			}

			profiler.pop();
		}
	}

	@Inject(
		method = "findLightningRod",
		at = @At("TAIL"),
		cancellable = true
	)
	public void friendsandfoes_getLightningRodPos(
		BlockPos pos,
		CallbackInfoReturnable<Optional<BlockPos>> cir
	) {
		if (cir.getReturnValue().isEmpty()) {
			ServerLevel serverWorld = (ServerLevel) (Object) this;

			Optional<BlockPos> optional = serverWorld.getPoiManager().findClosest((registryEntry) -> {
				return registryEntry.is(FriendsAndFoesTags.LIGHTNING_ROD_POI);
			}, (posx) -> {
				return posx.getY() == this.getHeight(Heightmap.Types.WORLD_SURFACE, posx.getX(), posx.getZ()) - 1;
			}, pos, 128, PoiManager.Occupancy.ANY);


			if (optional.isPresent()) {
				cir.setReturnValue(optional.map((posx) -> posx.above(1)));
			}
		}
	}
}
