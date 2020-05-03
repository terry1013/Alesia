package core;

import java.util.*;

import javax.swing.*;

import com.alee.managers.plugin.*;
import com.alee.managers.plugin.data.*;

public abstract class TPlugin extends Plugin {

	public TPlugin() {

	}
	PluginInformation pluginInformation;

	/**
	 * this method is called when the framework need to install all UI from detected plugin. sub class return the
	 * necesary instances of {@link Action} according to the pluging implementation. a complete list of configurable
	 * types are in {@link TPluginManager}
	 * @see #getTypes()
	 * @param type - any of the valid types
	 * @return list of action to apend 
	 */
	public abstract ArrayList<Action> getUI(String type);

	/**
	 * Called during plugin initializacion. use this method ot install plugins where database files or to check plugin
	 * integrity by counting class file or other actions. If plugin instalation or verification fail, throw an execption
	 * explain the situation. {@link PluginManager} deactivate the plugin and continue.
	 * 
	 * @param prps - Properties for this plugin
	 * 
	 * @throws Exception - throw if somethin wrong
	 */
	public void startPlugin() throws Exception {

		// perform standar installation & instalation
		// test plugin instalation
		boolean pi = (Boolean) TPreferences.getPreference(TPreferences.PLUGIN_INSTALL_INFO, getClass().getName(),
				false);
		if (!pi) {
			// execute db script
//			Connection conn = ConnectionManager.getDBConnection("");
//			ScriptRunner sr = new ScriptRunner(conn, false);
//			FileReader fr = new FileReader("script.sql");
//			sr.runScript(fr);

			// set the plugininstalled flag
			TPreferences.setPreference(TPreferences.PLUGIN_INSTALL_INFO, getClass().getName(), true);
		}
	}
	public void endPlugin() {

	}

}
