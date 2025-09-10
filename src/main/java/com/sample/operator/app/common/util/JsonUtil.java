package com.sample.operator.app.common.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JsonUtil
{

    // 들어온 json에 대해서 key:value 형식의 Map으로 리턴
    public Map<String, Object> parse(JsonElement element, String indent)
    {
        Map<String, Object> parsedMap = new HashMap<>();

        if (element.isJsonObject()) 
        {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) 
            {
                System.out.println(indent + entry.getKey() + ":");
                parse(entry.getValue(), indent + "  ");


                parsedMap.put(indent + entry.getKey(), entry.getValue() + indent + "  ");

            }
        } 
        else if (element.isJsonArray()) 
        {
            JsonArray array = element.getAsJsonArray();
            int index = 0;
            for (JsonElement item : array) 
            {
                System.out.println(indent + "[" + index + "]:");
                parsedMap.put(indent + "[" + index + "]", item);

                parse(item, indent + "  ");
                index++;
            }


        }
        else if (element.isJsonPrimitive()) 
        {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            System.out.println(indent + primitive);

            parsedMap.put(indent + primitive.getAsString(), primitive);
        } 
        else if (element.isJsonNull()) 
        {
            System.out.println(indent + "null");

            parsedMap.put(indent + "null", null);
        }

        return parsedMap;
    }
}
