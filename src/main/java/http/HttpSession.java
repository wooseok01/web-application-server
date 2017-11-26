package http;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wooseokSong on 2017-11-23.
 */
public class HttpSession {
	private String id;
	private Map<String, Object> map = new HashMap<>();

	public HttpSession(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setAttribute(String name, Object value) {
		map.put(name, value);
	}

	public Object getAttribute(String name) {
		return map.get(name);
	}

	public void removeAttribute(String name) {
		map.remove(name);
	}

	public void invalidate() {
		map.clear();
	}
}
