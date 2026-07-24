package online.slavok.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class FriendlyPhantomsModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		// The info screen uses the DrawContext API (1.20+). On older versions the mod
		// still appears in the ModMenu list, just without a custom screen.
		//? if >=1.20 {
		return FriendlyPhantomsScreen::new;
		//?} else {
		/*return screen -> null;*/
		//?}
	}
}
