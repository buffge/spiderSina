package spidersina;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * json初始化类
 * @author buff
 */
public class JsonHelper {

    public JsonHelper() throws JSONException {
    }
    /**
     * 解析json并返回json对象
     * @param jsonFilePath　json文件路径
     * @return　json对象
     * @throws JSONException 
     */
    public static JSONObject ParseJson(String jsonFilePath) throws JSONException {
        String sets = ReadFile(jsonFilePath);
        JSONObject jObject = new JSONObject(sets);
        return jObject;
    }
    /**
     * 从文件中读取文本并返回
     * @param path　文件路径
     * @return 文件中的文本
     */
    public static String ReadFile(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        String laststr = "";
        try {
            reader = new BufferedReader(new FileReader(file));

            for (String e = null; (e = reader.readLine()) != null; laststr = laststr + e) {
            }

            reader.close();
        } catch (IOException var13) {
            var13.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException var12) {
                }
            }

        }

        return laststr;
    }
}
