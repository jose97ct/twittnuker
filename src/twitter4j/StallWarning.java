/*
 * Copyright 2007 Yusuke Yamamoto
 * Copyright (C) 2012 Twitter, Inc.
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
package twitter4j;

import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Yusuke Yamamoto - yusuke at twitter.com
 * @since Twitter4J 3.0.0
 */
public final class StallWarning implements Serializable {
	private static final long serialVersionUID = 7387184309206228363L;
	private final String code;
	private final String message;
	private final int percentFull;

	StallWarning(final JSONObject json) throws JSONException {
		final JSONObject warning = json.getJSONObject("warning");
		code = getRawString("code", warning);
		message = getRawString("message", warning);
		percentFull = getInt("percent_full", warning);

	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final StallWarning that = (StallWarning) o;

		if (percentFull != that.percentFull) return false;
		if (code != null ? !code.equals(that.code) : that.code != null) return false;
		if (message != null ? !message.equals(that.message) : that.message != null) return false;

		return true;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public int getPercentFull() {
		return percentFull;
	}

	@Override
	public int hashCode() {
		int result = code != null ? code.hashCode() : 0;
		result = 31 * result + (message != null ? message.hashCode() : 0);
		result = 31 * result + percentFull;
		return result;
	}

	@Override
	public String toString() {
		return "StallWarning{" + "code='" + code + '\'' + ", message='" + message + '\'' + ", percentFull="
				+ percentFull + '}';
	}
}
