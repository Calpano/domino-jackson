/*
 * Copyright © 2019 Dominokit
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
// @formatter:off
/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dominokit.jackson.stream.impl;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dominokit.jackson.JacksonContextProvider;
import org.dominokit.jackson.exception.JsonDeserializationException;
import org.dominokit.jackson.stream.JsonToken;
import org.dominokit.jackson.stream.JsonWriter;
import org.dominokit.jackson.stream.Stack;

/**
 * Reads a JSON (<a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>) encoded value as a
 * stream of tokens. This stream includes both literal values (strings, numbers, booleans, and
 * nulls) as well as the begin and end delimiters of objects and arrays. The tokens are traversed in
 * depth-first order, the same order that they appear in the JSON document. Within JSON objects,
 * name/value pairs are represented by a single token.
 *
 * <h2>Parsing JSON</h2>
 *
 * To create a recursive descent parser for your own JSON streams, first create an entry point
 * method that creates a {@code JsonReader}.
 *
 * <p>Next, create handler methods for each structure in your JSON text. You'll need a method for
 * each object type and for each array type.
 *
 * <ul>
 *   <li>Within <strong>array handling</strong> methods, first call {@link #beginArray} to consume
 *       the array's opening bracket. Then create a while loop that accumulates values, terminating
 *       when {@link #hasNext} is false. Finally, read the array's closing bracket by calling {@link
 *       #endArray}.
 *   <li>Within <strong>object handling</strong> methods, first call {@link #beginObject} to consume
 *       the object's opening brace. Then create a while loop that assigns values to local variables
 *       based on their name. This loop should terminate when {@link #hasNext} is false. Finally,
 *       read the object's closing brace by calling {@link #endObject}.
 * </ul>
 *
 * <p>When a nested object or array is encountered, delegate to the corresponding handler method.
 *
 * <p>When an unknown name is encountered, strict parsers should fail with an exception. Lenient
 * parsers should call {@link #skipValue()} to recursively skip the value's nested tokens, which may
 * otherwise conflict.
 *
 * <p>If a value may be null, you should first check using {@link #peek()}. Null literals can be
 * consumed using either {@link #nextNull()} or {@link #skipValue()}.
 *
 * <h2>Example</h2>
 *
 * Suppose we'd like to parse a stream of messages such as the following:
 *
 * <pre>{@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "How do I read a JSON stream in Java?",
 *     "geo": null,
 *     "user": {
 *       "name": "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@json_newb just use JsonReader!",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]
 * }</pre>
 *
 * This code implements the parser for the above structure:
 *
 * <pre>{@code
 * <p>
 *   public List<Message> readJsonStream(InputStream in) {
 *     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
 *     try {
 *       return readMessagesArray(reader);
 *     } finally {
 *       reader.close();
 *     }
 *   }
 * <p>
 *   public List<Message> readMessagesArray(JsonReader reader) {
 *     List<Message> messages = new ArrayList<Message>();
 * <p>
 *     reader.beginArray();
 *     while (reader.hasNext()) {
 *       messages.add(readMessage(reader));
 *     }
 *     reader.endArray();
 *     return messages;
 *   }
 * <p>
 *   public Message readMessage(JsonReader reader) {
 *     long id = -1;
 *     String text = null;
 *     User user = null;
 *     List<Double> geo = null;
 * <p>
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *       String name = reader.nextName();
 *       if (name.equals("id")) {
 *         id = reader.nextLong();
 *       } else if (name.equals("text")) {
 *         text = reader.nextString();
 *       } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
 *         geo = readDoublesArray(reader);
 *       } else if (name.equals("user")) {
 *         user = readUser(reader);
 *       } else {
 *         reader.skipValue();
 *       }
 *     }
 *     reader.endObject();
 *     return new Message(id, text, user, geo);
 *   }
 * <p>
 *   public List<Double> readDoublesArray(JsonReader reader) {
 *     List<Double> doubles = new ArrayList<Double>();
 * <p>
 *     reader.beginArray();
 *     while (reader.hasNext()) {
 *       doubles.add(reader.nextDouble());
 *     }
 *     reader.endArray();
 *     return doubles;
 *   }
 * <p>
 *   public User readUser(JsonReader reader) {
 *     String username = null;
 *     int followersCount = -1;
 * <p>
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *       String name = reader.nextName();
 *       if (name.equals("name")) {
 *         username = reader.nextString();
 *       } else if (name.equals("followers_count")) {
 *         followersCount = reader.nextInt();
 *       } else {
 *         reader.skipValue();
 *       }
 *     }
 *     reader.endObject();
 *     return new User(username, followersCount);
 *   }
 * }</pre>
 *
 * <h2>Number Handling</h2>
 *
 * This reader permits numeric values to be read as strings and string values to be read as numbers.
 * For example, both elements of the JSON array {@code [1, "1"]} may be read using either {@link
 * #nextInt} or {@link #nextString}. This behavior is intended to prevent lossy numeric conversions:
 * double is JavaScript's only numeric type and very large values like {@code 9007199254740993}
 * cannot be represented exactly on that platform. To minimize precision loss, extremely large
 * values should be written and read as strings in JSON.
 *
 * <h2>Non-Execute Prefix</h2>
 *
 * Web servers that serve private data using JSON may be vulnerable to <a
 * href="http://en.wikipedia.org/wiki/JSON#Cross-site_request_forgery">Cross-site request
 * forgery</a> attacks. In such an attack, a malicious site gains access to a private JSON file by
 * executing it with an HTML {@code <script>} tag.
 *
 * <p>Prefixing JSON files with <code>")]}'\n"</code> makes them non-executable by {@code <script>}
 * tags, disarming the attack. Since the prefix is malformed JSON, strict parsing fails when it is
 * encountered. This class permits the non-execute prefix when {@link #setLenient(boolean) lenient
 * parsing} is enabled.
 *
 * <p>Each {@code JsonReader} may be used to read a single JSON stream. Instances of this class are
 * not thread safe.
 */
public class DefaultJsonReader implements org.dominokit.jackson.stream.JsonReader {
  private static final Logger logger = Logger.getLogger("JsonReader");

  /** The only non-execute prefix this parser permits */
  private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();

  private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

  private static final long MIN_INT_L = (long) Integer.MIN_VALUE;
  private static final long MAX_INT_L = (long) Integer.MAX_VALUE;

  private static final BigInteger MIN_LONG_BIGINTEGER = new BigInteger("" + Long.MIN_VALUE);
  private static final BigInteger MAX_LONG_BIGINTEGER = new BigInteger("" + Long.MAX_VALUE);

  private static final int PEEKED_NONE = 0;
  private static final int PEEKED_BEGIN_OBJECT = 1;
  private static final int PEEKED_END_OBJECT = 2;
  private static final int PEEKED_BEGIN_ARRAY = 3;
  private static final int PEEKED_END_ARRAY = 4;
  private static final int PEEKED_TRUE = 5;
  private static final int PEEKED_FALSE = 6;
  private static final int PEEKED_NULL = 7;
  private static final int PEEKED_SINGLE_QUOTED = 8;
  private static final int PEEKED_DOUBLE_QUOTED = 9;
  private static final int PEEKED_UNQUOTED = 10;
  /** When this is returned, the string value is stored in peekedString. */
  private static final int PEEKED_BUFFERED = 11;

  private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
  private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
  private static final int PEEKED_UNQUOTED_NAME = 14;
  /** When this is returned, the integer value is stored in peekedLong. */
  private static final int PEEKED_LONG = 15;

  private static final int PEEKED_NUMBER = 16;
  private static final int PEEKED_EOF = 17;

  /* State machine when parsing numbers */
  private static final int NUMBER_CHAR_NONE = 0;
  private static final int NUMBER_CHAR_SIGN = 1;
  private static final int NUMBER_CHAR_DIGIT = 2;
  private static final int NUMBER_CHAR_DECIMAL = 3;
  private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
  private static final int NUMBER_CHAR_EXP_E = 5;
  private static final int NUMBER_CHAR_EXP_SIGN = 6;
  private static final int NUMBER_CHAR_EXP_DIGIT = 7;

  /** The input JSON. */
  private final StringReader in;

  /** True to accept non-spec compliant JSON */
  private boolean lenient = false;

  /**
   * Use a manual buffer to easily read and unread upcoming characters, and also so we can create
   * strings without an intermediate StringBuilder. We decode literals directly out of this buffer,
   * so it must be at least as long as the longest token that can be reported as a number.
   */
  private final char[] buffer = new char[1024];

  private int pos = 0;
  private int limit = 0;

  private int lineNumber = 0;
  private int lineStart = 0;

  private int peeked = PEEKED_NONE;

  /**
   * A peeked value that was composed entirely of digits with an optional leading dash. Positive
   * values may not have a leading 0.
   */
  private long peekedLong;

  /**
   * The number of characters in a peeked number literal. Increment 'pos' by this after reading a
   * number.
   */
  private int peekedNumberLength;

  /**
   * A peeked string that should be parsed on the next double, long or string. This is populated
   * before a numeric value is parsed and used if that parsing fails.
   */
  private String peekedString;

  /*
   * The nesting stack. Using a manual array rather than an ArrayList saves 20%.
   */
  private Stack<Integer> stack = JacksonContextProvider.get().integerStackFactory().make();
  //  private int stackSize = 0;

  {
    stack.push(JsonScope.EMPTY_DOCUMENT);
  }

  /**
   * Creates a new instance that reads a JSON-encoded stream from {@code in}.
   *
   * @param in a {@link org.dominokit.jackson.stream.impl.StringReader} object.
   */
  public DefaultJsonReader(StringReader in) {
    if (in == null) {
      throw new NullPointerException("in == null");
    }
    this.in = in;
  }

  /** {@inheritDoc} */
  @Override
  public final void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  /**
   * Returns true if this parser is liberal in what it accepts.
   *
   * @return a boolean.
   */
  public final boolean isLenient() {
    return lenient;
  }

  /** {@inheritDoc} */
  @Override
  public void beginArray() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_ARRAY) {
      push(JsonScope.EMPTY_ARRAY);
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException(
          "Expected BEGIN_ARRAY but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void endArray() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_ARRAY) {
      stack.pop();
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException(
          "Expected END_ARRAY but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void beginObject() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_OBJECT) {
      push(JsonScope.EMPTY_OBJECT);
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException(
          "Expected BEGIN_OBJECT but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void endObject() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_OBJECT) {
      stack.pop();
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException(
          "Expected END_OBJECT but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasNext() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY;
  }

  /** {@inheritDoc} */
  @Override
  public JsonToken peek() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    switch (p) {
      case PEEKED_BEGIN_OBJECT:
        return JsonToken.BEGIN_OBJECT;
      case PEEKED_END_OBJECT:
        return JsonToken.END_OBJECT;
      case PEEKED_BEGIN_ARRAY:
        return JsonToken.BEGIN_ARRAY;
      case PEEKED_END_ARRAY:
        return JsonToken.END_ARRAY;
      case PEEKED_SINGLE_QUOTED_NAME:
      case PEEKED_DOUBLE_QUOTED_NAME:
      case PEEKED_UNQUOTED_NAME:
        return JsonToken.NAME;
      case PEEKED_TRUE:
      case PEEKED_FALSE:
        return JsonToken.BOOLEAN;
      case PEEKED_NULL:
        return JsonToken.NULL;
      case PEEKED_SINGLE_QUOTED:
      case PEEKED_DOUBLE_QUOTED:
      case PEEKED_UNQUOTED:
      case PEEKED_BUFFERED:
        return JsonToken.STRING;
      case PEEKED_LONG:
      case PEEKED_NUMBER:
        return JsonToken.NUMBER;
      case PEEKED_EOF:
        return JsonToken.END_DOCUMENT;
      default:
        throw new AssertionError();
    }
  }

  private int doPeek() {

    final int peekStack = stack.peek();
    if (peekStack == JsonScope.EMPTY_ARRAY) {
      stack.replaceTop(JsonScope.NONEMPTY_ARRAY);
    } else if (peekStack == JsonScope.NONEMPTY_ARRAY) {
      // Look for a comma before the next element.
      int c = nextNonWhitespace(true);
      switch (c) {
        case ']':
          return peeked = PEEKED_END_ARRAY;
        case ';':
          checkLenient(); // fall-through
        case ',':
          break;
        default:
          throw syntaxError("Unterminated array");
      }
    } else if (peekStack == JsonScope.EMPTY_OBJECT || peekStack == JsonScope.NONEMPTY_OBJECT) {
      stack.replaceTop(JsonScope.DANGLING_NAME);
      // Look for a comma before the next element.
      if (peekStack == JsonScope.NONEMPTY_OBJECT) {
        int c = nextNonWhitespace(true);
        switch (c) {
          case '}':
            return peeked = PEEKED_END_OBJECT;
          case ';':
            checkLenient(); // fall-through
          case ',':
            break;
          default:
            throw syntaxError("Unterminated object");
        }
      }
      int c = nextNonWhitespace(true);
      switch (c) {
        case '"':
          return peeked = PEEKED_DOUBLE_QUOTED_NAME;
        case '\'':
          checkLenient();
          return peeked = PEEKED_SINGLE_QUOTED_NAME;
        case '}':
          if (peekStack != JsonScope.NONEMPTY_OBJECT) {
            return peeked = PEEKED_END_OBJECT;
          } else {
            throw syntaxError("Expected name");
          }
        default:
          checkLenient();
          pos--; // Don't consume the first character in an unquoted string.
          if (isLiteral((char) c)) {
            return peeked = PEEKED_UNQUOTED_NAME;
          } else {
            throw syntaxError("Expected name");
          }
      }
    } else if (peekStack == JsonScope.DANGLING_NAME) {
      stack.replaceTop(JsonScope.NONEMPTY_OBJECT);
      // Look for a colon before the value.
      int c = nextNonWhitespace(true);
      switch (c) {
        case ':':
          break;
        case '=':
          checkLenient();
          if ((pos < limit || fillBuffer(1)) && buffer[pos] == '>') {
            pos++;
          }
          break;
        default:
          throw syntaxError("Expected ':'");
      }
    } else if (peekStack == JsonScope.EMPTY_DOCUMENT) {
      if (lenient) {
        consumeNonExecutePrefix();
      }
      stack.replaceTop(JsonScope.NONEMPTY_DOCUMENT);
    } else if (peekStack == JsonScope.NONEMPTY_DOCUMENT) {
      int c = nextNonWhitespace(false);
      if (c == -1) {
        return peeked = PEEKED_EOF;
      } else {
        checkLenient();
        pos--;
      }
    } else if (peekStack == JsonScope.CLOSED) {
      throw new IllegalStateException("JsonReader is closed");
    }

    int c = nextNonWhitespace(true);
    switch (c) {
      case ']':
        if (peekStack == JsonScope.EMPTY_ARRAY) {
          return peeked = PEEKED_END_ARRAY;
        }
        // fall-through to handle ",]"
      case ';':
      case ',':
        // In lenient mode, a 0-length literal in an array means 'null'.
        if (peekStack == JsonScope.EMPTY_ARRAY || peekStack == JsonScope.NONEMPTY_ARRAY) {
          checkLenient();
          pos--;
          return peeked = PEEKED_NULL;
        } else {
          throw syntaxError("Unexpected value");
        }
      case '\'':
        checkLenient();
        return peeked = PEEKED_SINGLE_QUOTED;
      case '"':
        if (stack.size() == 1) {
          checkLenient();
        }
        return peeked = PEEKED_DOUBLE_QUOTED;
      case '[':
        return peeked = PEEKED_BEGIN_ARRAY;
      case '{':
        return peeked = PEEKED_BEGIN_OBJECT;
      default:
        pos--; // Don't consume the first character in a literal value.
    }

    if (stack.size() == 1) {
      checkLenient(); // Top-level value isn't an array or an object.
    }

    int result = peekKeyword();
    if (result != PEEKED_NONE) {
      return result;
    }

    result = peekNumber();
    if (result != PEEKED_NONE) {
      return result;
    }

    if (!isLiteral(buffer[pos])) {
      throw syntaxError("Expected value");
    }

    checkLenient();
    return peeked = PEEKED_UNQUOTED;
  }

  private int peekKeyword() {
    // Figure out which keyword we're matching against by its first character.
    char c = buffer[pos];
    String keyword;
    String keywordUpper;
    int peeking;
    if (c == 't' || c == 'T') {
      keyword = "true";
      keywordUpper = "TRUE";
      peeking = PEEKED_TRUE;
    } else if (c == 'f' || c == 'F') {
      keyword = "false";
      keywordUpper = "FALSE";
      peeking = PEEKED_FALSE;
    } else if (c == 'n' || c == 'N') {
      keyword = "null";
      keywordUpper = "NULL";
      peeking = PEEKED_NULL;
    } else {
      return PEEKED_NONE;
    }

    // Confirm that chars [1..length) match the keyword.
    int length = keyword.length();
    for (int i = 1; i < length; i++) {
      if (pos + i >= limit && !fillBuffer(i + 1)) {
        return PEEKED_NONE;
      }
      c = buffer[pos + i];
      if (c != keyword.charAt(i) && c != keywordUpper.charAt(i)) {
        return PEEKED_NONE;
      }
    }

    if ((pos + length < limit || fillBuffer(length + 1)) && isLiteral(buffer[pos + length])) {
      return PEEKED_NONE; // Don't match trues, falsey or nullsoft!
    }

    // We've found the keyword followed either by EOF or by a non-literal character.
    pos += length;
    return peeked = peeking;
  }

  private int peekNumber() {
    // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
    char[] buffer = this.buffer;
    int p = pos;
    int l = limit;

    long value = 0; // Negative to accommodate Long.MIN_VALUE more easily.
    boolean negative = false;
    boolean fitsInLong = true;
    int last = NUMBER_CHAR_NONE;

    int i = 0;

    charactersOfNumber:
    for (; true; i++) {
      if (p + i == l) {
        if (i == buffer.length) {
          // Though this looks like a well-formed number, it's too long to continue reading. Give up
          // and let the application handle this as an unquoted literal.
          return PEEKED_NONE;
        }
        if (!fillBuffer(i + 1)) {
          break;
        }
        p = pos;
        l = limit;
      }

      char c = buffer[p + i];
      switch (c) {
        case '-':
          if (last == NUMBER_CHAR_NONE) {
            negative = true;
            last = NUMBER_CHAR_SIGN;
            continue;
          } else if (last == NUMBER_CHAR_EXP_E) {
            last = NUMBER_CHAR_EXP_SIGN;
            continue;
          }
          return PEEKED_NONE;

        case '+':
          if (last == NUMBER_CHAR_EXP_E) {
            last = NUMBER_CHAR_EXP_SIGN;
            continue;
          }
          return PEEKED_NONE;

        case 'e':
        case 'E':
          if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
            last = NUMBER_CHAR_EXP_E;
            continue;
          }
          return PEEKED_NONE;

        case '.':
          if (last == NUMBER_CHAR_DIGIT) {
            last = NUMBER_CHAR_DECIMAL;
            continue;
          }
          return PEEKED_NONE;

        default:
          if (c < '0' || c > '9') {
            if (!isLiteral(c)) {
              break charactersOfNumber;
            }
            return PEEKED_NONE;
          }
          if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
            value = -(c - '0');
            last = NUMBER_CHAR_DIGIT;
          } else if (last == NUMBER_CHAR_DIGIT) {
            if (value == 0) {
              return PEEKED_NONE; // Leading '0' prefix is not allowed (since it could be octal).
            }
            long newValue = value * 10 - (c - '0');
            fitsInLong &=
                value > MIN_INCOMPLETE_INTEGER
                    || (value == MIN_INCOMPLETE_INTEGER && newValue < value);
            value = newValue;
          } else if (last == NUMBER_CHAR_DECIMAL) {
            last = NUMBER_CHAR_FRACTION_DIGIT;
          } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
            last = NUMBER_CHAR_EXP_DIGIT;
          }
      }
    }

    // We've read a complete number. Decide if it's a PEEKED_LONG or a PEEKED_NUMBER.
    if (last == NUMBER_CHAR_DIGIT && fitsInLong && (value != Long.MIN_VALUE || negative)) {
      peekedLong = negative ? value : -value;
      pos += i;
      return peeked = PEEKED_LONG;
    } else if (last == NUMBER_CHAR_DIGIT
        || last == NUMBER_CHAR_FRACTION_DIGIT
        || last == NUMBER_CHAR_EXP_DIGIT) {
      peekedNumberLength = i;
      return peeked = PEEKED_NUMBER;
    } else {
      return PEEKED_NONE;
    }
  }

  private boolean isLiteral(char c) {
    switch (c) {
      case '/':
      case '\\':
      case ';':
      case '#':
      case '=':
        checkLenient(); // fall-through
      case '{':
      case '}':
      case '[':
      case ']':
      case ':':
      case ',':
      case ' ':
      case '\t':
      case '\f':
      case '\r':
      case '\n':
        return false;
      default:
        return true;
    }
  }

  /** {@inheritDoc} */
  @Override
  public String nextName() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    String result;
    if (p == PEEKED_UNQUOTED_NAME) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
      result = nextQuotedValue('\'');
    } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
      result = nextQuotedValue('"');
    } else {
      throw new IllegalStateException(
          "Expected a name but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
    peeked = PEEKED_NONE;
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public String nextString() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    String result;
    if (p == PEEKED_UNQUOTED) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_SINGLE_QUOTED) {
      result = nextQuotedValue('\'');
    } else if (p == PEEKED_DOUBLE_QUOTED) {
      result = nextQuotedValue('"');
    } else if (p == PEEKED_BUFFERED) {
      result = peekedString;
      peekedString = null;
    } else if (p == PEEKED_LONG) {
      result = Long.toString(peekedLong);
    } else if (p == PEEKED_NUMBER) {
      result = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else {
      throw new IllegalStateException(
          "Expected a string but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
    peeked = PEEKED_NONE;
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean nextBoolean() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_TRUE) {
      peeked = PEEKED_NONE;
      return true;
    } else if (p == PEEKED_FALSE) {
      peeked = PEEKED_NONE;
      return false;
    }
    throw new IllegalStateException(
        "Expected a boolean but was "
            + peek()
            + " at line "
            + getLineNumber()
            + " column "
            + getColumnNumber());
  }

  /** {@inheritDoc} */
  @Override
  public void nextNull() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_NULL) {
      peeked = PEEKED_NONE;
    } else {
      throw new IllegalStateException(
          "Expected null but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
  }

  /** {@inheritDoc} */
  @Override
  public double nextDouble() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      return (double) peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
    } else if (p == PEEKED_UNQUOTED) {
      peekedString = nextUnquotedValue();
    } else if (p != PEEKED_BUFFERED) {
      throw new IllegalStateException(
          "Expected a double but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }

    peeked = PEEKED_BUFFERED;
    double result = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    if (!lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw syntaxError("JSON forbids NaN and infinities: " + result);
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public long nextLong() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      return peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
      try {
        long result = Long.parseLong(peekedString);
        peeked = PEEKED_NONE;
        return result;
      } catch (NumberFormatException ignored) {
        // Fall back to parse as a double below.
      }
    } else {
      throw new IllegalStateException(
          "Expected a long but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }

    peeked = PEEKED_BUFFERED;
    double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    long result = (long) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'long'.
      throw new NumberFormatException(
          "Expected a long but was "
              + peekedString
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    return result;
  }

  /**
   * Returns the string up to but not including {@code quote}, unescaping any character escape
   * sequences encountered along the way. The opening quote should have already been read. This
   * consumes the closing quote, but does not include it in the returned string.
   *
   * @param quote either ' or ".
   * @throws NumberFormatException if any unicode escape sequences are malformed.
   */
  private String nextQuotedValue(char quote) {
    // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
    char[] buffer = this.buffer;
    StringBuilder builder = new StringBuilder();
    while (true) {
      int p = pos;
      int l = limit;
      /* the index of the first character not yet appended to the builder. */
      int start = p;
      while (p < l) {
        int c = buffer[p++];

        if (c == quote) {
          pos = p;
          builder.append(buffer, start, p - start - 1);
          return builder.toString();
        } else if (c == '\\') {
          pos = p;
          builder.append(buffer, start, p - start - 1);
          builder.append(readEscapeCharacter());
          p = pos;
          l = limit;
          start = p;
        } else if (c == '\n') {
          lineNumber++;
          lineStart = p;
        }
      }

      builder.append(buffer, start, p - start);
      pos = p;
      if (!fillBuffer(1)) {
        throw syntaxError("Unterminated string");
      }
    }
  }

  /** Returns an unquoted value as a string. */
  @SuppressWarnings("fallthrough")
  private String nextUnquotedValue() {
    StringBuilder builder = null;
    int i = 0;

    findNonLiteralCharacter:
    while (true) {
      for (; pos + i < limit; i++) {
        switch (buffer[pos + i]) {
          case '/':
          case '\\':
          case ';':
          case '#':
          case '=':
            checkLenient(); // fall-through
          case '{':
          case '}':
          case '[':
          case ']':
          case ':':
          case ',':
          case ' ':
          case '\t':
          case '\f':
          case '\r':
          case '\n':
            break findNonLiteralCharacter;
        }
      }

      // Attempt to load the entire literal into the buffer at once.
      if (i < buffer.length) {
        if (fillBuffer(i + 1)) {
          continue;
        } else {
          break;
        }
      }

      // use a StringBuilder when the value is too long. This is too long to be a number!
      if (builder == null) {
        builder = new StringBuilder();
      }
      builder.append(buffer, pos, i);
      pos += i;
      i = 0;
      if (!fillBuffer(1)) {
        break;
      }
    }

    String result;
    if (builder == null) {
      result = new String(buffer, pos, i);
    } else {
      builder.append(buffer, pos, i);
      result = builder.toString();
    }
    pos += i;
    return result;
  }

  private void skipQuotedValue(char quote) {
    // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
    char[] buffer = this.buffer;
    do {
      int p = pos;
      int l = limit;
      /* the index of the first character not yet appended to the builder. */
      while (p < l) {
        int c = buffer[p++];
        if (c == quote) {
          pos = p;
          return;
        } else if (c == '\\') {
          pos = p;
          readEscapeCharacter();
          p = pos;
          l = limit;
        } else if (c == '\n') {
          lineNumber++;
          lineStart = p;
        }
      }
      pos = p;
    } while (fillBuffer(1));
    throw syntaxError("Unterminated string");
  }

  private void skipUnquotedValue() {
    do {
      int i = 0;
      for (; pos + i < limit; i++) {
        switch (buffer[pos + i]) {
          case '/':
          case '\\':
          case ';':
          case '#':
          case '=':
            checkLenient(); // fall-through
          case '{':
          case '}':
          case '[':
          case ']':
          case ':':
          case ',':
          case ' ':
          case '\t':
          case '\f':
          case '\r':
          case '\n':
            pos += i;
            return;
        }
      }
      pos += i;
    } while (fillBuffer(1));
  }

  /** {@inheritDoc} */
  @Override
  public int nextInt() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    int result;
    if (p == PEEKED_LONG) {
      result = (int) peekedLong;
      if (peekedLong != result) { // Make sure no precision was lost casting to 'int'.
        throw new NumberFormatException(
            "Expected an int but was "
                + peekedLong
                + " at line "
                + getLineNumber()
                + " column "
                + getColumnNumber());
      }
      peeked = PEEKED_NONE;
      return result;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
      try {
        result = Integer.parseInt(peekedString);
        peeked = PEEKED_NONE;
        return result;
      } catch (NumberFormatException ignored) {
        // Fall back to parse as a double below.
      }
    } else {
      throw new IllegalStateException(
          "Expected an int but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }

    peeked = PEEKED_BUFFERED;
    double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    result = (int) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'int'.
      throw new NumberFormatException(
          "Expected an int but was "
              + peekedString
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    peeked = PEEKED_NONE;
    stack.clear();
    stack.push(JsonScope.CLOSED);
  }

  /** {@inheritDoc} */
  @Override
  public void skipValue() {
    int count = 0;
    do {
      int p = peeked;
      if (p == PEEKED_NONE) {
        p = doPeek();
      }

      if (p == PEEKED_BEGIN_ARRAY) {
        push(JsonScope.EMPTY_ARRAY);
        count++;
      } else if (p == PEEKED_BEGIN_OBJECT) {
        push(JsonScope.EMPTY_OBJECT);
        count++;
      } else if (p == PEEKED_END_ARRAY) {
        stack.pop();
        count--;
      } else if (p == PEEKED_END_OBJECT) {
        stack.pop();
        count--;
      } else if (p == PEEKED_UNQUOTED_NAME || p == PEEKED_UNQUOTED) {
        skipUnquotedValue();
      } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_SINGLE_QUOTED_NAME) {
        skipQuotedValue('\'');
      } else if (p == PEEKED_DOUBLE_QUOTED || p == PEEKED_DOUBLE_QUOTED_NAME) {
        skipQuotedValue('"');
      } else if (p == PEEKED_NUMBER) {
        pos += peekedNumberLength;
      }
      peeked = PEEKED_NONE;
    } while (count != 0);
  }

  private void push(int newTop) {
    stack.push(newTop);
  }

  /**
   * Returns true once {@code limit - pos >= minimum}. If the data is exhausted before that many
   * characters are available, this returns false.
   */
  private boolean fillBuffer(int minimum) {
    char[] buffer = this.buffer;
    lineStart -= pos;
    if (limit != pos) {
      limit -= pos;
      System.arraycopy(buffer, pos, buffer, 0, limit);
    } else {
      limit = 0;
    }

    pos = 0;
    int total;
    while ((total = in.read(buffer, limit, buffer.length - limit)) != -1) {
      limit += total;

      // if this is the first read, consume an optional byte order mark (BOM) if it exists
      if (lineNumber == 0 && lineStart == 0 && limit > 0 && buffer[0] == '\ufeff') {
        pos++;
        lineStart++;
        minimum++;
      }

      if (limit >= minimum) {
        return true;
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public int getLineNumber() {
    return lineNumber + 1;
  }

  /** {@inheritDoc} */
  @Override
  public int getColumnNumber() {
    return pos - lineStart + 1;
  }

  /**
   * Returns the next character in the stream that is neither whitespace nor a part of a comment.
   * When this returns, the returned character is always at {@code buffer[pos-1]}; this means the
   * caller can always push back the returned character by decrementing {@code pos}.
   */
  private int nextNonWhitespace(boolean throwOnEof) {
    /*
     * This code uses ugly local variables 'p' and 'l' representing the 'pos'
     * and 'limit' fields respectively. Using locals rather than fields saves
     * a few field reads for each whitespace character in a pretty-printed
     * document, resulting in a 5% speedup. We need to flush 'p' to its field
     * before any (potentially indirect) call to fillBuffer() and reread both
     * 'p' and 'l' after any (potentially indirect) call to the same method.
     */
    char[] buffer = this.buffer;
    int p = pos;
    int l = limit;
    while (true) {
      if (p == l) {
        pos = p;
        if (!fillBuffer(1)) {
          break;
        }
        p = pos;
        l = limit;
      }

      int c = buffer[p++];
      if (c == '\n') {
        lineNumber++;
        lineStart = p;
        continue;
      } else if (c == ' ' || c == '\r' || c == '\t') {
        continue;
      }

      if (c == '/') {
        pos = p;
        if (p == l) {
          pos--; // push back '/' so it's still in the buffer when this method returns
          boolean charsLoaded = fillBuffer(2);
          pos++; // consume the '/' again
          if (!charsLoaded) {
            return c;
          }
        }

        checkLenient();
        char peek = buffer[pos];
        switch (peek) {
          case '*':
            // skip a /* c-style comment */
            pos++;
            if (!skipTo("*/")) {
              throw syntaxError("Unterminated comment");
            }
            p = pos + 2;
            l = limit;
            continue;

          case '/':
            // skip a // end-of-line comment
            pos++;
            skipToEndOfLine();
            p = pos;
            l = limit;
            continue;

          default:
            return c;
        }
      } else if (c == '#') {
        pos = p;
        /*
         * Skip a # hash end-of-line comment. The JSON RFC doesn't
         * specify this behaviour, but it's required to parse
         * existing documents. See http://b/2571423.
         */
        checkLenient();
        skipToEndOfLine();
        p = pos;
        l = limit;
      } else {
        pos = p;
        return c;
      }
    }
    if (throwOnEof) {
      String mess = "End of input at line " + getLineNumber() + " column " + getColumnNumber();
      logger.log(Level.SEVERE, mess);
      throw new JsonDeserializationException(mess);
    } else {
      return -1;
    }
  }

  private void checkLenient() {
    if (!lenient) {
      throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
    }
  }

  /**
   * Advances the position until after the next newline character. If the line is terminated by
   * "\r\n", the '\n' must be consumed as whitespace by the caller.
   */
  private void skipToEndOfLine() {
    while (pos < limit || fillBuffer(1)) {
      char c = buffer[pos++];
      if (c == '\n') {
        lineNumber++;
        lineStart = pos;
        break;
      } else if (c == '\r') {
        break;
      }
    }
  }

  /** @param toFind a string to search for. Must not contain a newline. */
  private boolean skipTo(String toFind) {
    outer:
    for (; pos + toFind.length() <= limit || fillBuffer(toFind.length()); pos++) {
      if (buffer[pos] == '\n') {
        lineNumber++;
        lineStart = pos + 1;
        continue;
      }
      for (int c = 0; c < toFind.length(); c++) {
        if (buffer[pos + c] != toFind.charAt(c)) {
          continue outer;
        }
      }
      return true;
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "JsonReader at line " + getLineNumber() + " column " + getColumnNumber();
  }

  /**
   * Unescapes the character identified by the character or characters that immediately follow a
   * backslash. The backslash '\' should have already been read. This supports both unicode escapes
   * "u000A" and two-character escapes "\n".
   *
   * @throws NumberFormatException if any unicode escape sequences are malformed.
   */
  private char readEscapeCharacter() {
    if (pos == limit && !fillBuffer(1)) {
      throw syntaxError("Unterminated escape sequence");
    }

    char escaped = buffer[pos++];
    switch (escaped) {
      case 'u':
        if (pos + 4 > limit && !fillBuffer(4)) {
          throw syntaxError("Unterminated escape sequence");
        }
        // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
        char result = 0;
        for (int i = pos, end = i + 4; i < end; i++) {
          char c = buffer[i];
          result <<= 4;
          if (c >= '0' && c <= '9') {
            result += (c - '0');
          } else if (c >= 'a' && c <= 'f') {
            result += (c - 'a' + 10);
          } else if (c >= 'A' && c <= 'F') {
            result += (c - 'A' + 10);
          } else {
            throw new NumberFormatException("\\u" + new String(buffer, pos, 4));
          }
        }
        pos += 4;
        return result;

      case 't':
        return '\t';

      case 'b':
        return '\b';

      case 'n':
        return '\n';

      case 'r':
        return '\r';

      case 'f':
        return '\f';

      case '\n':
        lineNumber++;
        lineStart = pos;
        // fall-through

      case '\'':
      case '"':
      case '\\':
      default:
        return escaped;
    }
  }

  /**
   * Throws a new IO exception with the given message and a context snippet with this reader's
   * content.
   */
  private MalformedJsonException syntaxError(String message) {
    String mess = message + " at line " + getLineNumber() + " column " + getColumnNumber();
    logger.log(Level.SEVERE, mess);
    throw new MalformedJsonException(mess);
  }

  /** Consumes the non-execute prefix if it exists. */
  private void consumeNonExecutePrefix() {
    // fast forward through the leading whitespace
    nextNonWhitespace(true);
    pos--;

    if (pos + NON_EXECUTE_PREFIX.length > limit && !fillBuffer(NON_EXECUTE_PREFIX.length)) {
      return;
    }

    for (int i = 0; i < NON_EXECUTE_PREFIX.length; i++) {
      if (buffer[pos + i] != NON_EXECUTE_PREFIX[i]) {
        return; // not a security token!
      }
    }

    // we consumed a security token!
    pos += NON_EXECUTE_PREFIX.length;
  }

  /** {@inheritDoc} */
  @Override
  public String getInput() {
    return in.getInput();
  }

  /** {@inheritDoc} */
  @Override
  public String nextValue() {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p == PEEKED_NULL) {
      peeked = PEEKED_NONE;
      return "null";
    }

    JsonWriter writer = new DefaultJsonWriter(new StringBuilder());
    writer.setLenient(true);

    int count = 0;
    do {
      p = peeked;
      if (p == PEEKED_NONE) {
        p = doPeek();
      }

      if (p == PEEKED_BEGIN_ARRAY) {
        push(JsonScope.EMPTY_ARRAY);
        count++;
        writer.beginArray();
      } else if (p == PEEKED_BEGIN_OBJECT) {
        push(JsonScope.EMPTY_OBJECT);
        count++;
        writer.beginObject();
      } else if (p == PEEKED_END_ARRAY) {
        stack.pop();
        count--;
        writer.endArray();
      } else if (p == PEEKED_END_OBJECT) {
        stack.pop();
        count--;
        writer.endObject();
      } else if (p == PEEKED_UNQUOTED_NAME) {
        writer.name(nextUnquotedValue());
      } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
        writer.name(nextQuotedValue('\''));
      } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
        writer.name(nextQuotedValue('"'));
      } else if (p == PEEKED_UNQUOTED) {
        writer.value(nextUnquotedValue());
      } else if (p == PEEKED_SINGLE_QUOTED) {
        writer.value(nextQuotedValue('\''));
      } else if (p == PEEKED_DOUBLE_QUOTED) {
        writer.value(nextQuotedValue('"'));
      } else if (p == PEEKED_NUMBER) {
        writer.value(new String(buffer, pos, peekedNumberLength));
        pos += peekedNumberLength;
      } else if (p == PEEKED_TRUE) {
        writer.value(true);
      } else if (p == PEEKED_FALSE) {
        writer.value(false);
      } else if (p == PEEKED_LONG) {
        writer.value(peekedLong);
      } else if (p == PEEKED_BUFFERED) {
        writer.value(peekedString);
      } else if (p == PEEKED_NULL) {
        writer.nullValue();
      }
      peeked = PEEKED_NONE;
    } while (count != 0);

    writer.close();
    return writer.getOutput();
  }

  /** {@inheritDoc} */
  @Override
  public Number nextNumber() {
    // TODO needs better handling for BigInteger and BigDecimal.
    // Use of DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS and USE_BIG_INTEGER_FOR_INTS. See
    // NumberDeserializer of Jackson.

    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    Number result;
    if (p == PEEKED_LONG) {
      if (peekedLong < 0l) {
        if (peekedLong >= MIN_INT_L) {
          result = (int) peekedLong;
        } else {
          result = peekedLong;
        }
      } else {
        if (peekedLong <= MAX_INT_L) {
          result = (int) peekedLong;
        } else {
          result = peekedLong;
        }
      }
      peeked = PEEKED_NONE;
      return result;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
      peeked = PEEKED_BUFFERED;
      result = Double.parseDouble(peekedString);
      peekedString = null;
      peeked = PEEKED_NONE;
      return result;
    }

    if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
    } else if (p == PEEKED_UNQUOTED) {
      peekedString = nextUnquotedValue();
    } else if (p != PEEKED_BUFFERED) {
      throw new IllegalStateException(
          "Expected a double but was "
              + peek()
              + " at line "
              + getLineNumber()
              + " column "
              + getColumnNumber());
    }

    peeked = PEEKED_BUFFERED;
    if (peekedString.contains(".")) {
      // decimal
      double resultDouble =
          Double.parseDouble(peekedString); // don't catch this NumberFormatException.
      if (!lenient && (Double.isNaN(resultDouble) || Double.isInfinite(resultDouble))) {
        throw syntaxError("JSON forbids NaN and infinities: " + resultDouble);
      }
      result = resultDouble;
    } else {
      int length = peekedString.length();
      if (length <= 9) { // fits in int
        result = Integer.parseInt(peekedString);
      } else if (length <= 18) { // fits in long and potentially int
        long longResult = Long.parseLong(peekedString);
        if (length == 10) { // can fits in int
          if (longResult < 0l) {
            if (longResult >= MIN_INT_L) {
              result = (int) longResult;
            } else {
              result = longResult;
            }
          } else {
            if (longResult <= MAX_INT_L) {
              result = (int) longResult;
            } else {
              result = longResult;
            }
          }
        } else {
          result = longResult;
        }
      } else {
        BigInteger bigIntegerResult = new BigInteger(peekedString);
        if (bigIntegerResult.signum() == -1) {
          if (bigIntegerResult.compareTo(MIN_LONG_BIGINTEGER) >= 0) {
            result = bigIntegerResult.longValue();
          } else {
            result = bigIntegerResult;
          }
        } else {
          if (bigIntegerResult.compareTo(MAX_LONG_BIGINTEGER) <= 0) {
            result = bigIntegerResult.longValue();
          } else {
            result = bigIntegerResult;
          }
        }
      }
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    return result;
  }
}
