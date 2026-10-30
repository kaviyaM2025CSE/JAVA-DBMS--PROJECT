
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskWebApp {
    private static final TaskService taskService = new TaskService();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/get-tasks", new GetTasksHandler());
        server.createContext("/submit-task", new AddTaskHandler());
        server.createContext("/update-task", new UpdateTaskStatusHandler()); 
        server.createContext("/delete-task", new DeleteTaskHandler());       
        
        server.setExecutor(null);
        System.out.println("🚀 VS Code Web Application started at http://localhost:8000/");
        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) path = "/index.html";
            if (path.startsWith("/")) path = path.substring(1);
            
            String filePath = "web/" + path;
            File file = new File(filePath);
            
            if (file.exists() && !file.isDirectory()) {
                byte[] content = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) { fis.read(content); }
                
                String contentType = "text/html; charset=UTF-8";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String error = "404 Not Found";
                exchange.sendResponseHeaders(404, error.length());
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }
    }

    static class GetTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder htmlRows = new StringBuilder();
            try {
                List<Task> tasks = taskService.getAllTasks();
                for (Task task : tasks) {
                   
                    String statusBtn = task.getStatus().equalsIgnoreCase("PENDING") ?
                        "<a href='/update-task?id=" + task.getId() + "&status=COMPLETED' class='action-btn complete-btn'>Mark Completed</a>" :
                        "<a href='/update-task?id=" + task.getId() + "&status=PENDING' class='action-btn pending-btn'>Mark Pending</a>";
                    
                    String deleteBtn = "<a href='/delete-task?id=" + task.getId() + "' class='action-btn delete-btn'>Delete</a>";

                    htmlRows.append("<tr>")
                            .append("<td>").append(task.getId()).append("</td>")
                            .append("<td>").append(task.getTitle()).append("</td>")
                            .append("<td><span class='status-badge " + task.getStatus().toLowerCase() + "'>").append(task.getStatus()).append("</span></td>")
                            .append("<td>").append(task.getPriority()).append("</td>")
                            .append("<td>").append(statusBtn).append(" ").append(deleteBtn).append("</td>")
                            .append("</tr>");
                }
            } catch (SQLException e) {
                htmlRows.append("<tr><td colspan='5'>Error: ").append(e.getMessage()).append("</td></tr>");
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlRows.toString().getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(htmlRows.toString().getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    static class AddTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                String formData = sb.toString();
                
                Map<String, String> inputs = parseFormData(formData);
                String rawTitle = inputs.getOrDefault("title", "New Task");
                String title = URLDecoder.decode(rawTitle, StandardCharsets.UTF_8);
                String priority = inputs.getOrDefault("priority", "MEDIUM");

                try { taskService.addTask(title, priority); } catch (SQLException e) { e.printStackTrace(); }
            }
            exchange.getResponseHeaders().set("Location", "/index.html");
            exchange.sendResponseHeaders(302, -1);
        }

        private Map<String, String> parseFormData(String formData) {
            Map<String, String> result = new HashMap<>();
            if (formData != null && !formData.isEmpty()) {
                for (String pair : formData.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length > 1) result.put(kv[0], kv[1]);
                    else if (kv.length == 1) result.put(kv[0], "");
                }
            }
            return result;
        }
    }

    
    static class UpdateTaskStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            int id = Integer.parseInt(params.get("id"));
            String status = params.get("status");

            try {
                taskService.updateTaskStatus(id, status);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            exchange.getResponseHeaders().set("Location", "/index.html");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    
    static class DeleteTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            int id = Integer.parseInt(params.get("id"));

            try {
                taskService.deleteTask(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            exchange.getResponseHeaders().set("Location", "/index.html");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length > 1) result.put(kv[0], kv[1]);
            }
        }
        return result;
    }
}
