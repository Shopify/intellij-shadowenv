package com.shopify.shadowenv.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

public class Shadowenv {
    public static class ExecutionException extends Exception {
        ExecutionException(String err) {
            super(err);
        }

        ExecutionException(Throwable e) {
            super(e);
        }
    }

    public static class UntrustedException extends ExecutionException {
        UntrustedException() {
            super("untrusted shadowenv program: run shadowenv help trust for more info");
        }
    }

    public static void modifyEnv(String dir, Map<String, String> env) throws ExecutionException {
        JsonObject evs = Shadowenv.getExportedEnv(dir);
        for (Map.Entry<String, JsonElement> entry : evs.entrySet()) {
            if (entry.getValue().isJsonNull()) {
                env.remove(entry.getKey());
            } else {
                env.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
    }

    public static JsonObject getExportedEnv(String dir) throws ExecutionException {
        ProcessBuilder p = new ProcessBuilder("shadowenv", "hook", "--json", "");
        p.directory(new File(dir));
        try {
            Process proc = p.start();
            String out = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
            String err = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());

            if (!err.isEmpty()) {
                if (err.contains("untrusted")) {
                    throw new UntrustedException();
                } else {
                    throw new ExecutionException(err);
                }
            }

            JsonObject parsed = new JsonParser().parse(out).getAsJsonObject();
            return parsed.getAsJsonObject("exported");
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
}
