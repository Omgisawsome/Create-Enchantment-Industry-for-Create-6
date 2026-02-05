package plus.dragons.createenchantmentindustry.foundation.events;

//import plus.dragons.createdragonlib.event.TextureStitchPreEvent;
import plus.dragons.createdragonlib.event.ViewPointFogColorEvent;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.ink.InkRenderingCamera;

public class ClientEvents {
	public static void register(){
		ViewPointFogColorEvent.CallBack.EVENT.register(InkRenderingCamera::cameraEffect);
		//fixme
		//TextureStitchPreEvent.CallBack.EVENT.register(BlazeEnchanterRenderer::loadTexture);
	}
}
