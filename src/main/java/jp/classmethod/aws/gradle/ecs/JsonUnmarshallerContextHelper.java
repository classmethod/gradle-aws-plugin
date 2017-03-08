/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.ecs;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.gradle.api.GradleException;

import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.ListUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.BigDecimalJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.BigIntegerJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.BooleanJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.ByteBufferJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.ByteJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.CharacterJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.DateJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.DoubleJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.FloatJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.IntegerJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.LongJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.ShortJsonUnmarshaller;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller;
import com.amazonaws.transform.Unmarshaller;
import com.amazonaws.util.ImmutableMapParameter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

@SuppressWarnings("PMD.GodClass")
public class JsonUnmarshallerContextHelper {
	public static final Map<Class<?>, Unmarshaller<?, JsonUnmarshallerContext>> JSON_SCALAR_UNMARSHALLERS =
			new ImmutableMapParameter.Builder<Class<?>, Unmarshaller<?, JsonUnmarshallerContext>>()
				.put(String.class, StringJsonUnmarshaller.getInstance())
				.put(Double.class, DoubleJsonUnmarshaller.getInstance())
				.put(Integer.class, IntegerJsonUnmarshaller.getInstance())
				.put(BigInteger.class, BigIntegerJsonUnmarshaller.getInstance())
				.put(BigDecimal.class, BigDecimalJsonUnmarshaller.getInstance())
				.put(Boolean.class, BooleanJsonUnmarshaller.getInstance())
				.put(Float.class, FloatJsonUnmarshaller.getInstance())
				.put(Long.class, LongJsonUnmarshaller.getInstance())
				.put(Byte.class, ByteJsonUnmarshaller.getInstance()).put(Date.class, DateJsonUnmarshaller.getInstance())
				.put(ByteBuffer.class, ByteBufferJsonUnmarshaller.getInstance())
				.put(Character.class, CharacterJsonUnmarshaller.getInstance())
				.put(Short.class, ShortJsonUnmarshaller.getInstance()).build();
	
	
	public static JsonUnmarshallerContext create(String json) throws JsonParseException, IOException {
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jsonParser = jsonFactory.createParser(json);
		return new JsonUnmarshallerContextImpl(jsonParser, JSON_SCALAR_UNMARSHALLERS);
	}
	
	// Since the method(`unmarshall`) throws an exception, I inevitably ignored the exception.
	// com/amazonaws/transform/ListUnmarshaller.java
	// public List<T> unmarshall(JsonUnmarshallerContext context) throws Exception {
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public static <T> List<T> parse(Unmarshaller<T, JsonUnmarshallerContext> unmarshaller, String field, String json) {
		List<T> result = null;
		try {
			JsonUnmarshallerContext context = JsonUnmarshallerContextHelper.create(json);
			result = new ListUnmarshaller<T>(unmarshaller).unmarshall(context);
		} catch (JsonParseException e) {
			throw new GradleException("Invalid JSON(" + field + ") data. JSON parse exception", e);
		} catch (ParseException e) {
			throw new GradleException("Invalid JSON(" + field + ") data. ListUnmarshaller unmarshall(parse) exception",
					e);
		} catch (IOException e) {
			throw new GradleException("Invalid JSON(" + field + ") data. Something's wrong. ... :(", e);
		} catch (Exception e) {
			throw new GradleException("Invalid JSON(" + field + ") data. Something's wrong. ... :(", e);
		}
		
		return result;
	}
	
	// Since the method(`unmarshall`) throws an exception, I inevitably ignored the exception.
	// com/amazonaws/transform/ListUnmarshaller.java
	// public List<T> unmarshall(JsonUnmarshallerContext context) throws Exception {
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public static <T> T parseObject(Unmarshaller<T, JsonUnmarshallerContext> unmarshaller, String field, String json) {
		T result = null;
		try {
			JsonUnmarshallerContext context = JsonUnmarshallerContextHelper.create(json);
			result = unmarshaller.unmarshall(context);
		} catch (JsonParseException e) {
			throw new GradleException("Invalid JSON(" + field + ") data. JSON parse exception", e);
		} catch (ParseException e) {
			throw new GradleException("Invalid JSON(" + field + ") data. ListUnmarshaller unmarshall(parse) exception",
					e);
		} catch (IOException e) {
			throw new GradleException("Invalid JSON(" + field + ") data. Something's wrong. ... :(", e);
		} catch (Exception e) {
			throw new GradleException("Invalid JSON(" + field + ") data. Something's wrong. ... :(", e);
		}
		
		return result;
	}
}

class JsonUnmarshallerContextImpl extends JsonUnmarshallerContext {
	
	private JsonToken currentToken;
	
	private JsonToken nextToken;
	
	private final JsonParser jsonParser;
	
	private final Stack<JsonFieldTokenPair> stack = new Stack<JsonFieldTokenPair>();
	
	private String currentField;
	
	private String lastParsedParentElement;
	
	private Map<String, String> metadata = new HashMap<String, String>();
	
	private final Map<Class<?>, Unmarshaller<?, JsonUnmarshallerContext>> unmarshallerMap;
	
	
	JsonUnmarshallerContextImpl(JsonParser jsonParser,
			Map<Class<?>, Unmarshaller<?, JsonUnmarshallerContext>> mapper) {
		this.jsonParser = jsonParser;
		this.unmarshallerMap = mapper;
	}
	
	@Override
	public int getCurrentDepth() {
		int depth = stack.size();
		if (currentField != null) {
			depth++;
		}
		return depth;
	}
	
	@Override
	public String readText() throws IOException, RuntimeException {
		switch (currentToken) {
			case VALUE_STRING:
				return jsonParser.getText();
			case VALUE_FALSE:
				return "false";
			case VALUE_TRUE:
				return "true";
			case VALUE_NULL:
				return null;
			case VALUE_NUMBER_FLOAT:
			case VALUE_NUMBER_INT:
				return jsonParser.getNumberValue().toString();
			case FIELD_NAME:
				return jsonParser.getText();
			default:
				throw new IllegalArgumentException(
						"We expected a VALUE token but got: " + currentToken);
		}
	}
	
	@Override
	public boolean isInsideResponseHeader() {
		return false;
	}
	
	@Override
	public boolean isStartOfDocument() {
		return jsonParser == null || jsonParser.getCurrentToken() == null;
	}
	
	@Override
	public boolean testExpression(String expression) {
		if (expression.equals(".")) {
			return true;
		}
		
		if (currentField != null) {
			return currentField.equals(expression);
		}
		
		return (!stack.isEmpty())
				&& stack.peek().getField().equals(expression);
	}
	
	@Override
	public String getCurrentParentElement() {
		if (currentField != null) {
			return currentField;
		}
		
		if (!stack.isEmpty()) {
			return stack.peek().getField();
		}
		
		return "";
	}
	
	@Override
	public boolean testExpression(String expression, int stackDepth) {
		if (expression.equals(".")) {
			return true;
		}
		
		return testExpression(expression)
				&& stackDepth == getCurrentDepth();
	}
	
	@Override
	public JsonToken nextToken() throws IOException {
		JsonToken token = (nextToken != null) ? nextToken : jsonParser.nextToken();
		
		this.currentToken = token;
		nextToken = null;
		
		updateContext();
		return token;
	}
	
	@Override
	public JsonToken peek() throws IOException {
		if (nextToken != null) {
			return nextToken;
		}
		
		nextToken = jsonParser.nextToken();
		return nextToken;
	}
	
	@Override
	public JsonParser getJsonParser() {
		return jsonParser;
	}
	
	@Override
	public Map<String, String> getMetadata() {
		return metadata;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Unmarshaller<T, JsonUnmarshallerContext> getUnmarshaller(Class<T> type) {
		return (Unmarshaller<T, JsonUnmarshallerContext>) unmarshallerMap.get(type);
	}
	
	@Override
	public JsonToken getCurrentToken() {
		return currentToken;
	}
	
	@SuppressWarnings({
		"PMD.CyclomaticComplexity",
		"PMD.AvoidDeeplyNestedIfStmts"
	})
	private void updateContext() throws IOException {
		lastParsedParentElement = null;
		if (currentToken == null) {
			return;
		}
		
		if (currentToken == START_OBJECT || currentToken == START_ARRAY) {
			if (currentField != null) {
				stack.push(new JsonFieldTokenPair(currentField, currentToken));
				currentField = null;
			}
			return;
		}
		
		if (currentToken == END_OBJECT || currentToken == END_ARRAY) {
			if (!stack.isEmpty()) {
				boolean squareBracketsMatch = currentToken == END_ARRAY && stack.peek().getToken() == START_ARRAY;
				boolean curlyBracketsMatch = currentToken == END_OBJECT && stack.peek().getToken() == START_OBJECT;
				if (squareBracketsMatch || curlyBracketsMatch) {
					lastParsedParentElement = stack.pop().getField();
				}
			}
			currentField = null;
			return;
		}
		
		if (currentToken == FIELD_NAME) {
			currentField = jsonParser.getText();
			return;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder stackString = new StringBuilder();
		
		for (JsonFieldTokenPair jsonFieldTokenPair : stack) {
			stackString.append('/')
				.append(jsonFieldTokenPair.getField());
		}
		
		if (currentField != null) {
			stackString.append('/')
				.append(currentField);
		}
		
		return stackString.length() == 0 ? "/" : stackString.toString();
	}
	
	@Override
	public String getLastParsedParentElement() {
		return lastParsedParentElement;
	}
	
	
	private static class JsonFieldTokenPair {
		
		private final String field;
		
		private final JsonToken jsonToken;
		
		
		JsonFieldTokenPair(String fieldString, JsonToken token) {
			field = fieldString;
			jsonToken = token;
		}
		
		public String getField() {
			return field;
		}
		
		public JsonToken getToken() {
			return jsonToken;
		}
		
		public String toString() {
			return field + ": " + jsonToken.asString();
		}
	}
}
