package io.kettle.api;

public class ApiServerUtils {

	public static String formatApiVersion(String group, String version) {
	    if (group == null) {
	        return version;
	    }
		return String.format("%s/%s", group, version);
	}

}
