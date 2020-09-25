package execute;

import java.util.HashMap;
import java.util.Map;

public class TagHandler {

	private Map<String, Float> floats;

	public TagHandler() {
		floats = new HashMap<>();
	}

	/**
	 * @param key the key of the value to be returned
	 * @return the value of the given key in the game save state
	 **/
	public float getFloat(String key) {
		return floats.getOrDefault(key, 0.0f);
	}

	/**
	 * @param key   the key to be set in the saveGame
	 * @param value the value that should be assigned to the key in the current saveGame
	 **/
	public void setValue(String key, float value) {
		floats.put(key, value);
	}

	public boolean hasKey(String key) {
		return floats.containsKey(key);
	}

	/**
	 * removes all values from the loaded saveGame
	 **/
	public void clearValues() {
		floats.clear();
	}
}