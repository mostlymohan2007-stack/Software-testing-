import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local practice application used by the Selenium (38-40, 44) and JMeter (46-60) experiments.
 * It listens only on 127.0.0.1, so load tests never touch a real website.
 *
 * Run (Java 21 or newer) from this folder:   java PracticeServer.java
 *
 * The result-lookup and ticket-booking pages have a deliberately limited capacity,
 * so that stress and capacity tests have a breaking point to find.
 */
public class PracticeServer {

    static final Map<String, String> USERS = new HashMap<>();
    static final AtomicLong REQUESTS = new AtomicLong();
    static final Semaphore RESULT_WORKERS = new Semaphore(40, true);   // 40 x 200 ms = about 200 req/s
    static final Semaphore BOOKING_WORKERS = new Semaphore(60, true);  // 60 x 150 ms = about 400 req/s

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8000;
        System.setProperty("sun.net.httpserver.maxIdleConnections", "3000");
        System.setProperty("sun.net.httpserver.nodelay", "true");

        List<String> lines = Files.readAllLines(Path.of("users.csv"));
        for (String line : lines.subList(1, lines.size())) {
            String[] p = line.split(",");
            if (p.length == 3 && p[2].equals("200")) USERS.put(p[0], p[1]);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 4096);
        server.createContext("/", PracticeServer::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.println("Practice server running at http://127.0.0.1:" + port + "  (Ctrl+C to stop)");

        long start = System.currentTimeMillis();
        while (true) {
            Thread.sleep(10_000);
            Runtime rt = Runtime.getRuntime();
            System.out.printf("[metrics] uptime=%ds requests=%d heapUsedMB=%d%n",
                    (System.currentTimeMillis() - start) / 1000, REQUESTS.get(),
                    (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024));
        }
    }

    static void handle(HttpExchange ex) throws IOException {
        REQUESTS.incrementAndGet();
        try (ex) {
            String path = ex.getRequestURI().getPath();
            boolean post = ex.getRequestMethod().equals("POST");
            Map<String, String> query = form(ex.getRequestURI().getRawQuery());
            byte[] raw = ex.getRequestBody().readAllBytes();
            String body = raw.length < 100_000 ? new String(raw, StandardCharsets.UTF_8) : "";

            if (path.equals("/") || path.equals("/my-government")) {
                pause(15, 60);
                page(ex, 200, "Practice Site", "<h1>Practice Site " + path + "</h1><p>Service is available.</p>");
            } else if (path.equals("/search")) {
                String q = clean(query.get("q"));
                page(ex, 200, q.isEmpty() ? "Practice Search" : q + " - Practice Search", "<h1>Practice Search</h1>"
                        + "<form><input name='q' value='" + q + "' placeholder='Search'></form>"
                        + (q.isEmpty() ? "" : "<p>Results for <b>" + q + "</b></p><ul><li>" + q + " - Home</li><li>" + q + " - Courses</li></ul>"));
            } else if (path.equals("/register")) {
                register(ex, post, form(body));
            } else if (path.equals("/portal/login")) {
                portalLogin(ex, post, form(body));
            } else if (path.equals("/portal/home")) {
                String user = cookie(ex, "portal_user");
                if (user == null) redirect(ex, "/portal/login");
                else page(ex, 200, "Practice Portal - Home",
                        "<h1 id='welcome'>Welcome, " + user + "</h1><a id='logout' href='/portal/logout'>Logout</a>");
            } else if (path.equals("/portal/logout")) {
                ex.getResponseHeaders().add("Set-Cookie", "portal_user=; Path=/; Max-Age=0");
                redirect(ex, "/portal/login");
            } else if (path.equals("/upload")) {
                upload(ex, post, body);
            } else if (path.equals("/Login.aspx")) {
                pause(40, 120);
                Map<String, String> in = post ? form(body) : query;
                boolean ok = valid(in.get("username"), in.get("password"));
                // GET shows a login page (always 200); POST is the login API (200 or 401).
                page(ex, ok || !post ? 200 : 401, "Practice Student Login",
                        ok ? "<p>Login successful</p>" : "<p>Invalid username or password</p>");
            } else if (path.equals("/api/balance")) {
                pause(20, 80);
                if ("12345".equals(query.get("accno")))
                    json(ex, 200, "{\"accno\":\"12345\",\"balance\":25480.50,\"currency\":\"INR\"}");
                else json(ex, 404, "{\"error\":\"Account not found\"}");
            } else if (path.equals("/api/register") && post) {
                pause(30, 90);
                if (field(body, "email") == null) json(ex, 400, "{\"error\":\"Missing email\"}");
                else if (field(body, "password") == null) json(ex, 400, "{\"error\":\"Missing password\"}");
                else json(ex, 200, "{\"id\":" + REQUESTS.get() + ",\"status\":\"registered\"}");
            } else if (path.equals("/results")) {
                limited(ex, RESULT_WORKERS, 200, 2000,
                        "{\"regno\":\"" + clean(query.get("regno")) + "\",\"result\":\"PASS\"}");
            } else if (path.equals("/booking") && post) {
                limited(ex, BOOKING_WORKERS, 150, 1000, "{\"booking\":\"CONFIRMED\"}");
            } else if (path.equals("/transaction") && post) {
                pause(50, 150);
                if (field(body, "amount") == null) json(ex, 400, "{\"status\":\"FAILED\"}");
                else json(ex, 200, "{\"status\":\"SUCCESS\",\"txnId\":" + REQUESTS.get() + "}");
            } else if (path.equals("/cookies/set")) {
                pause(20, 60);
                String id = clean(query.get("session_id"));
                ex.getResponseHeaders().add("Set-Cookie", "session_id=" + id + "; Path=/");
                json(ex, 200, "{\"cookies\":{\"session_id\":\"" + id + "\"}}");
            } else if (path.equals("/cookies/delete")) {
                pause(20, 60);
                ex.getResponseHeaders().add("Set-Cookie", "session_id=; Path=/; Max-Age=0");
                json(ex, 200, "{\"cookies\":{}}");
            } else if (path.equals("/get")) {
                pause(20, 90);
                String id = cookie(ex, "session_id");
                if (id == null) json(ex, 401, "{\"error\":\"No session\"}");
                else json(ex, 200, "{\"session_id\":\"" + id + "\"}");
            } else if (path.equals("/reserve.php") && post) {
                pause(60, 180);
                Map<String, String> in = form(body);
                page(ex, 200, "Flights", "<h3>Flights from " + clean(in.get("fromPort")) + " to "
                        + clean(in.get("toPort")) + ":</h3><p>PS101, PS202, PS303</p>");
            } else if (path.equals("/login/index.php")) {
                pause(30, 100);
                page(ex, 200, "Practice LMS", "<h1>Log in to Practice LMS</h1>");
            } else if (path.equals("/post") && post) {
                json(ex, 200, "{\"bytesReceived\":" + raw.length + "}");
            } else if (path.startsWith("/bytes/")) {
                int n = Math.min(Integer.parseInt(path.substring(7)), 20 * 1024 * 1024);
                send(ex, 200, "application/octet-stream", new byte[n]);
            } else if (path.equals("/v1/forecast")) {
                pause(25, 100);
                json(ex, 200, "{\"latitude\":" + number(query.get("latitude")) + ",\"longitude\":"
                        + number(query.get("longitude")) + ",\"current_weather\":{\"temperature\":18.4,\"windspeed\":9.2}}");
            } else {
                page(ex, 404, "Not found", "<p>Not found</p>");
            }
        } catch (Exception e) {
            System.out.println("[error] " + e);
        }
    }

    static void register(HttpExchange ex, boolean post, Map<String, String> in) throws IOException {
        String msg = "";
        if (post) {
            boolean ok = !in.getOrDefault("name", "").isBlank()
                    && in.getOrDefault("email", "").contains("@")
                    && in.getOrDefault("password", "").length() >= 8;
            msg = ok ? "<p id='message'>Registration successful for " + clean(in.get("name")) + "</p>"
                     : "<p id='message'>Please enter valid details</p>";
        }
        page(ex, 200, "Practice Registration", "<h1>Create account</h1><form method='post'>"
                + "<input id='name' name='name' placeholder='Name'>"
                + "<input id='email' name='email' placeholder='Email'>"
                + "<input id='password' name='password' type='password' placeholder='Password'>"
                + "<button id='submit'>Register</button></form>" + msg);
    }

    static void portalLogin(HttpExchange ex, boolean post, Map<String, String> in) throws IOException {
        String msg = "";
        if (post) {
            if (valid(in.get("txtusername"), in.get("txtpassword"))) {
                ex.getResponseHeaders().add("Set-Cookie", "portal_user=" + clean(in.get("txtusername")) + "; Path=/");
                redirect(ex, "/portal/home");
                return;
            }
            msg = "<p id='error' style='color:#b00020'>Invalid username or password</p>";
        }
        page(ex, 200, "Practice Portal - Login", "<h1>Practice Student Portal</h1><form method='post'>"
                + "<input id='txtusername' name='txtusername' placeholder='Username'>"
                + "<input id='txtpassword' name='txtpassword' type='password' placeholder='Password'>"
                + "<button id='btnlogin'>Login</button></form>" + msg);
    }

    static void upload(HttpExchange ex, boolean post, String body) throws IOException {
        String msg = "";
        if (post) {
            Matcher m = Pattern.compile("filename=\"([^\"]*)\"").matcher(body);
            msg = m.find() && !m.group(1).isEmpty()
                    ? "<h2>File Uploaded!</h2><div id='uploaded-files'>" + clean(m.group(1)) + "</div>"
                    : "<p>No file selected</p>";
        }
        page(ex, 200, "Practice File Upload", "<h1>File Uploader</h1>"
                + "<form method='post' enctype='multipart/form-data'><input type='file' id='file-upload' name='file'>"
                + "<button id='file-submit'>Upload</button></form>" + msg);
    }

    /** Only a fixed number of requests are processed at once; the rest wait and may time out. */
    static void limited(HttpExchange ex, Semaphore workers, int workMs, int maxWaitMs, String ok) throws Exception {
        if (!workers.tryAcquire(maxWaitMs, TimeUnit.MILLISECONDS)) {
            json(ex, 503, "{\"error\":\"Server busy\"}");
            return;
        }
        try {
            Thread.sleep(workMs);
        } finally {
            workers.release();
        }
        json(ex, 200, ok);
    }

    static boolean valid(String user, String password) {
        return user != null && password != null && password.equals(USERS.get(user));
    }

    static void pause(int min, int max) throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(min, max));
    }

    static Map<String, String> form(String data) {
        Map<String, String> map = new HashMap<>();
        if (data == null) return map;
        for (String pair : data.split("&")) {
            String[] kv = pair.split("=", 2);
            try {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
            } catch (IllegalArgumentException ignored) {
                // not form data
            }
        }
        return map;
    }

    static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"?([^\",}\\s]+)").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static String cookie(HttpExchange ex, String name) {
        for (String header : ex.getRequestHeaders().getOrDefault("Cookie", List.of())) {
            for (String c : header.split(";")) {
                String[] kv = c.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(name) && !kv[1].isEmpty()) return clean(kv[1]);
            }
        }
        return null;
    }

    static String clean(String s) {
        return s == null ? "" : s.replaceAll("[^A-Za-z0-9 ._@-]", "");
    }

    static String number(String s) {
        return s != null && s.matches("-?\\d+(\\.\\d+)?") ? s : "0";
    }

    static void page(HttpExchange ex, int code, String title, String content) throws IOException {
        String html = "<!doctype html><html><head><meta charset='utf-8'><title>" + title + "</title><style>"
                + "body{font-family:Segoe UI,Arial;max-width:520px;margin:60px auto}"
                + "input,button{display:block;width:100%;padding:10px;margin:10px 0;box-sizing:border-box}"
                + "small{color:#777}</style></head><body><small>LOCAL PRACTICE PAGE (127.0.0.1)</small>"
                + content + "</body></html>";
        send(ex, code, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8));
    }

    static void json(HttpExchange ex, int code, String body) throws IOException {
        send(ex, code, "application/json", body.getBytes(StandardCharsets.UTF_8));
    }

    static void redirect(HttpExchange ex, String to) throws IOException {
        ex.getResponseHeaders().add("Location", to);
        ex.sendResponseHeaders(302, -1);
    }

    static void send(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, data.length == 0 ? -1 : data.length);
        if (data.length > 0) {
            try (OutputStream out = ex.getResponseBody()) {
                out.write(data);
            }
        }
    }
}
