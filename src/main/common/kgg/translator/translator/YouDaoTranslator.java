package kgg.translator.translator;

import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kgg.translator.exception.ErrorCodeException;
import kgg.translator.exception.TranslateException;
import kgg.translator.ocrtrans.ResRegion;
import kgg.translator.util.EasyProperties;
import kgg.translator.util.RequestUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class YouDaoTranslator extends Translator {
    public static final String URL = "https://openapi.youdao.com/api";
    private static final String OCR_URL = "https://openapi.youdao.com/ocrtransapi";
    protected String appId = "";
    protected String appKey = "";

    @Override
    protected synchronized String translate(String text, String from, String to) throws IOException {
        return delay(800, () -> {
            Map<String, Object> params = buildParams(text, from, to);
            params.put("strict", "true");
            // 请求
            String result = RequestUtil.get(URL, params);
            JsonObject object = (JsonObject) JsonParser.parseString(result);
            // 解析
            checkCode(Integer.parseInt(object.get("errorCode").getAsString()));
            return object.get("translation").getAsString();
        });
    }

    private Map<String, Object> buildParams(String q, String from, String to) {
        String curTime = String.valueOf(System.currentTimeMillis() / 1000);
        String input = q.length() > 20 ? q.substring(0, 10) + q.length() + q.substring(q.length() - 10) : q;
        String sign = Hashing.sha256().hashString(appId + input + curTime + curTime + appKey, StandardCharsets.UTF_8).toString();
        HashMap<String, Object> params = new HashMap<>();
        params.put("q", q);
        params.put("from", from);
        params.put("to", to);
        params.put("appKey", appId);
        params.put("salt", curTime);
        params.put("sign", sign);
        params.put("signType", "v3");
        params.put("curtime", curTime);
        return params;
    }

    @Override
    public ResRegion[] ocrtrans(byte[] img, String from, String to) throws IOException {
        String q = Base64.getEncoder().encodeToString(img);
        Map<String, Object> params = buildParams(q, from, to);
        params.put("type", "1");
        String result = RequestUtil.form(OCR_URL, params);

        JsonObject object = (JsonObject) JsonParser.parseString(result);
        checkCode(Integer.parseInt(object.get("errorCode").getAsString()));

        return object.getAsJsonArray("resRegions").asList().stream().map(JsonElement::getAsJsonObject).map(e -> {
            String rect = e.get("boundingBox").getAsString();
            List<Integer> list = Arrays.stream(rect.split(",")).map(Integer::parseInt).toList();
            return new ResRegion(list.get(0), list.get(1), list.get(2), list.get(3), e.get("tranContent").getAsString(), e.get("context").getAsString());
        }).toArray(ResRegion[]::new);
    }

    private void checkCode(int code) throws TranslateException {
        if (code != 0) {
            throw new ErrorCodeException("youdao", String.valueOf(code));
        }
    }

    public void setConfig(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
        setConfigured();
    }

    @Override
    public String getName() {
        return "有道翻译";
    }

    @Override
    public void read(JsonObject object) { // 考虑到2.0.0配置文件中的名字为appKey，为了兼容旧版本配置，故不更改
        setConfig(object.get("appKey").getAsString(), object.get("appSecret").getAsString());
    }

    @Override
    public void write(JsonObject object) {
        object.addProperty("appKey", appId);
        object.addProperty("appSecret", appKey);
    }
}
