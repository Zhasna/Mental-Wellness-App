package com.journal.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtils {
	private static final Gson GSON = new Gson();

	public static <T> T parseJson(BufferedReader reader, Type typeOfT) throws IOException {
		try {
			return GSON.fromJson(reader, typeOfT);
		} catch (JsonSyntaxException ex) {
			throw new IOException("Malformed JSON", ex);
		}
	}

	public static void sendJson(HttpServletResponse response, Object payload) throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(GSON.toJson(payload));
	}

	public static void sendError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		sendJson(response, Map.of("message", message));
	}
}
