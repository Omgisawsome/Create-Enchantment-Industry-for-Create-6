package plus.dragons.createenchantmentindustry.content.contraptions.fluids.ink;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.material.FogType;
import plus.dragons.createdragonlib.event.ViewPointFogColorEvent;

public interface InkRenderingCamera {

    boolean isInInk();

	static ViewPointFogColorEvent.FogColor cameraEffect(Camera activeRenderInfo, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier){
		if(activeRenderInfo.getFluidInCamera() == FogType.POWDER_SNOW)
			return null;
		if (((InkRenderingCamera) activeRenderInfo).isInInk()) {
			return new ViewPointFogColorEvent.FogColor(0,0,0);
		}
		return null;
	}
}
