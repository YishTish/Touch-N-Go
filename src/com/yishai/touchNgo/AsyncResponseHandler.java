package com.yishai.touchNgo;

import org.json.JSONObject;

public interface AsyncResponseHandler {

	void processFinish(JSONObject response);
}
