package dev.langchain4j.mcp.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A minimal MCP server for the STDIO transport, started as a subprocess by the tests.
 * It exposes one tool whose description is the value of the {@code MCP_TEST_VALUE} environment variable.
 * When started with the {@code fail} argument, it answers every request with an error.
 */
public class StdioTestMcpServer {

    private static final Pattern REQUEST_ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
    private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]+)\"");

    public static void main(String[] args) throws IOException {
        boolean fail = args.length > 0 && args[0].equals("fail");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in, UTF_8));
        String request;
        while ((request = input.readLine()) != null) {
            Matcher id = REQUEST_ID.matcher(request);
            if (!id.find()) {
                continue; // notifications need no response
            }
            if (fail) {
                System.out.println("{\"jsonrpc\":\"2.0\",\"id\":" + id.group(1)
                        + ",\"error\":{\"code\":-32021,\"message\":\"failing on purpose\"}}");
                System.out.flush();
                continue;
            }
            Matcher method = METHOD.matcher(request);
            String result = method.find() && method.group(1).equals("tools/list")
                    ? "{\"tools\":[{\"name\":\"echo_environment\",\"description\":\"" + System.getenv("MCP_TEST_VALUE")
                    + "\",\"inputSchema\":{\"type\":\"object\",\"properties\":{}}}]}"
                    : "{\"supportedVersions\":[\"2026-07-28\"],\"capabilities\":{\"tools\":{}}}";
            System.out.println("{\"jsonrpc\":\"2.0\",\"id\":" + id.group(1) + ",\"result\":" + result + "}");
            System.out.flush();
        }
    }
}
