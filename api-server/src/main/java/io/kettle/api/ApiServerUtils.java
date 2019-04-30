package io.kettle.api;

public class ApiServerUtils {

	public static String formatApiVersion(String group, String version) {
		return String.format("%s/%s", group, version);
	}
	
}
