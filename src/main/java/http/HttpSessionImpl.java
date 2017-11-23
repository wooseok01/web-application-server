package http;

import java.util.Map;

/**
 * Created by wooseokSong on 2017-11-23.
 */
public class HttpSessionImpl implements HttpSession {
	private int id;
	private Map<String, Object> map;

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setAttribute(String name, Object value) {

	}

	@Override
	public Object getAttribute(String name) {
		return map.get(name);
	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public void invalidate() {

	}
}
