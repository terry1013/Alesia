package core;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.slf4j.*;

import com.alee.managers.plugin.*;
import com.alee.managers.plugin.data.*;
import com.alee.utils.*;

public class TPluginManager extends PluginManager<TPlugin> {

	public TPluginManager() {
		super("plugins", true);
		setFileFilter((fn) -> fn.getName().endsWith(getPluginDescriptorFile()));
	}
	
	/**
	 * left panel plugin type
	 */
	public static String leftPanelUI = "leftPanelUI";
	/**
	 * setting panel plugin type
	 */
	public  static String settingsUI = "settingsUI";
	
	@Override
	protected DetectedPlugin<TPlugin> detectPlugin(File file) {

		/**
		 * overide implementation to support load plugins form directory
		 */
		try {
			// final String pluginDescriptor = getPluginDescriptorFile();
			final String pluginLogo = getPluginLogoFile();

			// Reading plugin information
			FileInputStream inputStream = new FileInputStream(file);
			final PluginInformation info = XmlUtils.fromXML(inputStream);
			info.setTypes(Arrays.asList(leftPanelUI, settingsUI));
			inputStream.close();

			// Reading plugin icon
			final ImageIcon logo;
			File fl = new File(file.getParent() + "/" + pluginLogo);
			if (fl.exists()) {
				inputStream = new FileInputStream(file);
				logo = ImageUtils.loadImage(inputStream);
			} else {
				logo = null;
			}

			// Checking whether we have already detected this plugin or not
			if (!wasDetected(file.getParent(), file.getName())) {
				// Cache and return new plugin information
				// This cache map is filled here since it has different usage cases
				final DetectedPlugin<TPlugin> plugin = new DetectedPlugin<TPlugin>(file.getParent(), file.getName(),
						info, logo);
				detectedPluginsByPath.put(FileUtils.canonicalPath(file), plugin);
				return plugin;
			}
		} catch (final IOException e) {
			LoggerFactory.getLogger(PluginManager.class).error("Unable to read plugin information", e);
		}
		return null;
	}

	public void startPlugins() {
		for (final TPlugin plugin : getAvailablePlugins()) {
			try {
				plugin.startPlugin();

				String pid = getPluginsDirectoryPath();
				pid = pid.substring(pid.length(), pid.lastIndexOf("\\"));
				FileInputStream fis;
				String pip = TResources.USER_DIR + "/" + pid + "/";
				// lookup for all plugin properties files
				Vector<File> plugprp = TResources.findFiles(new File(pip), ".properties");
				for (File file : plugprp) {
					Properties prps = new Properties();
					fis = new FileInputStream(file);
					prps.load(fis);

					// append properties to main contant table (no plugin header)
					Vector kls = new Vector(prps.keySet());
					for (int i = 0; i < kls.size(); i++) {
						String pk = (String) kls.elementAt(i);
						String pv = prps.getProperty(pk);
						// TODO: verify. maybe need tobe appended to the main resourcemap or a new resourcemap in the
						// chain
						// TStringUtils.constants.put(pk, pv);
					}
					// add resource path form plugins to main resource path
					// TODO: verify. maybe need tobe appended to the main resourcemap or a new resourcemap in the
					// chain
//					TResources.addResourcePath(pip + pid + "/");
				}
			} catch (Exception e) {
				e.printStackTrace();
				SystemLog.severe("Plugin not instaled");
			}
		}
	}

	public void endPlugins() {
		for (final TPlugin plugin : getAvailablePlugins()) {
			plugin.endPlugin();
		}
	}
}
