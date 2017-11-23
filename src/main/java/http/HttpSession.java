package http;

/**
 * Created by wooseokSong on 2017-11-24.
 */
public interface HttpSession {
	int getId();
	void setAttribute(String name, Object value);
	Object getAttribute(String name);
	void removeAttribute(String name);
	void invalidate();
}
