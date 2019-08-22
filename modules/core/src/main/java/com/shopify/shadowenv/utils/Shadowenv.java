package com.shopify.shadowenv.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import com.intellij.execution.ExecutionException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;

public class Shadowenv {
    public static class UntrustedException extends ExecutionException {
        UntrustedException() {
            super("untrusted shadowenv program: run shadowenv help trust for more info");
        }
    }

    public static void evaluate(String pwd, Map<String, String> env) throws ExecutionException {
        ProcessBuilder p = new ProcessBuilder("shadowenv", "hook", "--json", "");
        p.directory(new File(pwd));
        BufferedReader er, fr;
        String out, err;
        try {
            Process proc = p.start();
            out = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
            err = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
        } catch (Exception e) {
            throw new ExecutionException(e);
        }

        if (!err.isEmpty()) {
            if (err.contains("untrusted")) {
                throw new UntrustedException();
            } else {
                throw new ExecutionException(err);
            }
        }
        try {
            JsonObject parsed = new JsonParser().parse(out).getAsJsonObject();
            JsonObject evs = parsed.getAsJsonObject("exported");
            for (Map.Entry<String, JsonElement> e : evs.entrySet()) {
                if (e.getValue() == null) {
                    env.remove(e.getKey());
                } else {
                    env.put(e.getKey(), e.getValue().getAsString());
                }
            }
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
}
