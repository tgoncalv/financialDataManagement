package fr.moneyManaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.dbManaging.DbConnection;

/**
 * A profile can contain numerous accounts which are possessed by the same person.
 * Every informations about this profile will be stored in \src\main\ressources\'profileName'
 * @author taiga
 *
 */
public class Profile {
	protected String m_profileName;
	protected String m_profileResourcesPath;

	Profile(String profileName) {
		m_profileName = profileName;
		m_profileResourcesPath = DbConnection.getResourcesEquivalentPath(profileName);
	}

	/**
	 * Get access to the list of profiles, which are stored in \src\main\resources
	 * path
	 * 
	 * @return Returns the list of profiles created.
	 */
	public static List<String> showProfileList() {
		String profilesPath = "C:\\dev\\moneyManager";
		File profiles = new File(profilesPath);
		List<String> profileList = new ArrayList<String>();
		Collections.addAll(profileList, profiles.list());
		int i = 0;
		File file = new File(profilesPath);
		for (String profile : profiles.list()) {
			file = new File(profilesPath + "\\" + profile);
			if (file.isFile()) {
				profileList.remove(i);
			}
			i += 1;
		}
		return profileList;
	}

	/**
	 * Delete an existing profiles (else does nothing)
	 * 
	 * @param profile Select the profile to delete
	 */
	public static void deleteProfile(String profile) {
		List<String> profileList = new ArrayList<String>();
		profileList = showProfileList();
		if (profileList.contains(profile)) {
			String profilePath = "C:\\dev\\moneyManager\\" + profile;
			File fileToDelete = new File(profilePath);
			for (File file : fileToDelete.listFiles()) {
				file.delete(); //Need to delete every files contained in the folder before deleting the folder itself
			}
			fileToDelete.delete();
		}
	}

	/**
	 * Create a new profile (if the name is not already used)
	 * @param profile	Name of the profile to create
	 */
	public static void createProfile(String profile) {
		List<String> profileList = new ArrayList<String>();
		profileList = showProfileList();
		if (!profileList.contains(profile)) {
			String profilePath = "C:\\dev\\moneyManager\\" + profile;
			File fileToCreate = new File(profilePath);
			fileToCreate.mkdir();
			DbConnection.initializeDb(profile);
		}
	}
}
